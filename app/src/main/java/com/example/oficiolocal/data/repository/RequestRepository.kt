package com.example.oficiolocal.data.repository

import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.domain.ServiceRequest
import com.example.oficiolocal.domain.Urgency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface RequestRepository {
    val requests: Flow<List<ServiceRequest>>
    suspend fun add(request: ServiceRequest)
    suspend fun cancel(id: String)
}

class FakeRequestRepository : RequestRepository {
    private val data = MutableStateFlow(
        listOf(
            ServiceRequest("r1", "Juan Pérez", "Fuga de agua en la cocina", Urgency.HIGH,
                RequestStatus.PENDING, null, 10, 30),
            ServiceRequest("r2", "María López", "Revisar contacto sin corriente", Urgency.MEDIUM,
                RequestStatus.ACCEPTED, null, 16, 0),
            ServiceRequest("r3", "Carlos Ruiz", "Arreglar puerta del clóset", Urgency.LOW,
                RequestStatus.COMPLETED, null, 9, 0),
            ServiceRequest("r4", "Ana Torres", "Instalar impresora en red", Urgency.LOW,
                RequestStatus.CANCELLED, null, 12, 0)
        )
    )

    override val requests: Flow<List<ServiceRequest>> = data

    override suspend fun add(request: ServiceRequest) = data.update { listOf(request) + it }

    override suspend fun cancel(id: String) = data.update { list ->
        list.map { if (it.id == id) it.copy(status = RequestStatus.CANCELLED) else it }
    }
}