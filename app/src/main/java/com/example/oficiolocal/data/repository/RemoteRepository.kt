package com.example.oficiolocal.data.repository

import com.example.oficiolocal.data.remote.ApiService
import com.example.oficiolocal.data.remote.toDomain
import com.example.oficiolocal.data.remote.toDto
import com.example.oficiolocal.domain.Provider
import com.example.oficiolocal.domain.Review
import com.example.oficiolocal.domain.ServiceRequest

/** Llama a la API. Devuelve Result para manejar errores como no tener internet. */
class RemoteRepository(private val api: ApiService) {

    suspend fun getProviders(): Result<List<Provider>> =
        runCatching { api.getProviders().map { it.toDomain() } }

    suspend fun getProvider(id: String): Result<Provider> =
        runCatching { api.getProvider(id).toDomain() }

    suspend fun getRequests(): Result<List<ServiceRequest>> =
        runCatching { api.getRequests().map { it.toDomain() } }

    suspend fun getReviews(): Result<List<Review>> =
        runCatching { api.getReviews().map { it.toDomain() } }

    suspend fun createReview(review: Review): Result<Review> =
        runCatching { api.createReview(review.toDto()).toDomain() }

    suspend fun updateReview(id: String, review: Review): Result<Review> =
        runCatching { api.updateReview(id, review.toDto(id)).toDomain() }

    suspend fun deleteReview(id: String): Result<Unit> =
        runCatching { api.deleteReview(id) }

    suspend fun createRequest(request: ServiceRequest): Result<ServiceRequest> =
        runCatching { api.createRequest(request.toDto()).toDomain() }

    suspend fun updateRequest(request: ServiceRequest): Result<ServiceRequest> =
        runCatching { api.updateRequest(request.id, request.toDto()).toDomain() }

    suspend fun deleteRequest(id: String): Result<Unit> =
        runCatching { api.deleteRequest(id) }
}
