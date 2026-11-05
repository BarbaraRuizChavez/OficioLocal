package com.example.oficiolocal.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.domain.User
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val authRepository = ServiceLocator.authRepository
    private val favoriteRepository = ServiceLocator.favoriteRepository
    private val settings = ServiceLocator.settingsRepository

    val user: StateFlow<User?> = authRepository.currentUser

    val darkMode: StateFlow<Boolean> = settings.darkMode
    val notifications: StateFlow<Boolean> = settings.notifications
    val offline: StateFlow<Boolean> = settings.offline

    fun onDarkMode(value: Boolean) = settings.toggleDarkMode(value)
    fun onNotifications(value: Boolean) = settings.toggleNotifications(value)
    fun onOffline(value: Boolean) = settings.setOffline(value)

    fun updateName(name: String) {
        viewModelScope.launch { authRepository.updateCurrentUserName(name) }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        val accountId = user.value?.id ?: return
        viewModelScope.launch {
            if (authRepository.deleteCurrentAccount()) {
                runCatching { favoriteRepository.removeAllForClient(accountId) }
                onDeleted()
            }
        }
    }

    fun logout() = authRepository.logout()
}
