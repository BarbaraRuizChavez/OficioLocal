package com.example.oficiolocal.data.repository

import com.example.oficiolocal.domain.Provider

class ProviderRepository {
    private val providersList = listOf(
        Provider(
            id = "1",
            name = "Carlos Mendoza",
            category = "Plomería",
            location = "Centro",
            rating = 4.8,
            reviewCount = 15,
            priceRange = "$200 - $500",
            schedule = "Lun - Sáb: 8:00 AM - 7:00 PM",
            phone = "4451234567",
            description = "Plomero certificado con más de 10 años de experiencia en reparación de fugas e instalación sanitaria.",
            services = listOf("Reparación de fugas", "Instalación de tanques", "Destape de cañerías")
        ),
        Provider(
            id = "2",
            name = "Ana Gómez",
            category = "Electricidad",
            location = "Norte",
            rating = 4.9,
            reviewCount = 22,
            priceRange = "$300 - $800",
            schedule = "Lun - Vie: 9:00 AM - 5:00 PM",
            phone = "4457654321",
            description = "Técnica electricista enfocada en instalaciones residenciales y mantenimiento.",
            services = listOf("Cableado residencial", "Solución de cortocircuitos", "Instalación de lámparas")
        ),
        Provider(
            id = "3",
            name = "Roberto Silva",
            category = "Carpintería",
            location = "Sur",
            rating = 4.6,
            reviewCount = 9,
            priceRange = "$400 - $1200",
            schedule = "Lun - Sáb: 9:00 AM - 6:00 PM",
            phone = "4459876543",
            description = "Fabricación y reparación de muebles a medida, puertas y closets.",
            services = listOf("Reparación de puertas", "Muebles a medida", "Lijado y barnizado")
        )
    )

    fun getProviders(query: String = "", categoryFilter: String? = null): List<Provider> {
        return providersList.filter { provider ->
            val matchesQuery = provider.name.contains(query, ignoreCase = true) ||
                    provider.category.contains(query, ignoreCase = true) ||
                    provider.location.contains(query, ignoreCase = true)
            val matchesCategory = categoryFilter == null || provider.category.equals(categoryFilter, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }

    fun getProviderById(id: String): Provider? {
        return providersList.find { it.id == id }
    }
}