package com.example.easeapp.model.requests

data class AppointmentRequest(
    val patientId: String,
    val doctorId: String,
    val appointmentDate: String,
    val notes: String? = null,
    val isEmergency: Boolean = false,
    val initiator: String
)
