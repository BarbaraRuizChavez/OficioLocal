package com.example.oficiolocal.data.repository

import com.example.oficiolocal.domain.Provider
import com.example.oficiolocal.domain.Trade
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface ProviderRepository {
    val providers: Flow<List<Provider>>
}

/** Datos de prueba. En la siguiente etapa se reemplaza por Room + Retrofit. */
class FakeProviderRepository : ProviderRepository {
    private val data = MutableStateFlow(
        listOf(
            Provider("1", "Juan Pérez", Trade.PLUMBING, "Centro", 4.8, 350, true),
            Provider("2", "María López", Trade.ELECTRICITY, "Norte", 4.5, 400, false),
            Provider("3", "Carlos Ruiz", Trade.CARPENTRY, "Sur", 4.2, 500, true),
            Provider("4", "Ana Torres", Trade.TECH, "Centro", 4.9, 300, true),
            Provider("5", "Luis Gómez", Trade.PLUMBING, "Norte", 4.0, 280, false)
        )
    )
    override val providers: Flow<List<Provider>> = data
}