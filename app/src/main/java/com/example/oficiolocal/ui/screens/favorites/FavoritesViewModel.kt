package com.example.oficiolocal.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.oficiolocal.data.repository.FavoriteRepository
import com.example.oficiolocal.data.repository.ProviderRepository
import com.example.oficiolocal.domain.Provider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<Provider>>(emptyList())
    val favorites: StateFlow<List<Provider>> = _favorites.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        val favoriteIds = favoriteRepository.getFavoriteIds()

        _favorites.value = providerRepository
            .getProviders()
            .filter { provider ->
                provider.id in favoriteIds
            }
    }

    fun toggleFavorite(providerId: String) {
        favoriteRepository.toggleFavorite(providerId)
        loadFavorites()
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