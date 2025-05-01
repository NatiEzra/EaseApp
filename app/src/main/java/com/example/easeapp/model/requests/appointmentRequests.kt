package com.example.easeapp.model.requests

data class AppointmentRequest(
    val patientId: String,
    val doctorId: String?,
    val appointmentDate: String?,
    val notes: List<String>? = null,
    val isEmergency: Boolean = false,
    val initiator: String
)

data class UpdateAppointmentRequest(
    val status: String,
    val appointmentDate: String? = null,
    val notes: String? = null,
    val isEmergency: Boolean? = null
)

data class RoleRequest(val role: String)