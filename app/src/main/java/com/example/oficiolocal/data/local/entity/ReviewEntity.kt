package com.example.oficiolocal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.oficiolocal.domain.Review

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val userName: String,
    val rating: Double,
    val comment: String,
    val date: String,
    val updatedAt: Long,
    val pendingSync: Boolean
)

fun ReviewEntity.toDomain() = Review(
    id = id,
    providerId = providerId,
    userName = userName,
    rating = rating,
    comment = comment,
    date = date
)