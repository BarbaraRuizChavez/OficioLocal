package com.example.oficiolocal.domain

data class Provider(
    val id: String,
    val name: String,
    val category: String,
    val location: String,
    val rating: Double,
    val reviewCount: Int,
    val priceRange: String,
    val schedule: String,
    val phone: String,
    val description: String,
    val services: List<String>,
    /** UID Firebase del usuario propietario; null para prestadores de demostración. */
    val ownerUid: String? = null
)
