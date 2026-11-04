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
    val services: String // servicios separados por "|"
)

fun ProviderEntity.toDomain() = Provider(
    id = id, name = name, category = category, location = location,
    rating = rating, reviewCount = reviewCount, priceRange = priceRange,
    schedule = schedule, phone = phone, description = description,
    services = if (services.isBlank()) emptyList() else services.split("|")
)

fun Provider.toEntity() = ProviderEntity(
    id = id, name = name, category = category, location = location,
    rating = rating, reviewCount = reviewCount, priceRange = priceRange,
    schedule = schedule, phone = phone, description = description,
    services = services.joinToString("|")
)