package com.example.oficiolocal.data.repository

import com.example.oficiolocal.domain.User

/** Usuarios de prueba en memoria. En la siguiente etapa se reemplaza por la API con token JWT. */
class AuthRepository {

    private data class Account(val user: User, val password: String)

    private val accounts = mutableListOf(
        Account(User("u1", "Ana Torres", "cliente@correo.com", false), "123456"),
        Account(User("u2", "Juan Pérez", "prestador@correo.com", true), "123456")
    )

    private var currentUser: User? = null

    fun login(email: String, password: String): Boolean {
        val account = accounts.firstOrNull {
            it.user.email.equals(email.trim(), ignoreCase = true) && it.password == password
        } ?: return false
        currentUser = account.user
        return true
    }

    fun register(name: String, email: String, password: String, isProvider: Boolean): Boolean {
        if (accounts.any { it.user.email.equals(email.trim(), ignoreCase = true) }) return false
        val user = User("u${accounts.size + 1}", name.trim(), email.trim(), isProvider)
        accounts.add(Account(user, password))
        currentUser = user
        return true
    }

    fun logout() {
        currentUser = null
    }

    fun getCurrentUser(): User? = currentUser

    fun isLoggedIn(): Boolean = currentUser != null
}
