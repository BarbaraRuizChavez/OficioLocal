package com.example.oficiolocal.ui.screens.providerprofile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oficiolocal.R
import com.example.oficiolocal.data.repository.ProviderRepository
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
    var photoUrl by mutableStateOf(initial?.photoUrl)
        private set
    var gallery by mutableStateOf(initial?.gallery.orEmpty())
        private set
    var photoBusy by mutableStateOf(false)
        private set
    var photoMessage by mutableStateOf<Int?>(null)
        private set
    val canAddWorkPhotos: Boolean get() = gallery.size < ProviderRepository.MAX_WORK_PHOTOS
    val remainingWorkSlots: Int get() = ProviderRepository.MAX_WORK_PHOTOS - gallery.size

    var isSaving by mutableStateOf(false)
        private set
    var saveState by mutableStateOf<Boolean?>(null)
        private set

    private fun refreshPhotos() {
        val current = providerId?.let(repository::getProviderById)
        photoUrl = current?.photoUrl
        gallery = current?.gallery.orEmpty()
    }

    private fun runPhotoOperation(block: suspend () -> Result<Unit>) {
        if (photoBusy || !canSave) return
        photoBusy = true
        photoMessage = null
        viewModelScope.launch {
            val result = block()
            photoMessage = if (result.isFailure) R.string.photo_error else null
            refreshPhotos()
            photoBusy = false
        }
    }

    fun onAvatarPicked(uri: Uri?) {
        val id = providerId ?: return
        if (uri == null) return
        runPhotoOperation { repository.setAvatar(id, uri) }
    }

    fun onWorkPhotosPicked(uris: List<Uri>) {
        val id = providerId ?: return
        if (uris.isEmpty()) return
        if (!canAddWorkPhotos) {
            photoMessage = R.string.photo_limit
            return
        }
        val selected = uris.take(remainingWorkSlots)
        runPhotoOperation {
            var outcome: Result<Unit> = Result.success(Unit)
            for (uri in selected) {
                outcome = repository.addWorkPhoto(id, uri)
                if (outcome.isFailure) break
            }
            outcome
        }
    }

    fun removeWorkPhoto(url: String) {
        val id = providerId ?: return
        runPhotoOperation { repository.removeWorkPhoto(id, url) }
    }

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
                services = services.split(',').map { it.trim() }.filter { it.isNotBlank() },
                // Las fotos se leen al guardar para no pisar las que se subieron mientras tanto.
                photoUrl = repository.getProviderById(id)?.photoUrl,
                gallery = repository.getProviderById(id)?.gallery.orEmpty()
            )
            saveState = repository.save(updated)
            isSaving = false
        }
    }
}
