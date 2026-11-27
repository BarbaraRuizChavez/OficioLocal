package com.example.oficiolocal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.oficiolocal.domain.Provider

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val location: String,
    val rating: Double,
    val reviewCount: Int,
    val priceRange: String,
    val schedule: String,
    val phone: String,
    val description: String,
    val services: String, // servicios separados por "|"
    val ownerUid: String? = null,
    val photoUrl: String? = null,
    val gallery: String = "" // URLs separadas por salto de línea
)

fun ProviderEntity.toDomain() = Provider(
    id = id, name = name, category = category, location = location,
    rating = rating, reviewCount = reviewCount, priceRange = priceRange,
    schedule = schedule, phone = phone, description = description,
    services = if (services.isBlank()) emptyList() else services.split("|"),
    ownerUid = ownerUid,
    photoUrl = photoUrl,
    gallery = if (gallery.isBlank()) emptyList() else gallery.split("\n")
)

fun Provider.toEntity() = ProviderEntity(
    id = id, name = name, category = category, location = location,
    rating = rating, reviewCount = reviewCount, priceRange = priceRange,
    schedule = schedule, phone = phone, description = description,
    services = services.joinToString("|"),
    ownerUid = ownerUid,
    photoUrl = photoUrl,
    gallery = gallery.joinToString("\n")
)
