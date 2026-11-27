package com.example.oficiolocal.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.oficiolocal.data.local.dao.MediaAttachmentDao
import com.example.oficiolocal.data.local.entity.MediaAttachmentEntity
import java.io.File
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
        val mimeType = resolver.getType(uri) ?: "image/*"
        val extension = fileName?.substringAfterLast('.', "")
            ?.takeIf { it.matches(Regex("[A-Za-z0-9]{1,8}")) }
            ?: "img"
        val directory = File(context.filesDir, "$MEDIA_DIRECTORY/$requestId")
        check(directory.mkdirs() || directory.isDirectory) { "No se pudo crear la carpeta local de imágenes" }
        val localFile = File(directory, "${UUID.randomUUID()}.$extension")
        try {
            val input = resolver.openInputStream(uri)
                ?: error("No se pudo leer la imagen seleccionada")
            input.use { source ->
                localFile.outputStream().use { destination -> source.copyTo(destination) }
            }
        } catch (error: Exception) {
            localFile.delete()
            throw error
        }

        val attachment = MediaAttachmentEntity(
            id = UUID.randomUUID().toString(),
            ownerType = REQUEST_OWNER,
            ownerId = requestId,
            uri = Uri.fromFile(localFile).toString(),
            mimeType = mimeType,
            fileName = fileName,
            sizeBytes = localFile.length(),
            syncState = "LOCAL_ONLY"
        )
        dao.upsert(attachment)
    }

    /** Normaliza adjuntos previos; esta versión no sincroniza imágenes con la nube. */
    suspend fun normalizeLocalOnlyAttachments() = dao.markAllLocalOnly()

    /** No descarga adjuntos porque esta versión mantiene las imágenes en el dispositivo. */
    @Suppress("UNUSED_PARAMETER")
    suspend fun refreshFromCloud(requestId: String) = Unit

    suspend fun delete(attachmentId: String) {
        val attachment = dao.getById(attachmentId)
        val filePath = attachment?.uri?.let { Uri.parse(it).path }
        if (filePath != null) {
            val file = File(filePath).canonicalFile
            val mediaRoot = File(context.filesDir, MEDIA_DIRECTORY).canonicalFile
            if (file.canonicalPath.startsWith(mediaRoot.canonicalPath + File.separator)) {
                file.delete()
            }
        }
        dao.delete(attachmentId)
    }

    suspend fun deleteForRequest(requestId: String) {
        dao.getForOwner(REQUEST_OWNER, requestId).forEach { attachment ->
            delete(attachment.id)
        }
    }

    private companion object {
        const val REQUEST_OWNER = "REQUEST"
        const val MEDIA_DIRECTORY = "request_media"
    }
}
