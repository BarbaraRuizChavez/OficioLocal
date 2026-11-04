package com.example.oficiolocal.domain

import androidx.annotation.StringRes
import com.example.oficiolocal.R

enum class Urgency { LOW, MEDIUM, HIGH }

enum class RequestStatus { PENDING, ACCEPTED, COMPLETED, CANCELLED }

enum class JobType(@StringRes val label: Int) {
    PLUMBING(R.string.job_plumbing),
    ELECTRICITY(R.string.job_electricity),
    CARPENTRY(R.string.job_carpentry),
    TECHNICIAN(R.string.job_technician);

    companion object {
        // Convierte la categoría (texto) de un prestador al tipo de trabajo.
        fun fromCategory(category: String?): JobType? = when (category?.lowercase()) {
            "plomería" -> PLUMBING
            "electricidad" -> ELECTRICITY
            "carpintería" -> CARPENTRY
            "técnico" -> TECHNICIAN
            else -> null
        }
    }
}

data class ServiceRequest(
    val id: String,
    val providerName: String,
    val description: String,
    val urgency: Urgency,
    val status: RequestStatus,
    val dateMillis: Long?,
    val hour: Int?,
    val minute: Int?,
    val needsMaterials: Boolean = false,
    val providerId: String? = null,
    val jobType: JobType? = null,
    val location: String = ""
)