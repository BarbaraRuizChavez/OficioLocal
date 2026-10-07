package com.example.oficiolocal.data.repository

import com.example.oficiolocal.domain.Review

class ReviewRepository {
    private val reviews = mutableListOf(
        Review("1", "1", "María López", 5.0, "Excelente servicio, llegó a tiempo y resolvió la fuga.", "2026-02-10"),
        Review("2", "1", "Juan Pérez", 4.5, "Muy profesional, recomendado.", "2026-02-15"),
        Review("3", "2", "Carla Gómez", 5.0, "Hizo la instalación eléctrica súper rápido.", "2026-02-20")
    )

    fun getReviewsByProvider(providerId: String): List<Review> {
        return reviews.filter { it.providerId == providerId }
    }

    fun addReview(providerId: String, userName: String, rating: Double, comment: String): Review {
        val newReview = Review(
            id = (reviews.size + 1).toString(),
            providerId = providerId,
            userName = userName,
            rating = rating,
            comment = comment,
            date = "2026-03-01"
        )
        reviews.add(newReview)
        return newReview
    }
}