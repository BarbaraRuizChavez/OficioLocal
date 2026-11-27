package com.example.oficiolocal.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Guarda las fotos SOLO en este teléfono (almacenamiento interno de la app).
 * No se sube nada a internet. Si se desinstala la app o se borran sus datos, las fotos se pierden.
 */
class PhotoStorage(private val context: Context) {

    private val root: File get() = File(context.filesDir, "photos")

    /** Copia la imagen (reducida) a [folder] y devuelve su URI "file://...". */
    suspend fun save(folder: String, uri: Uri): String = withContext(Dispatchers.IO) {
        val dir = File(root, folder).apply { mkdirs() }
        val file = File(dir, "${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out -> out.write(compress(uri)) }
        Uri.fromFile(file).toString()
    }

    /** Borra una foto guardada. Solo toca archivos dentro de la carpeta de fotos de la app. */
    suspend fun delete(url: String) = withContext(Dispatchers.IO) {
        runCatching {
            val path = Uri.parse(url).path ?: return@runCatching
            val file = File(path)
            if (file.canonicalPath.startsWith(root.canonicalPath)) file.delete()
        }
        Unit
    }

    private fun compress(uri: Uri): ByteArray {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > MAX_SIDE * 2 || bounds.outHeight / sample > MAX_SIDE * 2) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        var bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IllegalStateException("No se pudo leer la imagen")

        val rotation = resolver.openInputStream(uri)?.use { stream ->
            runCatching {
                when (ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            }.getOrDefault(0f)
        } ?: 0f

        val scale = min(1f, MAX_SIDE.toFloat() / max(bitmap.width, bitmap.height))
        if (scale < 1f || rotation != 0f) {
            val matrix = Matrix().apply {
                postScale(scale, scale)
                postRotate(rotation)
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        return java.io.ByteArrayOutputStream().also {
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it)
        }.toByteArray()
    }

    private companion object {
        const val MAX_SIDE = 1280
        const val JPEG_QUALITY = 85
    }
}
