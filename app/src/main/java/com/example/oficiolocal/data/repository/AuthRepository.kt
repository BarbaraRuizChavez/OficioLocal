package com.example.oficiolocal.data.repository

import android.content.Context
import com.example.oficiolocal.data.local.dao.UserDao
import com.example.oficiolocal.data.local.entity.UserEntity
import com.example.oficiolocal.data.local.entity.toDomain
import com.example.oficiolocal.domain.User
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/** Usuarios guardados en Room. La sesión vive en memoria. En la siguiente etapa se suma la API con JWT. */
class AuthRepository(
    private val userDao: UserDao,
    context: Context
) {
    private val seedPreferences = context.getSharedPreferences("oficiolocal_auth", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val seedMutex = Mutex()
    private val firebaseAuth get() = FirebaseAuth.getInstance()
    private val profiles get() = FirebaseFirestore.getInstance().collection("users")

    private fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }

    /** Cuentas de prueba la primera vez que se abre la app. */
    suspend fun seedIfEmpty() = seedMutex.withLock {
        if (seedPreferences.getBoolean(KEY_DEMO_USERS_SEEDED, false)) return@withLock
        if (userDao.count() == 0) {
            val now = System.currentTimeMillis()
            userDao.insert(UserEntity("u1", "Ana Torres", "cliente@correo.com", hash("123456"), false, now, false))
            userDao.insert(UserEntity("u2", "Juan Pérez", "prestador@correo.com", hash("123456"), true, now, false))
        }
        seedPreferences.edit().putBoolean(KEY_DEMO_USERS_SEEDED, true).apply()
    }

    suspend fun login(email: String, password: String): Boolean {
        seedIfEmpty()
        val cleanEmail = email.trim()

        // 1) Firebase Authentication
        val firebaseUser = runCatching {
            withTimeout(8_000) {
                firebaseAuth.signInWithEmailAndPassword(cleanEmail, password).await().user
            }
        }.getOrNull()

        if (firebaseUser != null) {
            var local = userDao.findByEmail(cleanEmail)
            if (local == null) {
                // Primera vez en este teléfono: se crea la copia local con los datos de la nube.
                val profile = runCatching {
                    withTimeout(8_000) { profiles.document(firebaseUser.uid).get().await() }
                }.getOrNull()
                local = UserEntity(
                    id = firebaseUser.uid,
                    name = profile?.getString("name") ?: cleanEmail.substringBefore("@"),
                    email = cleanEmail,
                    passwordHash = hash(password),
                    isProvider = profile?.getBoolean("isProvider") ?: false,
                    updatedAt = System.currentTimeMillis(),
                    pendingSync = false
                )
                userDao.insert(local)
            }
            _currentUser.value = local.toDomain()
            return true
        }

        // 2) Respaldo local: cuentas de demostración y uso sin internet
        val user = userDao.findByEmail(cleanEmail) ?: return false
        if (user.passwordHash != hash(password)) return false
        _currentUser.value = user.toDomain()
        return true
    }

    suspend fun register(name: String, email: String, password: String, isProvider: Boolean): Boolean {
        seedIfEmpty()
        val cleanEmail = email.trim()
        if (userDao.findByEmail(cleanEmail) != null) return false

        var uid: String? = null
        try {
            val created = withTimeout(8_000) {
                firebaseAuth.createUserWithEmailAndPassword(cleanEmail, password).await().user
            }
            uid = created?.uid
            if (uid != null) {
                runCatching {
                    withTimeout(8_000) {
                        profiles.document(uid).set(
                            mapOf("name" to name.trim(), "email" to cleanEmail, "isProvider" to isProvider)
                        ).await()
                    }
                }
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            return false // el correo ya existe en Firebase
        } catch (e: Exception) {
            // Sin internet u otro error: se registra solo en local (pendingSync = true)
        }

        val user = UserEntity(
            id = uid ?: UUID.randomUUID().toString(),
            name = name.trim(),
            email = cleanEmail,
            passwordHash = hash(password),
            isProvider = isProvider,
            updatedAt = System.currentTimeMillis(),
            pendingSync = uid == null
        )
        userDao.insert(user)
        _currentUser.value = user.toDomain()
        return true
    }

    suspend fun updateCurrentUserName(name: String): Boolean {
        val current = _currentUser.value ?: return false
        val cleanName = name.trim()
        if (cleanName.isBlank()) return false
        val user = userDao.findById(current.id) ?: return false
        val updated = user.copy(
            name = cleanName,
            updatedAt = System.currentTimeMillis(),
            pendingSync = true
        )
        userDao.update(updated)
        _currentUser.value = updated.toDomain()
        return true
    }

    suspend fun deleteCurrentAccount(): Boolean {
        val current = _currentUser.value ?: return false
        val deletedRows = userDao.deleteById(current.id)
        if (deletedRows == 0) return false
        runCatching { firebaseAuth.currentUser?.delete() }
        logout()
        return true
    }

    fun logout() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    fun getCurrentUser(): User? = _currentUser.value

    fun isLoggedIn(): Boolean = _currentUser.value != null

    private companion object {
        const val KEY_DEMO_USERS_SEEDED = "demo_users_seeded"
    }
}
