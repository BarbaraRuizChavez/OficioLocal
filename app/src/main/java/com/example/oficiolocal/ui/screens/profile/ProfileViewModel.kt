package com.example.oficiolocal.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.domain.User
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {
    private val authRepository = ServiceLocator.authRepository
    private val settings = ServiceLocator.settingsRepository

    val user: User? get() = authRepository.getCurrentUser()

    val darkMode: StateFlow<Boolean> = settings.darkMode
    val notifications: StateFlow<Boolean> = settings.notifications
    val offline: StateFlow<Boolean> = settings.offline

    fun onDarkMode(value: Boolean) = settings.toggleDarkMode(value)
    fun onNotifications(value: Boolean) = settings.toggleNotifications(value)
    fun onOffline(value: Boolean) = settings.setOffline(value)

    fun logout() = authRepository.logout()
}
