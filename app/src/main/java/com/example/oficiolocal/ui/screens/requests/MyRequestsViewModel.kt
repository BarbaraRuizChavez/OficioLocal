package com.example.oficiolocal.ui.screens.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.domain.ServiceRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyRequestsViewModel : ViewModel() {
    private val repository = ServiceLocator.requestRepository

    private val _selected = MutableStateFlow<RequestStatus?>(null)
    val selected: StateFlow<RequestStatus?> = _selected.asStateFlow()

    val requests: StateFlow<List<ServiceRequest>> =
        combine(repository.requests, _selected) { list, status ->
            if (status == null) list else list.filter { it.status == status }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onFilterSelected(status: RequestStatus?) {
        _selected.value = status
    }

    fun onCancel(id: String) {
        viewModelScope.launch { repository.cancel(id) }
    }
}
