package com.example.oficiolocal.di

import android.content.Context
import com.example.oficiolocal.data.local.AppDatabase
import com.example.oficiolocal.data.repository.AuthRepository
import com.example.oficiolocal.data.repository.FavoriteRepository
import com.example.oficiolocal.data.repository.MediaAttachmentRepository
import com.example.oficiolocal.data.repository.NotificationRepository
import com.example.oficiolocal.data.repository.PhotoStorage
import com.example.oficiolocal.data.repository.ProviderRepository
import com.example.oficiolocal.data.repository.PushTokenRepository
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
    private lateinit var database: AppDatabase
    private lateinit var applicationContext: Context
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(context: Context) {
        applicationContext = context.applicationContext
        database = AppDatabase.getInstance(context)
        appScope.launch {
            authRepository.seedIfEmpty()
            reviewRepository.seedIfEmpty()
            // Sincronización con la API: si no hay internet, simplemente no hace nada.
            runCatching { providerRepository.refreshFromApi() }
            runCatching { providerRepository.fixDemoPhones() }
            runCatching { reviewRepository.syncPending() }
            runCatching { reviewRepository.refreshFromApi() }
            runCatching { mediaAttachmentRepository.normalizeLocalOnlyAttachments() }
            runCatching { PushTokenRepository.syncCurrentUserToken() }
        }
    }

    private val remoteRepository by lazy { RemoteRepository() }

    private val photoStorage by lazy { PhotoStorage(applicationContext) }

    val authRepository by lazy { AuthRepository(database.userDao(), applicationContext, photoStorage) }
    val favoriteRepository by lazy { FavoriteRepository(database.favoriteDao(), authRepository) }
    val providerRepository by lazy {
        ProviderRepository(database.providerDao(), remoteRepository, authRepository, photoStorage)
    }
    val reviewRepository by lazy {
        ReviewRepository(database.reviewDao(), remoteRepository, authRepository)
    }
    val settingsRepository by lazy { SettingsRepository(applicationContext) }
    val notificationRepository by lazy {
        NotificationRepository(database.notificationDao(), applicationContext, settingsRepository)
    }
    val mediaAttachmentRepository by lazy {
        MediaAttachmentRepository(database.mediaAttachmentDao(), applicationContext)
    }

    private val roomRequestRepository by lazy {
        RoomRequestRepository(
            database.serviceRequestDao(),
            remoteRepository,
            appScope,
            notificationRepository,
            authRepository,
            providerRepository,
            mediaAttachmentRepository
        )
    }
    val requestRepository: RequestRepository get() = roomRequestRepository
}
