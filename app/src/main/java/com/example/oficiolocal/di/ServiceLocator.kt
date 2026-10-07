package com.example.oficiolocal.di

import com.example.oficiolocal.data.repository.AuthRepository
import com.example.oficiolocal.data.repository.FakeRequestRepository
import com.example.oficiolocal.data.repository.FavoriteRepository
import com.example.oficiolocal.data.repository.ProviderRepository
import com.example.oficiolocal.data.repository.RequestRepository
import com.example.oficiolocal.data.repository.ReviewRepository
import com.example.oficiolocal.data.repository.SettingsRepository

object ServiceLocator {
    val authRepository by lazy { AuthRepository() }
    val favoriteRepository by lazy { FavoriteRepository() }
    val providerRepository by lazy { ProviderRepository() }
    val reviewRepository by lazy { ReviewRepository() }
    val settingsRepository by lazy { SettingsRepository() }
    val requestRepository: RequestRepository by lazy { FakeRequestRepository() }
}
