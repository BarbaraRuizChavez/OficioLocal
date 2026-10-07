package com.example.oficiolocal.domain

data class Review(
    val id: String,
    val providerId: String,
    val userName: String,
    val rating: Double,
    val comment: String,
    val date: String
)