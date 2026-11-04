package com.example.oficiolocal.data.remote

import com.example.oficiolocal.domain.JobType
import com.example.oficiolocal.domain.Provider
import com.example.oficiolocal.domain.RequestStatus
import com.example.oficiolocal.domain.ServiceRequest
import com.example.oficiolocal.domain.Urgency

// Casi todo es nulo porque Gson no respeta los valores por defecto de Kotlin.
data class ProviderDto(
    val id: String? = null,
    val name: String? = null,
    val category: String? = null,
    val location: String? = null,
    val rating: Double? = null,
    val reviewCount: Int? = null,
    val priceRange: String? = null,
    val schedule: String? = null,
    val phone: String? = null,
    val description: String? = null,
    val services: List<String>? = null
)

data class RequestDto(
    val id: String? = null,
    val providerId: String? = null,
    val providerName: String? = null,
    val jobType: String? = null,
    val description: String? = null,
    val location: String? = null,
    val urgency: String? = null,
    val needsMaterials: Boolean? = null,
    val status: String? = null,
    val dateMillis: Long? = null,
    val hour: Int? = null,
    val minute: Int? = null
)

fun ProviderDto.toDomain() = Provider(
    id = id.orEmpty(),
    name = name.orEmpty(),
    category = category.orEmpty(),
    location = location.orEmpty(),
    rating = rating ?: 0.0,
    reviewCount = reviewCount ?: 0,
    priceRange = priceRange.orEmpty(),
    schedule = schedule.orEmpty(),
    phone = phone.orEmpty(),
    description = description.orEmpty(),
    services = services ?: emptyList()
)

fun RequestDto.toDomain() = ServiceRequest(
    id = id.orEmpty(),
    providerName = providerName.orEmpty(),
    description = description.orEmpty(),
    urgency = runCatching { Urgency.valueOf(urgency.orEmpty()) }.getOrDefault(Urgency.MEDIUM),
    status = runCatching { RequestStatus.valueOf(status.orEmpty()) }.getOrDefault(RequestStatus.PENDING),
    dateMillis = dateMillis,
    hour = hour,
    minute = minute,
    needsMaterials = needsMaterials ?: false,
    providerId = providerId,
    jobType = jobType?.let { runCatching { JobType.valueOf(it) }.getOrNull() },
    location = location.orEmpty()
)

fun ServiceRequest.toDto() = RequestDto(
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