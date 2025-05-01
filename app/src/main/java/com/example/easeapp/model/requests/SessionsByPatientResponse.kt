package com.example.easeapp.model.responses

import com.example.easeapp.model.requests.AppointmentDetails

data class SessionsByPatientResponse(
    val patientName: String,
    val sessions: List<AppointmentDetails>
)
