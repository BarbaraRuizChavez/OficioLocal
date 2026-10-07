package com.example.oficiolocal.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.oficiolocal.di.ServiceLocator

class LoginViewModel : ViewModel() {
    private val authRepository = ServiceLocator.authRepository

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newEmail: String) {
        email = newEmail
        errorMessage = null
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        errorMessage = null
    }

    fun login(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Por favor completa todos los campos"
            return
        }

        val success = authRepository.login(email, password)
        if (success) {
            onSuccess()
        } else {
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
        }
    }
}