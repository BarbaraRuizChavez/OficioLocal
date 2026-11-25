package com.example.oficiolocal.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context? = null) {
    private val prefs = context?.getSharedPreferences("oficiolocal_settings", Context.MODE_PRIVATE)

    private val _darkMode = MutableStateFlow(prefs?.getBoolean(KEY_DARK, false) ?: false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _notifications = MutableStateFlow(prefs?.getBoolean(KEY_NOTIF, true) ?: true)
    val notifications: StateFlow<Boolean> = _notifications.asStateFlow()

    private val _offline = MutableStateFlow(false)
    val offline: StateFlow<Boolean> = _offline.asStateFlow()

    val isDarkModeEnabled: Boolean get() = _darkMode.value
    val notificationsEnabled: Boolean get() = _notifications.value

    fun toggleDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
        prefs?.edit()?.putBoolean(KEY_DARK, enabled)?.apply()
    }

    fun toggleNotifications(enabled: Boolean) {
        _notifications.value = enabled
        prefs?.edit()?.putBoolean(KEY_NOTIF, enabled)?.apply()
    }

    fun setOffline(enabled: Boolean) { _offline.value = enabled }

    private companion object {
        const val KEY_DARK = "dark_mode"
        const val KEY_NOTIF = "notifications"
    }
}
