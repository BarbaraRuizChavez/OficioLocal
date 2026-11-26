package com.example.oficiolocal.data.repository

import android.content.Context
import com.example.oficiolocal.R
import com.example.oficiolocal.data.local.dao.NotificationDao
import com.example.oficiolocal.data.local.entity.NotificationEntity
import com.example.oficiolocal.data.notifications.NotificationHelper
import com.example.oficiolocal.domain.RequestStatus
import java.util.UUID

/** Guarda los avisos en Room y, si el usuario los permite, también los muestra en el teléfono. */
class NotificationRepository(
    private val dao: NotificationDao,
    private val context: Context,
    private val settings: SettingsRepository
) {
    val all = dao.observeAll()
    val unreadCount = dao.observeUnreadCount()

    suspend fun requestCreated(requestId: String) = notify(
        context.getString(R.string.notif_created_title),
        context.getString(R.string.notif_created_msg),
        requestId
    )

    suspend fun requestStatusChanged(requestId: String, status: RequestStatus) {
        val label = context.getString(
            when (status) {
                RequestStatus.PENDING -> R.string.req_status_pending
                RequestStatus.ACCEPTED -> R.string.req_status_accepted
                RequestStatus.COMPLETED -> R.string.req_status_completed
                RequestStatus.CANCELLED -> R.string.req_status_cancelled
            }
        )
        notify(
            context.getString(R.string.notif_status_title),
            context.getString(R.string.notif_status_msg, label),
            requestId
        )
    }

    suspend fun receivePush(id: String, title: String, message: String, requestId: String?) {
        dao.upsert(
            NotificationEntity(
                id = id,
                title = title,
                message = message,
                category = "REQUEST",
                relatedRequestId = requestId,
                pendingSync = false
            )
        )
        if (settings.notificationsEnabled) {
            NotificationHelper.show(context, id.hashCode(), title, message)
        }
    }

    suspend fun markAllRead() = dao.markAllRead()
    suspend fun clear() = dao.deleteAll()

    private suspend fun notify(title: String, message: String, requestId: String?) {
        val id = UUID.randomUUID().toString()
        dao.upsert(
            NotificationEntity(
                id = id, title = title, message = message,
                category = "REQUEST", relatedRequestId = requestId
            )
        )
        if (settings.notificationsEnabled) {
            NotificationHelper.show(context, id.hashCode(), title, message)
        }
    }
}
