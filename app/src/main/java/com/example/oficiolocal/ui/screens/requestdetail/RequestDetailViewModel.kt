package com.example.oficiolocal.ui.screens.requestdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.domain.ServiceRequest
import com.example.oficiolocal.data.local.entity.MediaAttachmentEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RequestDetailViewModel(private val requestId: String) : ViewModel() {
    private val repository = ServiceLocator.requestRepository

    init {
        viewModelScope.launch {
            runCatching {
                ServiceLocator.mediaAttachmentRepository.refreshFromCloud(requestId)
            }
        }
    }

    val request: StateFlow<ServiceRequest?> = repository.requests
        .map { requests -> requests.find { it.id == requestId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val attachments: StateFlow<List<MediaAttachmentEntity>> =
        ServiceLocator.mediaAttachmentRepository.observeForRequest(requestId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isProvider: Boolean
        get() = ServiceLocator.authRepository.getCurrentUser()?.isProvider == true

    fun accept() = setStatus(RequestStatus.ACCEPTED)
    fun reject() = setStatus(RequestStatus.CANCELLED)
    fun complete() = setStatus(RequestStatus.COMPLETED)

    fun cancel() {
        viewModelScope.launch { repository.cancel(requestId) }
    }

    private fun setStatus(status: RequestStatus) {
        viewModelScope.launch { repository.setStatus(requestId, status) }
    }
}
