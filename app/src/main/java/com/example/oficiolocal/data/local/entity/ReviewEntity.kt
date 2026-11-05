package com.example.oficiolocal.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.oficiolocal.domain.Review

@Entity(tableName = "reviews", indices = [Index(value = ["remoteId"], unique = true)])
data class ReviewEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val userName: String,
    val rating: Double,
    val comment: String,
    val date: String,
    val updatedAt: Long,
    val pendingSync: Boolean,
    val remoteId: String? = null,
    @ColumnInfo(defaultValue = "0") val pendingDelete: Boolean = false
)

fun ReviewEntity.toDomain() = Review(
    id = id,
    providerId = providerId,
    userName = userName,
    rating = rating,
    comment = comment,
    date = date
)
