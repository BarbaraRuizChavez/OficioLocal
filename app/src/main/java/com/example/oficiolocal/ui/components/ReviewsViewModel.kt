package com.example.oficiolocal.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

    var rating by mutableIntStateOf(0)
        private set

    var comment by mutableStateOf("")
        private set

    init {
        // La lista se actualiza sola cuando cambia la base de datos.
        viewModelScope.launch {
            reviewRepository.observeByProvider(providerId).collect { reviews = it }
        }
    }

    fun onRating(value: Int) { rating = value }
    fun onComment(value: String) { comment = value }

    fun submit() {
        if (rating == 0) return
        val author = authRepository.getCurrentUser()?.name ?: "Usuario"
        val value = rating.toDouble()
        val text = comment.trim()
        rating = 0
        comment = ""
        viewModelScope.launch {
            reviewRepository.addReview(providerId, author, value, text)
        }
    }
}