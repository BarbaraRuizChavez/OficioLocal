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
    suspend fun update(request: ServiceRequest)
    suspend fun setStatus(id: String, status: RequestStatus)
    suspend fun cancel(id: String)
    suspend fun delete(id: String)
    suspend fun refreshFromApi(): Boolean
    suspend fun syncPending()
}

/** Solicitudes: siempre se guardan primero en Room y después se envían a la API. */
class RoomRequestRepository(
    private val dao: ServiceRequestDao,
    private val remote: RemoteRepository,
    private val scope: CoroutineScope,
    private val notifications: NotificationRepository,
    private val auth: AuthRepository,
    private val providers: ProviderRepository
) : RequestRepository {

    override val requests: Flow<List<ServiceRequest>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(request: ServiceRequest) {
        dao.insert(request.toEntity())
        notifications.requestCreated(request.id)
        scope.launch { sendToApi(request.id) }
    }

    override suspend fun update(request: ServiceRequest) {
        val current = dao.getById(request.id) ?: return
        val updated = request.toEntity().copy(
            remoteId = current.remoteId,
            createdAt = current.createdAt
        )
        dao.update(updated)

        val remoteId = current.remoteId
        scope.launch {
            if (auth.getFirebaseUserId() == null) return@launch
            if (remoteId == null) {
                sendToApi(request.id)
            } else {
                remote.updateRequest(updated.toDomain().copy(id = remoteId))
            }
        }
    }

    override suspend fun setStatus(id: String, status: RequestStatus) {
        dao.updateStatus(id, status.name)
        notifications.requestStatusChanged(id, status)
        scope.launch {
            if (auth.getFirebaseUserId() == null) return@launch
            val entity = dao.getById(id) ?: return@launch
            val remoteId = entity.remoteId
            if (remoteId == null) {
                sendToApi(id)
            } else {
                remote.updateRequest(entity.toDomain().copy(id = remoteId))
            }
        }
    }

    override suspend fun cancel(id: String) {
        dao.updateStatus(id, RequestStatus.CANCELLED.name)
        notifications.requestStatusChanged(id, RequestStatus.CANCELLED)
        scope.launch {
            if (auth.getFirebaseUserId() == null) return@launch
            val entity = dao.getById(id) ?: return@launch
            val remoteId = entity.remoteId
            if (remoteId == null) {
                sendToApi(id) // si todavía no se había enviado, se crea ya con el estado cancelado
            } else {
                remote.updateRequest(entity.toDomain().copy(id = remoteId))
            }
        }
    }

    override suspend fun delete(id: String) {
        val entity = dao.getById(id)
        val userId = auth.getFirebaseUserId()
        dao.delete(id)
        val remoteId = entity?.remoteId
        if (remoteId != null && userId != null && entity?.clientId == userId) {
            scope.launch { remote.deleteRequest(remoteId) }
        }
    }

    /** Recupera solicitudes remotas y las combina con Room sin duplicar las ya sincronizadas. */
    override suspend fun refreshFromApi(): Boolean {
        val user = auth.getCurrentUser() ?: return false
        val userId = auth.getFirebaseUserId() ?: return false
        // Las solicitudes guardan el ID del perfil, no el UID de Firebase.
        // ProviderRepository mantiene el mapeo del perfil usado al crear la solicitud.
        val providerId = user.takeIf { it.isProvider }?.let(providers::providerIdFor)
        val remoteRequests = remote.getRequestsForUser(userId, providerId).getOrNull() ?: return false
        val entities = remoteRequests
            .filter { it.id.isNotBlank() }
            .map { request ->
                val existing = dao.getByRemoteId(request.id)
                if (existing != null && existing.status != request.status.name) {
                    notifications.requestStatusChanged(existing.id, request.status)
                }
                request.toEntity().copy(
                    id = existing?.id ?: "remote-${request.id}",
                    remoteId = request.id,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis()
                )
            }
        dao.mergeRemote(entities)
        return true
    }

    /** Envía a la API las solicitudes que quedaron guardadas sin internet. */
    override suspend fun syncPending() {
        val userId = auth.getFirebaseUserId() ?: return
        dao.getUnsynced()
            .filter { it.clientId == userId }
            .forEach { sendToApi(it.id) }
    }

    private suspend fun sendToApi(id: String) {
        val userId = auth.getFirebaseUserId() ?: return
        val entity = dao.getById(id) ?: return
        if (entity.remoteId != null || entity.clientId != userId) return
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

    override suspend fun update(request: ServiceRequest) = data.update { list ->
        list.map { if (it.id == request.id) request else it }
    }

    override suspend fun setStatus(id: String, status: RequestStatus) = data.update { list ->
        list.map { if (it.id == id) it.copy(status = status) else it }
    }

    override suspend fun cancel(id: String) = data.update { list ->
        list.map { if (it.id == id) it.copy(status = RequestStatus.CANCELLED) else it }
    }

    override suspend fun delete(id: String) = data.update { list -> list.filter { it.id != id } }

    override suspend fun refreshFromApi(): Boolean = true

    override suspend fun syncPending() = Unit
}
