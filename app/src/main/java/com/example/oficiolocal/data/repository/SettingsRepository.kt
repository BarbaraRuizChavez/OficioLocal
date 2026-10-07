package com.example.oficiolocal.data.repository

class SettingsRepository {
    var isDarkModeEnabled: Boolean = false
        private set

    var notificationsEnabled: Boolean = true
        private set

    fun toggleDarkMode(enabled: Boolean) {
        isDarkModeEnabled = enabled
    }

    fun toggleNotifications(enabled: Boolean) {
        notificationsEnabled = enabled
    }
}