package com.example.oficiolocal.domain

enum class Trade { PLUMBING, ELECTRICITY, CARPENTRY, TECH }

data class Provider(
    val id: String,
    val name: String,
    val trade: Trade,
    val zone: String,
    val rating: Double,
    val approxPrice: Int,
    val availableToday: Boolean
)