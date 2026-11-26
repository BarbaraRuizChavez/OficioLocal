package com.example.oficiolocal.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.oficiolocal.data.local.dao.MediaAttachmentDao
import com.example.oficiolocal.data.local.entity.MediaAttachmentEntity
import java.util.UUID
import kotlinx.coroutines.flow.Flow

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

        dao.upsert(
            MediaAttachmentEntity(
                id = UUID.randomUUID().toString(),
                ownerType = REQUEST_OWNER,
                ownerId = requestId,
                uri = uri.toString(),
                mimeType = resolver.getType(uri) ?: "image/*",
                fileName = fileName,
                sizeBytes = sizeBytes,
                syncState = "LOCAL_ONLY"
            )
        )
    }

    suspend fun delete(attachmentId: String) = dao.delete(attachmentId)

    private companion object {
        const val REQUEST_OWNER = "REQUEST"
    }
}
