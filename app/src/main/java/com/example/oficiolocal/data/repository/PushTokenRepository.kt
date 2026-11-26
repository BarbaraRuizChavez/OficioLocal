package com.example.oficiolocal.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.security.MessageDigest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/** Registra los tokens FCM por usuario para que el backend pueda enviarle avisos. */
object PushTokenRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun syncCurrentUserToken() {
        val userId = auth.currentUser?.uid ?: return
        withTimeout(8_000) {
            registerToken(userId, FirebaseMessaging.getInstance().token.await())
        }
    }

    suspend fun registerToken(userId: String, token: String) {
        val tokenId = token.sha256()
        firestore.collection("users")
            .document(userId)
            .collection("fcmTokens")
            .document(tokenId)
            .set(
                mapOf(
                    "token" to token,
                    "platform" to "android",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    fun onTokenRefresh(token: String) {
        val userId = auth.currentUser?.uid ?: return
        ioScope.launch { runCatching { registerToken(userId, token) } }
    }

    suspend fun removeCurrentUserToken() {
        val userId = auth.currentUser?.uid ?: return
        runCatching {
            withTimeout(4_000) {
                val token = FirebaseMessaging.getInstance().token.await()
                firestore.collection("users")
                    .document(userId)
                    .collection("fcmTokens")
                    .document(token.sha256())
                    .delete()
                    .await()
            }
        }
    }

    private fun String.sha256(): String = MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
        .joinToString("") { byte -> "%02x".format(byte) }
}
