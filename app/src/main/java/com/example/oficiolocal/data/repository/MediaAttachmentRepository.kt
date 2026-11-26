package com.example.oficiolocal.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.oficiolocal.data.local.dao.MediaAttachmentDao
import com.example.oficiolocal.data.local.entity.MediaAttachmentEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class MediaAttachmentRepository(
    private val dao: MediaAttachmentDao,
    private val context: Context
) {
    fun observeForRequest(requestId: String): Flow<List<MediaAttachmentEntity>> =
        dao.observeForOwner(REQUEST_OWNER, requestId)

    suspend fun addPhotoToRequest(requestId: String, uri: Uri) {
        val resolver = context.contentResolver
        val fileName = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        }.getOrNull()
        val sizeBytes = runCatching {
            resolver.openAssetFileDescriptor(uri, "r")?.use { it.length.coerceAtLeast(0L) }
        }.getOrNull() ?: 0L

        val attachment = MediaAttachmentEntity(
            id = UUID.randomUUID().toString(),
            ownerType = REQUEST_OWNER,
            ownerId = requestId,
            uri = uri.toString(),
            mimeType = resolver.getType(uri) ?: "image/*",
            fileName = fileName,
            sizeBytes = sizeBytes,
            syncState = "PENDING"
        )
        dao.upsert(attachment)
        upload(attachment)
    }

    suspend fun syncPendingUploads() {
        for (attachment in dao.getPendingUploads()) {
            if (attachment.ownerType == REQUEST_OWNER) upload(attachment)
        }
    }

    suspend fun refreshFromCloud(requestId: String) {
        if (FirebaseAuth.getInstance().currentUser == null) return
        val documents = FirebaseFirestore.getInstance()
            .collection("requests")
            .document(requestId)
            .collection("attachments")
            .get()
            .await()

        documents.documents.forEach { document ->
            val downloadUrl = document.getString("downloadUrl") ?: return@forEach
            val existing = dao.getById(document.id)
            val attachment = existing?.copy(
                remoteUrl = downloadUrl,
                syncState = "SYNCED"
            ) ?: MediaAttachmentEntity(
                id = document.id,
                ownerType = REQUEST_OWNER,
                ownerId = requestId,
                uri = downloadUrl,
                mimeType = document.getString("mimeType") ?: "image/*",
                fileName = document.getString("fileName"),
                sizeBytes = document.getLong("sizeBytes") ?: 0L,
                remoteUrl = downloadUrl,
                syncState = "SYNCED"
            )
            dao.upsert(attachment)
        }
    }

    private suspend fun upload(attachment: MediaAttachmentEntity) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            dao.update(attachment.copy(syncState = "LOCAL_ONLY"))
            return
        }

        val remoteFile = FirebaseStorage.getInstance().reference
            .child("requests/${attachment.ownerId}/users/$userId/${attachment.id}")
        try {
            val metadata = StorageMetadata.Builder()
                .setContentType(attachment.mimeType)
                .build()
            remoteFile.putFile(Uri.parse(attachment.uri), metadata).await()
            val downloadUrl = remoteFile.downloadUrl.await().toString()
            FirebaseFirestore.getInstance()
                .collection("requests")
                .document(attachment.ownerId)
                .collection("attachments")
                .document(attachment.id)
                .set(
                    mapOf(
                        "downloadUrl" to downloadUrl,
                        "mimeType" to attachment.mimeType,
                        "fileName" to attachment.fileName,
                        "sizeBytes" to attachment.sizeBytes,
                        "uploaderUid" to userId,
                        "createdAt" to attachment.createdAt
                    )
                )
                .await()
            dao.update(attachment.copy(remoteUrl = downloadUrl, syncState = "SYNCED"))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // El archivo sigue disponible localmente y se volverá a intentar al iniciar la app.
            dao.update(attachment.copy(syncState = "FAILED"))
        }
    }

    suspend fun delete(attachmentId: String) = dao.delete(attachmentId)

    private companion object {
        const val REQUEST_OWNER = "REQUEST"
    }
}
