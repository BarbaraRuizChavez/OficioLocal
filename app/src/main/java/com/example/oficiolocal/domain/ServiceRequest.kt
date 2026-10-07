package com.example.oficiolocal.domain

enum class Urgency { LOW, MEDIUM, HIGH }

enum class RequestStatus { PENDING, ACCEPTED, COMPLETED, CANCELLED }

data class ServiceRequest(
    val id: String,
    val providerName: String,
    val description: String,
    val urgency: Urgency,
    val status: RequestStatus,
    val dateMillis: Long?,
    val hour: Int?,
    val minute: Int?
)