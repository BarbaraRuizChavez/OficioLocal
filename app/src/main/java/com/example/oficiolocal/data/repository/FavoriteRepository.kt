package com.example.oficiolocal.data.repository

class FavoriteRepository {
    private val favoriteProviderIds = mutableSetOf<String>()

    fun toggleFavorite(providerId: String): Boolean {
        return if (favoriteProviderIds.contains(providerId)) {
            favoriteProviderIds.remove(providerId)
            false
        } else {
            favoriteProviderIds.add(providerId)
            true
        }
    }

    fun isFavorite(providerId: String): Boolean {
        return favoriteProviderIds.contains(providerId)
    }

    fun getFavoriteIds(): Set<String> {
        return favoriteProviderIds.toSet()
    }
}