package com.example.oficiolocal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.oficiolocal.domain.JobType
import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.domain.ServiceRequest
import com.example.oficiolocal.domain.Urgency

@Entity(tableName = "service_requests")
data class ServiceRequestEntity(
    @PrimaryKey val id: String,
    val providerId: String?,
    val providerName: String,
    val jobType: String?,
    val description: String,
    val location: String,
    val urgency: String,
    val needsMaterials: Boolean,
    val status: String,
    val dateMillis: Long?,
    val hour: Int?,
    val minute: Int?,
    val remoteId: String? = null, // id que le asigna la API; null = aún no se envía
    val createdAt: Long = System.currentTimeMillis()
)

fun ServiceRequestEntity.toDomain() = ServiceRequest(
    id = id,
    providerName = providerName,
    description = description,
    urgency = Urgency.valueOf(urgency),
    status = RequestStatus.valueOf(status),
    dateMillis = dateMillis,
    hour = hour,
    minute = minute,
    needsMaterials = needsMaterials,
    providerId = providerId,
    jobType = jobType?.let { runCatching { JobType.valueOf(it) }.getOrNull() },
    location = location
)

fun ServiceRequest.toEntity() = ServiceRequestEntity(
    id = id,
    providerId = providerId,
    providerName = providerName,
    jobType = jobType?.name,
    description = description,
    location = location,
    urgency = urgency.name,
    needsMaterials = needsMaterials,
    status = status.name,
    dateMillis = dateMillis,
    hour = hour,
    minute = minute
)