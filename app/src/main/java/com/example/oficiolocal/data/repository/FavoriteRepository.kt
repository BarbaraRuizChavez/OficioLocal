package com.example.oficiolocal.data.repository

import com.example.oficiolocal.data.local.dao.FavoriteDao
import com.example.oficiolocal.data.local.entity.FavoriteEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/** Favoritos por usuario, guardados en Room. */
class FavoriteRepository(
    private val dao: FavoriteDao,
    private val auth: AuthRepository
) {
    private fun clientId(): String = auth.getCurrentUser()?.id ?: GUEST

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeFavoriteIds(): Flow<Set<String>> =
        auth.currentUser.flatMapLatest { user ->
            dao.observeIds(user?.id ?: GUEST).map { it.toSet() }
        }

    fun observeIsFavorite(providerId: String): Flow<Boolean> =
        observeFavoriteIds().map { providerId in it }

    suspend fun isFavorite(providerId: String): Boolean =
        dao.exists(clientId(), providerId) > 0

    /** Aquì verifiqcamos si devuelve true si quedó como favorito y false si se quitó. */
    suspend fun toggleFavorite(providerId: String): Boolean {
        val client = clientId()
        return if (dao.exists(client, providerId) > 0) {
            dao.delete(client, providerId)
            false
        } else {
            dao.insert(FavoriteEntity(client, providerId, System.currentTimeMillis(), true))
            true
        }
    }

    private companion object {
        const val GUEST = "guest"
    }
}