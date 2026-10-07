package com.example.oficiolocal.domain

data class User(
    val id: String,
    val name: String,
    val email: String,
    val isProvider: Boolean = false
)