package com.example.oficiolocal.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["createdAt"])]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val category: String,
    val relatedRequestId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val pendingSync: Boolean = true
)
