package com.example.oficiolocal.data.repository

import com.example.oficiolocal.data.local.dao.ProviderDao
import com.example.oficiolocal.data.local.entity.toDomain
import com.example.oficiolocal.data.local.entity.toEntity
import com.example.oficiolocal.domain.Provider
import kotlinx.coroutines.runBlocking

/** Prestadores: se leen de Room y se actualizan desde la API. */
class ProviderRepository(
    private val dao: ProviderDao,
    private val remote: RemoteRepository
) {

    // Datos de ejemplo: se usan solo si la base está vacía y aún no hay internet.
    private val seed = listOf(
        Provider(
            id = "1", name = "Carlos Mendoza", category = "Plomería", location = "Centro",
            rating = 4.8, reviewCount = 15, priceRange = "$200 - $500",
            schedule = "Lun - Sáb: 8:00 AM - 7:00 PM", phone = "4451234567",
            description = "Plomero certificado con más de 10 años de experiencia en reparación de fugas e instalación sanitaria.",
            services = listOf("Reparación de fugas", "Instalación de tanques", "Destape de cañerías")
        ),
        Provider(
            id = "2", name = "Ana Gómez", category = "Electricidad", location = "Norte",
            rating = 4.9, reviewCount = 22, priceRange = "$300 - $800",
            schedule = "Lun - Vie: 9:00 AM - 5:00 PM", phone = "4457654321",
            description = "Técnica electricista enfocada en instalaciones residenciales y mantenimiento.",
            services = listOf("Cableado residencial", "Solución de cortocircuitos", "Instalación de lámparas")
        ),
        Provider(
            id = "3", name = "Roberto Silva", category = "Carpintería", location = "Sur",
            rating = 4.6, reviewCount = 9, priceRange = "$400 - $1200",
            schedule = "Lun - Sáb: 9:00 AM - 6:00 PM", phone = "4459876543",
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

    /** Pide los prestadores a la API y los guarda en Room. Devuelve false si no hubo red. */
    suspend fun refreshFromApi(): Boolean {
        val list = remote.getProviders().getOrNull() ?: return false
        if (list.isEmpty()) return false
        dao.insertAll(list.map { it.toEntity() })
        cache = dao.getAll().map { it.toDomain() }
        return true
    }
}