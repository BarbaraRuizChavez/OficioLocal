package com.example.oficiolocal.ui.screens.providerprofile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.domain.Provider
import kotlinx.coroutines.launch

class ProviderProfileViewModel : ViewModel() {
    private val repository = ServiceLocator.providerRepository
    private val user = ServiceLocator.authRepository.getCurrentUser()
    val canSave: Boolean get() = user?.isProvider == true
    private val providerId = user?.let(repository::providerIdFor)
    private val initial = providerId?.let(repository::getProviderById)

    var category by mutableStateOf(initial?.category ?: "Plomería")
    var location by mutableStateOf(initial?.location.orEmpty())
    var priceRange by mutableStateOf(initial?.priceRange.orEmpty())
    var schedule by mutableStateOf(initial?.schedule.orEmpty())
    var phone by mutableStateOf(initial?.phone.orEmpty())
    var description by mutableStateOf(initial?.description.orEmpty())
    var services by mutableStateOf(initial?.services.orEmpty().joinToString(", "))
    var isSaving by mutableStateOf(false)
        private set
    var saveState by mutableStateOf<Boolean?>(null)
        private set

    fun save() {
        val currentUser = user ?: return
        val id = providerId ?: return
        isSaving = true
        saveState = null

        viewModelScope.launch {
            val updated = Provider(
                id = id,
                name = initial?.name ?: currentUser.name,
                category = category.trim(),
                location = location.trim(),
                rating = initial?.rating ?: 0.0,
                reviewCount = initial?.reviewCount ?: 0,
                priceRange = priceRange.trim(),
                schedule = schedule.trim(),
                phone = phone.trim(),
                description = description.trim(),
                services = services.split(',').map { it.trim() }.filter { it.isNotBlank() }
            )
            saveState = repository.save(updated)
            isSaving = false
        }
    }
}
