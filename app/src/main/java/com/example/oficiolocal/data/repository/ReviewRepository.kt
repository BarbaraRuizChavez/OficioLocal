package com.example.oficiolocal.data.repository

import com.example.oficiolocal.data.local.dao.ReviewDao
import com.example.oficiolocal.data.local.entity.ReviewEntity
import com.example.oficiolocal.data.local.entity.toDomain
import com.example.oficiolocal.domain.Review
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Room conserva las reseñas y la API aporta las reseñas compartidas del servidor. */
class ReviewRepository(
    private val dao: ReviewDao,
    private val remote: RemoteRepository
) {

    private val seedMutex = Mutex()

    suspend fun seedIfEmpty() = seedMutex.withLock {
        if (dao.count() == 0) {
            val now = System.currentTimeMillis()
            listOf(
                ReviewEntity("seed-1", "1", "María López", 5.0, "Excelente servicio, llegó a tiempo y resolvió la fuga.", "2026-02-10", now, false),
                ReviewEntity("seed-2", "1", "Juan Pérez", 4.5, "Muy profesional, recomendado.", "2026-02-15", now, false),
                ReviewEntity("seed-3", "2", "Carla Gómez", 5.0, "Hizo la instalación eléctrica súper rápido.", "2026-02-20", now, false)
            ).forEach { dao.insert(it) }
        }
    }

    fun observeByProvider(providerId: String): Flow<List<Review>> =
        dao.observeByProvider(providerId).map { list -> list.map { it.toDomain() } }

    suspend fun refreshFromApi(): Boolean {
        val remoteReviews = remote.getReviews().getOrNull() ?: return false
        val entities = remoteReviews.mapNotNull { review ->
            if (review.id.isBlank() || review.providerId.isBlank()) return@mapNotNull null
            val existing = dao.getByRemoteId(review.id) ?: dao.getById(review.id)
            if (existing?.pendingSync == true || existing?.pendingDelete == true) return@mapNotNull null
            ReviewEntity(
                id = existing?.id ?: "remote-${review.id}",
                providerId = review.providerId,
                userName = review.userName,
                rating = review.rating,
                comment = review.comment,
                date = review.date,
                updatedAt = System.currentTimeMillis(),
                pendingSync = false,
                remoteId = review.id
            )
        }
        dao.upsertAll(entities)
        return true
    }

    suspend fun addReview(providerId: String, userName: String, rating: Double, comment: String) {
        val id = UUID.randomUUID().toString()
        dao.insert(
            ReviewEntity(
                id = id,
                providerId = providerId,
                userName = userName,
                rating = rating,
                comment = comment,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )
        )
        syncReview(id)
    }

    suspend fun updateReview(id: String, userName: String, rating: Double, comment: String) {
        val existing = dao.getById(id) ?: return
        if (existing.userName != userName || existing.pendingDelete) return
        dao.update(
            existing.copy(
                rating = rating,
                comment = comment.trim(),
                date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )
        )
        syncReview(id)
    }

    suspend fun deleteReview(id: String, userName: String) {
        val existing = dao.getById(id) ?: return
        if (existing.userName != userName) return
        val remoteId = existing.remoteId
        if (remoteId == null) {
            dao.delete(id)
        } else if (remote.deleteReview(remoteId).isSuccess) {
            dao.delete(id)
        } else {
            dao.markDeletePending(id)
        }
    }

    suspend fun syncPending() {
        dao.getPendingSync().forEach { review ->
            if (review.pendingDelete) {
                val remoteId = review.remoteId
                if (remoteId == null || remote.deleteReview(remoteId).isSuccess) {
                    dao.delete(review.id)
                }
            } else {
                syncReview(review.id)
            }
        }
    }

    private suspend fun syncReview(id: String) {
        val review = dao.getById(id) ?: return
        if (!review.pendingSync || review.pendingDelete) return
        val remoteId = review.remoteId
        val result = if (remoteId == null) {
            remote.createReview(review.toDomain())
        } else {
            remote.updateReview(remoteId, review.toDomain())
        }
        result.onSuccess { saved ->
            val savedRemoteId = saved.id.takeIf { it.isNotBlank() } ?: remoteId
            if (savedRemoteId != null) dao.markSynced(id, savedRemoteId)
        }
    }
}
