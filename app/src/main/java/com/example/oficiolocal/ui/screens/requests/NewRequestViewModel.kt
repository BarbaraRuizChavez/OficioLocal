package com.example.oficiolocal.ui.screens.requests

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oficiolocal.R
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.domain.ServiceRequest
import com.example.oficiolocal.domain.Urgency
import java.util.UUID
import kotlinx.coroutines.launch

class NewRequestViewModel : ViewModel() {
    private val repository = ServiceLocator.requestRepository

    var description by mutableStateOf("")
        private set

    var urgency by mutableStateOf(Urgency.MEDIUM)
        private set

    var needsMaterials by mutableStateOf(false)
        private set

    var dateMillis by mutableStateOf<Long?>(null)
        private set

    var hour by mutableStateOf<Int?>(null)
        private set

    var minute by mutableStateOf<Int?>(null)
        private set

    /** Id del texto de error (recurso), o null si no hay error. */
    var errorRes by mutableStateOf<Int?>(null)
        private set

    fun onDescription(value: String) { description = value; errorRes = null }
    fun onUrgency(value: Urgency) { urgency = value }
    fun onNeedsMaterials(value: Boolean) { needsMaterials = value }
    fun onDate(value: Long?) { dateMillis = value; errorRes = null }
    fun onTime(h: Int, m: Int) { hour = h; minute = m; errorRes = null }

    fun submit(onDone: () -> Unit) {
        errorRes = when {
            description.isBlank() -> R.string.field_required
            dateMillis == null || hour == null -> R.string.req_error_datetime
            else -> null
        }
        if (errorRes != null) return

        viewModelScope.launch {
            repository.add(
                ServiceRequest(
                    id = UUID.randomUUID().toString(),
                    providerName = "",
                    description = description.trim(),
                    urgency = urgency,
                    status = RequestStatus.PENDING,
                    dateMillis = dateMillis,
                    hour = hour,
                    minute = minute,
                    needsMaterials = needsMaterials
                )
            )
            onDone()
        }
    }
}
