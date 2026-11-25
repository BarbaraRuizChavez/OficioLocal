package com.example.oficiolocal.data.repository

import com.example.oficiolocal.domain.JobType
import com.example.oficiolocal.domain.Provider
import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.domain.Review
import com.example.oficiolocal.domain.ServiceRequest
import com.example.oficiolocal.domain.Urgency
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * Origen remoto con Cloud Firestore. Devuelve Result para manejar errores (sin internet, permisos).
 * Cada operación tiene un tiempo máximo: sin red, Firestore deja la escritura en cola y no responde.
 * Los ids de solicitudes y reseñas son los ids locales de Room, así un reintento no crea duplicados.
 */
class RemoteRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val providers get() = db.collection("providers")
    private val requests get() = db.collection("requests")
    private val reviews get() = db.collection("reviews")

    private suspend fun <T> online(block: suspend () -> T): Result<T> =
        runCatching { withTimeout(TIMEOUT_MS) { block() } }

    // ---------- Prestadores ----------
    suspend fun getProviders(): Result<List<Provider>> = online {
        providers.get().await().documents.map { it.toProvider() }
    }

    suspend fun getProvider(id: String): Result<Provider> = online {
        providers.document(id).get().await().toProvider()
    }

    suspend fun upsertProvider(provider: Provider): Result<Unit> = online {
        providers.document(provider.id).set(provider.toMap()).await()
        Unit
    }

    // ---------- Solicitudes ----------
    suspend fun getRequests(): Result<List<ServiceRequest>> = online {
        requests.get().await().documents.map { it.toRequest() }
    }

    suspend fun createRequest(request: ServiceRequest): Result<ServiceRequest> = online {
        requests.document(request.id).set(request.toMap()).await()
        request
    }

    suspend fun updateRequest(request: ServiceRequest): Result<ServiceRequest> = online {
        requests.document(request.id).set(request.toMap()).await()
        request
    }

    suspend fun deleteRequest(id: String): Result<Unit> = online {
        requests.document(id).delete().await()
        Unit
    }

    // ---------- Reseñas ----------
    suspend fun getReviews(): Result<List<Review>> = online {
        reviews.get().await().documents.map { it.toReview() }
    }

    suspend fun createReview(review: Review): Result<Review> = online {
        reviews.document(review.id).set(review.toMap()).await()
        review
    }

    suspend fun updateReview(id: String, review: Review): Result<Review> = online {
        reviews.document(id).set(review.copy(id = id).toMap()).await()
        review.copy(id = id)
    }

    suspend fun deleteReview(id: String): Result<Unit> = online {
        reviews.document(id).delete().await()
        Unit
    }

    private companion object {
        const val TIMEOUT_MS = 8_000L
    }
}

// ---------- Conversión Firestore <-> dominio ----------
private fun Provider.toMap() = mapOf(
    "name" to name, "category" to category, "location" to location,
    "rating" to rating, "reviewCount" to reviewCount, "priceRange" to priceRange,
    "schedule" to schedule, "phone" to phone, "description" to description,
    "services" to services
)

private fun DocumentSnapshot.toProvider() = Provider(
    id = id,
    name = getString("name").orEmpty(),
    category = getString("category").orEmpty(),
    location = getString("location").orEmpty(),
    rating = getDouble("rating") ?: 0.0,
    reviewCount = (getLong("reviewCount") ?: 0L).toInt(),
    priceRange = getString("priceRange").orEmpty(),
    schedule = getString("schedule").orEmpty(),
    phone = getString("phone").orEmpty(),
    description = getString("description").orEmpty(),
    services = (get("services") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
)

private fun ServiceRequest.toMap() = mapOf(
    "providerId" to providerId, "providerName" to providerName, "jobType" to jobType?.name,
    "description" to description, "location" to location, "urgency" to urgency.name,
    "needsMaterials" to needsMaterials, "status" to status.name,
    "dateMillis" to dateMillis, "hour" to hour, "minute" to minute
)

private fun DocumentSnapshot.toRequest() = ServiceRequest(
    id = id,
    providerName = getString("providerName").orEmpty(),
    description = getString("description").orEmpty(),
    urgency = runCatching { Urgency.valueOf(getString("urgency").orEmpty()) }.getOrDefault(Urgency.MEDIUM),
    status = runCatching { RequestStatus.valueOf(getString("status").orEmpty()) }.getOrDefault(RequestStatus.PENDING),
    dateMillis = getLong("dateMillis"),
    hour = getLong("hour")?.toInt(),
    minute = getLong("minute")?.toInt(),
    needsMaterials = getBoolean("needsMaterials") ?: false,
    providerId = getString("providerId"),
    jobType = getString("jobType")?.let { runCatching { JobType.valueOf(it) }.getOrNull() },
    location = getString("location").orEmpty()
)

private fun Review.toMap() = mapOf(
    "providerId" to providerId, "userName" to userName, "rating" to rating,
    "comment" to comment, "date" to date
)

private fun DocumentSnapshot.toReview() = Review(
    id = id,
    providerId = getString("providerId").orEmpty(),
    userName = getString("userName").orEmpty(),
    rating = getDouble("rating") ?: 0.0,
    comment = getString("comment").orEmpty(),
    date = getString("date").orEmpty()
)