package com.example.oficiolocal.data.local.entity

import androidx.room.Entity

@Entity(tableName = "favorites", primaryKeys = ["clientId", "providerId"])
data class FavoriteEntity(
    val clientId: String,
    val providerId: String,
    val updatedAt: Long,
    val pendingSync: Boolean
)