package com.example.oficiolocal.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.oficiolocal.data.repository.FavoriteRepository
import com.example.oficiolocal.data.repository.ProviderRepository
import com.example.oficiolocal.domain.Provider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    // Se actualiza sola cuando cambian los favoritos en la base de datos.
    val favorites: StateFlow<List<Provider>> =
        favoriteRepository.observeFavoriteIds()
            .map { ids -> providerRepository.getProviders().filter { it.id in ids } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Se conserva para no cambiar la pantalla: ya no hace falta recargar a mano. */
    fun loadFavorites() = Unit

    fun toggleFavorite(providerId: String) {
        viewModelScope.launch { favoriteRepository.toggleFavorite(providerId) }
    }
}

class FavoritesViewModelFactory(
    private val favoriteRepository: FavoriteRepository,
    private val providerRepository: ProviderRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            return FavoritesViewModel(
                favoriteRepository = favoriteRepository,
                providerRepository = providerRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}