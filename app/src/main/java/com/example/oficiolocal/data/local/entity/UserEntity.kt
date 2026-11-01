package com.example.oficiolocal.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.oficiolocal.domain.User

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val isProvider: Boolean,
    val updatedAt: Long,
    val pendingSync: Boolean
)

fun UserEntity.toDomain() = User(id = id, name = name, email = email, isProvider = isProvider)