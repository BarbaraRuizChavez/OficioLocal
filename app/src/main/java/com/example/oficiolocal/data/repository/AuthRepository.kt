package com.example.oficiolocal.data.repository

import com.example.oficiolocal.domain.User

class AuthRepository {
    private var currentUser: User? = null

    fun login(email: String, password: String): Boolean {
        if (email.isNotBlank() && password.length >= 6) {
            currentUser = User(
                id = "usr_101",
                name = "Usuario Ejemplo",
                email = email
            )
            return true
        }
        return false
    }

    fun logout() {
        currentUser = null
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    fun isLoggedIn(): Boolean {
        return currentUser != null
    }
}