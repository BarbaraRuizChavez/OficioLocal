package com.example.oficiolocal.ui.screens.notifications


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oficiolocal.data.local.entity.NotificationEntity
import com.example.oficiolocal.di.ServiceLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val repository = ServiceLocator.notificationRepository
    val notifications: StateFlow<List<NotificationEntity>> =
        repository.all.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markAllRead() { viewModelScope.launch { repository.markAllRead() } }
    fun clear() { viewModelScope.launch { repository.clear() } }
}
