package com.example.oficiolocal.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Guarda metadatos y la URI; el archivo multimedia permanece en almacenamiento del dispositivo. */
@Entity(
    tableName = "media_attachments",
    indices = [Index(value = ["ownerType", "ownerId"])]
)
data class MediaAttachmentEntity(
    @PrimaryKey val id: String,
    val ownerType: String,
    val ownerId: String,
    val uri: String,
    val mimeType: String,
    val fileName: String? = null,
    val sizeBytes: Long = 0,
    val remoteUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncState: String = "PENDING"
)
