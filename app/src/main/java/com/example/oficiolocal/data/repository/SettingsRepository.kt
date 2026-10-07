package com.example.oficiolocal.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Ajustes en memoria. En la siguiente etapa pasan a DataStore y al estado real de WorkManager. */
class SettingsRepository {

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _notifications = MutableStateFlow(true)
    val notifications: StateFlow<Boolean> = _notifications.asStateFlow()

    private val _offline = MutableStateFlow(false)
    val offline: StateFlow<Boolean> = _offline.asStateFlow()

    val isDarkModeEnabled: Boolean get() = _darkMode.value
    val notificationsEnabled: Boolean get() = _notifications.value

    fun toggleDarkMode(enabled: Boolean) { _darkMode.value = enabled }
    fun toggleNotifications(enabled: Boolean) { _notifications.value = enabled }
    fun setOffline(enabled: Boolean) { _offline.value = enabled }
}
