package com.example.oficiolocal.data.repository

import com.example.oficiolocal.data.local.dao.ServiceRequestDao
import com.example.oficiolocal.data.local.entity.toDomain
import com.example.oficiolocal.data.local.entity.toEntity
import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.domain.ServiceRequest
import com.example.oficiolocal.domain.Urgency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface RequestRepository {
    val requests: Flow<List<ServiceRequest>>
    suspend fun add(request: ServiceRequest)
    suspend fun cancel(id: String)
    suspend fun delete(id: String)
}

/** Solicitudes: siempre se guardan primero en Room y después se envían a la API. */
class RoomRequestRepository(
    private val dao: ServiceRequestDao,
    private val remote: RemoteRepository,
    private val scope: CoroutineScope
) : RequestRepository {

    override val requests: Flow<List<ServiceRequest>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(request: ServiceRequest) {
        dao.insert(request.toEntity())
        scope.launch { sendToApi(request.id) }
    }

    override suspend fun cancel(id: String) {
        dao.updateStatus(id, RequestStatus.CANCELLED.name)
        scope.launch {
            val entity = dao.getById(id) ?: return@launch
            val remoteId = entity.remoteId ?: return@launch // si aún no se envió, se manda ya cancelada
            remote.updateRequest(entity.toDomain().copy(id = remoteId))
        }
    }

    override suspend fun delete(id: String) {
        val remoteId = dao.getById(id)?.remoteId
        dao.delete(id)
        if (remoteId != null) scope.launch { remote.deleteRequest(remoteId) }
    }

    /** Envía a la API las solicitudes que quedaron guardadas sin internet. */
    suspend fun syncPending() {
        dao.getUnsynced().forEach { sendToApi(it.id) }
    }

    private suspend fun sendToApi(id: String) {
        val entity = dao.getById(id) ?: return
        if (entity.remoteId != null) return
        remote.createRequest(entity.toDomain()).onSuccess { created ->
            if (created.id.isNotBlank()) dao.setRemoteId(id, created.id)
        }
    }
}

/** Datos de ejemplo en memoria, por si se necesitan para pruebas. */
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

    override suspend fun delete(id: String) = data.update { list -> list.filter { it.id != id } }
}