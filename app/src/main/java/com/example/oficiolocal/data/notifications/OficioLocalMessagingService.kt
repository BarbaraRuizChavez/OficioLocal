package com.example.oficiolocal.data.notifications

import com.example.oficiolocal.R
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.data.repository.PushTokenRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class OficioLocalMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        PushTokenRepository.onTokenRefresh(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.data[DATA_TITLE]
            ?: message.notification?.title
            ?: getString(R.string.notif_status_title)
        val body = message.data[DATA_BODY]
            ?: message.notification?.body
            ?: getString(R.string.notif_status_msg, message.data[DATA_STATUS].orEmpty())
        val requestId = message.data[DATA_REQUEST_ID]
        val notificationId = message.messageId ?: UUID.randomUUID().toString()

        serviceScope.launch {
            runCatching {
                ServiceLocator.notificationRepository.receivePush(
                    id = notificationId,
                    title = title,
                    message = body,
                    requestId = requestId
                )
            }
        }
    }

    private companion object {
        const val DATA_TITLE = "title"
        const val DATA_BODY = "body"
        const val DATA_REQUEST_ID = "requestId"
        const val DATA_STATUS = "status"
    }
}
