package com.example.oficiolocal.data.repository

import android.net.Uri
import com.example.oficiolocal.data.local.dao.ProviderDao
import com.example.oficiolocal.data.local.entity.toDomain
import com.example.oficiolocal.data.local.entity.toEntity
import com.example.oficiolocal.domain.Provider
import com.example.oficiolocal.domain.User
import kotlinx.coroutines.runBlocking

/** Prestadores: se leen de Room y se actualizan desde la API. */
class ProviderRepository(
    private val dao: ProviderDao,
    private val remote: RemoteRepository,
    private val auth: AuthRepository,
    private val photos: PhotoStorage
) {

    companion object {
        const val MAX_WORK_PHOTOS = 8
    }

    // Datos de ejemplo: se usan solo si la base está vacía y aún no hay internet.
    private val seed = listOf(
        Provider(
            id = "1", name = "Carlos Mendoza", category = "Plomería", location = "Centro",
            rating = 4.8, reviewCount = 15, priceRange = "$200 - $500",
            schedule = "Lun - Sáb: 8:00 AM - 7:00 PM", phone = "4450000101",
            description = "Plomero certificado con más de 10 años de experiencia en reparación de fugas e instalación sanitaria.",
            services = listOf("Reparación de fugas", "Instalación de tanques", "Destape de cañerías")
        ),
        Provider(
            id = "2", name = "Ana Gómez", category = "Electricidad", location = "Norte",
            rating = 4.9, reviewCount = 22, priceRange = "$300 - $800",
            schedule = "Lun - Vie: 9:00 AM - 5:00 PM", phone = "4450000102",
            description = "Técnica electricista enfocada en instalaciones residenciales y mantenimiento.",
            services = listOf("Cableado residencial", "Solución de cortocircuitos", "Instalación de lámparas")
        ),
        Provider(
            id = "3", name = "Roberto Silva", category = "Carpintería", location = "Sur",
            rating = 4.6, reviewCount = 9, priceRange = "$400 - $1200",
            schedule = "Lun - Sáb: 9:00 AM - 6:00 PM", phone = "4450000103",
            description = "Fabricación y reparación de muebles a medida, puertas y closets.",
            services = listOf("Reparación de puertas", "Muebles a medida", "Lijado y barnizado")
        )
    )

    // Carga inicial desde Room. Son pocas filas y las pantallas piden los datos de forma síncrona.
    @Volatile
    private var cache: List<Provider> = runBlocking {
        if (dao.count() == 0) dao.insertAll(seed.map { it.toEntity() })
        dao.getAll().map { it.toDomain() }
    }

    fun getProviders(query: String = "", categoryFilter: String? = null): List<Provider> {
        return cache.filter { provider ->
            val matchesQuery = provider.name.contains(query, ignoreCase = true) ||
                    provider.category.contains(query, ignoreCase = true) ||
                    provider.location.contains(query, ignoreCase = true)
            val matchesCategory = categoryFilter == null ||
                    provider.category.equals(categoryFilter, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }

    fun getProviderById(id: String): Provider? = cache.find { it.id == id }

    fun providerIdFor(user: User): String = if (user.id == "u2") "1" else user.id

    /** Obtiene el perfil remoto si existe; si no, crea un perfil local inicial para el prestador. */
    suspend fun ensureProviderProfile(user: User) {
        val id = providerIdFor(user)
        val remoteProvider = remote.getProvider(id).getOrNull()
            ?.takeIf { it.name.isNotBlank() }
        if (remoteProvider != null) {
            persistLocal(withLocalPhotos(remoteProvider))
            return
        }

        if (cache.any { it.id == id }) return

        save(
            Provider(
                id = id,
                name = user.name,
                category = "Plomería",
                location = "",
                rating = 0.0,
                reviewCount = 0,
                priceRange = "",
                schedule = "",
                phone = "",
                description = "",
                services = emptyList(),
                ownerUid = auth.getFirebaseUserId()
            )
        )
    }

    /** Guarda primero en Room y luego intenta actualizar el perfil remoto. */
    suspend fun save(provider: Provider): Boolean {
        val ownerUid = auth.getFirebaseUserId()
        val ownedProvider = provider.copy(ownerUid = ownerUid)
        persistLocal(ownedProvider)
        // Cuentas demo o sesiones locales pueden editar Room, pero nunca escribir a Firestore.
        return ownerUid != null && remote.upsertProvider(ownedProvider).isSuccess
    }

    /** Guarda la foto de perfil del prestador en este teléfono. */
    suspend fun setAvatar(id: String, uri: Uri): Result<Unit> = runCatching {
        val current = getProviderById(id) ?: throw IllegalStateException("Perfil no encontrado")
        val saved = photos.save("providers/$id", uri)
        persistLocal(current.copy(photoUrl = saved))
        current.photoUrl?.let { photos.delete(it) }
    }

    /** Agrega una foto de trabajo a la galería local del prestador. */
    suspend fun addWorkPhoto(id: String, uri: Uri): Result<Unit> = runCatching {
        val current = getProviderById(id) ?: throw IllegalStateException("Perfil no encontrado")
        if (current.gallery.size >= MAX_WORK_PHOTOS) throw IllegalStateException("Límite de fotos")
        val saved = photos.save("providers/$id/work", uri)
        persistLocal(current.copy(gallery = current.gallery + saved))
    }

    /** Quita una foto de trabajo de la galería. */
    suspend fun removeWorkPhoto(id: String, url: String): Result<Unit> = runCatching {
        val current = getProviderById(id) ?: throw IllegalStateException("Perfil no encontrado")
        persistLocal(current.copy(gallery = current.gallery - url))
        photos.delete(url)
    }

    /** Las fotos solo existen en este teléfono: al traer datos de Firestore se conservan. */
    private fun withLocalPhotos(provider: Provider): Provider {
        val local = cache.find { it.id == provider.id } ?: return provider
        return provider.copy(photoUrl = local.photoUrl, gallery = local.gallery)
    }

    /** Actualiza el perfil desde Firestore; mantiene la copia local si no hay conexión. */
    suspend fun refreshOne(id: String): Provider? {
        val provider = remote.getProvider(id).getOrNull()
            ?.takeIf { it.name.isNotBlank() }
            ?: return getProviderById(id)
        val merged = withLocalPhotos(provider)
        persistLocal(merged)
        return merged
    }

    /** Pide los prestadores a la API y los guarda en Room. Devuelve false si no hubo red. */
    suspend fun refreshFromApi(): Boolean {
        val list = remote.getProviders().getOrNull() ?: return false
        if (list.isEmpty()) return false
        dao.insertAll(list.map { withLocalPhotos(it).toEntity() })
        cache = dao.getAll().map { it.toDomain() }
        return true
    }

    /**
     * Reemplaza en Room los teléfonos de ejemplo por números ficticios (445-000-01xx).
     * Solo toca la copia local; no escribe en Firestore ni cambia el dueño del perfil.
     */
    suspend fun fixDemoPhones() {
        val replacements = mapOf(
            "4451234567" to "4450000101",
            "4457654321" to "4450000102",
            "4459876543" to "4450000103"
        )
        val toFix = dao.getAll().map { it.toDomain() }.filter { it.phone in replacements }
        if (toFix.isEmpty()) return
        dao.insertAll(toFix.map { it.copy(phone = replacements.getValue(it.phone)).toEntity() })
        cache = dao.getAll().map { it.toDomain() }
    }

    private suspend fun persistLocal(provider: Provider) {
        dao.insertAll(listOf(provider.toEntity()))
        cache = dao.getAll().map { it.toDomain() }
    }
}
