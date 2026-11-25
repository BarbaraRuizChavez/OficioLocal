package com.example.oficiolocal.di

import android.content.Context
import com.example.oficiolocal.data.local.AppDatabase
import com.example.oficiolocal.data.repository.AuthRepository
import com.example.oficiolocal.data.repository.FakeRequestRepository
import com.example.oficiolocal.data.repository.FavoriteRepository
import com.example.oficiolocal.data.repository.NotificationRepository
import com.example.oficiolocal.data.repository.ProviderRepository
import com.example.oficiolocal.data.repository.RemoteRepository
import com.example.oficiolocal.data.repository.RequestRepository
import com.example.oficiolocal.data.repository.ReviewRepository
import com.example.oficiolocal.data.repository.RoomRequestRepository
import com.example.oficiolocal.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object ServiceLocator {
    private lateinit var applicationContext: Context
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database by lazy { AppDatabase.getInstance(applicationContext) }
    private val remoteRepository by lazy { RemoteRepository() }

    val settingsRepository by lazy { SettingsRepository(applicationContext) }

    val authRepository by lazy { AuthRepository(database.userDao(), applicationContext) }

    val favoriteRepository by lazy { FavoriteRepository(database.favoriteDao(), authRepository) }
    val providerRepository by lazy { ProviderRepository(database.providerDao(), remoteRepository) }
    val reviewRepository by lazy { ReviewRepository(database.reviewDao(), remoteRepository) }

    val notificationRepository by lazy {
        NotificationRepository(database.notificationDao(), applicationContext, settingsRepository)
    }

    private val roomRequestRepository by lazy {
        RoomRequestRepository(database.serviceRequestDao(), remoteRepository, appScope, notificationRepository)
    }
    private val fakeRequestRepository by lazy { FakeRequestRepository() }

    val requestRepository: RequestRepository
        get() = if (settingsRepository.offline.value) fakeRequestRepository else roomRequestRepository

    fun init(context: Context) {
        applicationContext = context.applicationContext
        appScope.launch {
            runCatching { authRepository.seedIfEmpty() }
            runCatching { providerRepository.refreshFromApi() }
            runCatching { reviewRepository.refreshFromApi() }
            runCatching { roomRequestRepository.refreshFromApi() }
            runCatching { roomRequestRepository.syncPending() }
        }
    }
}
