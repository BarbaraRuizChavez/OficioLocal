package com.example.oficiolocal.di

import com.example.oficiolocal.data.repository.AuthRepository
import com.example.oficiolocal.data.repository.FakeRequestRepository
import com.example.oficiolocal.data.repository.FavoriteRepository
import com.example.oficiolocal.data.repository.ProviderRepository
import com.example.oficiolocal.data.repository.RequestRepository
import com.example.oficiolocal.data.repository.ReviewRepository
import com.example.oficiolocal.data.repository.SettingsRepository
import android.content.Context
import com.example.oficiolocal.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch



object ServiceLocator {
    private lateinit var database: AppDatabase
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(context: Context) {
        database = AppDatabase.getInstance(context)
        appScope.launch {
            authRepository.seedIfEmpty()
            reviewRepository.seedIfEmpty()
        }
    }
    val authRepository by lazy { AuthRepository(database.userDao()) }

    val favoriteRepository by lazy { FavoriteRepository(database.favoriteDao(), authRepository) }
    val providerRepository by lazy { ProviderRepository() }
    val reviewRepository by lazy { ReviewRepository(database.reviewDao()) }
    val settingsRepository by lazy { SettingsRepository() }
    val requestRepository: RequestRepository by lazy { FakeRequestRepository() }
    }
