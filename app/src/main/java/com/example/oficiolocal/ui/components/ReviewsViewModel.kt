package com.example.oficiolocal.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.domain.Review

class ReviewsViewModel(private val providerId: String) : ViewModel() {
    private val reviewRepository = ServiceLocator.reviewRepository
    private val authRepository = ServiceLocator.authRepository

    var reviews by mutableStateOf<List<Review>>(reviewRepository.getReviewsByProvider(providerId))
        private set

    var rating by mutableIntStateOf(0)
        private set

    var comment by mutableStateOf("")
        private set

    fun onRating(value: Int) { rating = value }
    fun onComment(value: String) { comment = value }

    fun submit() {
        if (rating == 0) return
        val author = authRepository.getCurrentUser()?.name ?: "Usuario"
        reviewRepository.addReview(providerId, author, rating.toDouble(), comment.trim())
        reviews = reviewRepository.getReviewsByProvider(providerId)
        rating = 0
        comment = ""
    }
}
