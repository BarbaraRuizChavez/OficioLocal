package com.example.oficiolocal.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.domain.Review
import kotlinx.coroutines.launch

class ReviewsViewModel(private val providerId: String) : ViewModel() {
    private val reviewRepository = ServiceLocator.reviewRepository
    private val authRepository = ServiceLocator.authRepository

    var reviews by mutableStateOf<List<Review>>(emptyList())
        private set

    var rating by mutableDoubleStateOf(0.0)
        private set

    var comment by mutableStateOf("")
        private set

    var editingReviewId by mutableStateOf<String?>(null)
        private set

    init {
        // La lista se actualiza sola cuando cambia la base de datos.
        viewModelScope.launch {
            reviewRepository.observeByProvider(providerId).collect { reviews = it }
        }
    }

    fun onRating(value: Int) { rating = value.toDouble() }
    fun onComment(value: String) { comment = value }

    fun canManage(review: Review): Boolean =
        authRepository.getCurrentUser()?.name == review.userName

    fun startEditing(review: Review) {
        if (!canManage(review)) return
        editingReviewId = review.id
        rating = review.rating
        comment = review.comment
    }

    fun cancelEditing() {
        editingReviewId = null
        rating = 0.0
        comment = ""
    }

    fun submit() {
        if (rating <= 0.0) return
        val author = authRepository.getCurrentUser()?.name ?: return
        val value = rating
        val text = comment.trim()
        val reviewId = editingReviewId
        viewModelScope.launch {
            if (reviewId == null) {
                reviewRepository.addReview(providerId, author, value, text)
            } else {
                reviewRepository.updateReview(reviewId, author, value, text)
            }
            cancelEditing()
        }
    }

    fun delete(review: Review) {
        val author = authRepository.getCurrentUser()?.name ?: return
        if (!canManage(review)) return
        viewModelScope.launch { reviewRepository.deleteReview(review.id, author) }
    }
}
