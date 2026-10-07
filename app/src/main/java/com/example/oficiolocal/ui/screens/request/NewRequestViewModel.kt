package com.example.oficiolocal.ui.screens.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.domain.JobType
import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.domain.ServiceRequest
import com.example.oficiolocal.domain.Urgency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class NewRequestUiState(
    val providerName: String? = null,
    val jobType: JobType = JobType.PLUMBING,
    val description: String = "",
    val location: String = "",
    val urgency: Urgency = Urgency.MEDIUM,
    val needsMaterials: Boolean = false,
    val dateMillis: Long? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val descriptionError: Boolean = false,
    val locationError: Boolean = false,
    val dateTimeError: Boolean = false,
    val sentId: String? = null
)

class NewRequestViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    // Viene de la ruta; es nulo si la solicitud no se hizo desde el detalle de un prestador.
    private val providerId: String? = savedStateHandle.get<String>("providerId")
    private val provider = providerId?.let { ServiceLocator.providerRepository.getProviderById(it) }

    private val initialJobType = provider?.category ?: JobType.PLUMBING

    private val _state = MutableStateFlow(
        NewRequestUiState(providerName = provider?.name, jobType = initialJobType)
    )
    val state: StateFlow<NewRequestUiState> = _state.asStateFlow()

    fun onJobTypeChange(value: JobType) = _state.update { it.copy(jobType = value) }

    fun onDescriptionChange(value: String) =
        _state.update { it.copy(description = value, descriptionError = false) }

    fun onLocationChange(value: String) =
        _state.update { it.copy(location = value, locationError = false) }

    fun onUrgencyChange(value: Urgency) = _state.update { it.copy(urgency = value) }

    fun onNeedsMaterialsChange(value: Boolean) = _state.update { it.copy(needsMaterials = value) }

    fun onDateSelected(millis: Long) =
        _state.update { it.copy(dateMillis = millis, dateTimeError = false) }

    fun onTimeSelected(hour: Int, minute: Int) =
        _state.update { it.copy(hour = hour, minute = minute, dateTimeError = false) }

    fun onSentShown() = _state.update { it.copy(sentId = null) }

    fun submit() {
        val s = _state.value
        val descriptionError = s.description.isBlank()
        val locationError = s.location.isBlank()
        val dateTimeError = s.dateMillis == null || s.hour == null || s.minute == null

        if (descriptionError || locationError || dateTimeError) {
            _state.update {
                it.copy(
                    descriptionError = descriptionError,
                    locationError = locationError,
                    dateTimeError = dateTimeError
                )
            }
            return
        }

        val id = "SOL-" + UUID.randomUUID().toString().take(6).uppercase()
        viewModelScope.launch {
            ServiceLocator.requestRepository.add(
                ServiceRequest(
                    id = id,
                    providerId = providerId,
                    providerName = s.providerName,
                    jobType = s.jobType,
                    description = s.description.trim(),
                    location = s.location.trim(),
                    urgency = s.urgency,
                    needsMaterials = s.needsMaterials,
                    status = RequestStatus.SENT,
                    dateMillis = s.dateMillis,
                    hour = s.hour,
                    minute = s.minute
                )
            )
            // Limpia el formulario y avisa que se envió.
            _state.value = NewRequestUiState(
                providerName = s.providerName,
                jobType = initialJobType,
                sentId = id
            )
        }
    }
}