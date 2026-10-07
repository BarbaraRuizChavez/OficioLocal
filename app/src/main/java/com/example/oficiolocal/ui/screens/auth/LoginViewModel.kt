package com.example.oficiolocal.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.oficiolocal.R
import com.example.oficiolocal.di.ServiceLocator

class LoginViewModel : ViewModel() {
    private val authRepository = ServiceLocator.authRepository

    var isRegister by mutableStateOf(false)
        private set

    var name by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isProvider by mutableStateOf(false)
        private set

    /** Id del texto de error (recurso), o null si no hay error. */
    var errorRes by mutableStateOf<Int?>(null)
        private set

    fun onNameChange(value: String) { name = value; errorRes = null }
    fun onEmailChange(value: String) { email = value; errorRes = null }
    fun onPasswordChange(value: String) { password = value; errorRes = null }
    fun onRoleChange(asProvider: Boolean) { isProvider = asProvider }

    fun toggleMode() {
        isRegister = !isRegister
        errorRes = null
    }

    fun submit(onSuccess: () -> Unit) {
        errorRes = when {
            isRegister && name.isBlank() -> R.string.error_name
            !email.contains("@") -> R.string.error_email
            password.length < 6 -> R.string.error_password
            else -> null
        }
        if (errorRes != null) return

        val ok = if (isRegister) {
            authRepository.register(name, email, password, isProvider)
        } else {
            authRepository.login(email, password)
        }

        if (ok) {
            onSuccess()
        } else {
            errorRes = if (isRegister) R.string.error_email_taken else R.string.error_credentials
        }
    }
}
