package com.example.easeapp.model.requests

data class ScheduleResponse(
    val schedules: List<Schedule>
)

data class Schedule(
    val _id: String,
    val doctorId: String,
    val dayOfWeek: Int
)

// תגובה לזמינות פגישות לפי תאריך
data class AvailableSlotsResponse(
    val doctorId: String,
    val date: String,
    val slots: List<String>
)

data class AppointmentResponse(
    val message: String,
    val appointment: AppointmentDetails
)

data class AppointmentDetails(
    val _id: String,
    val patientId: UserDetails,
    val patientName: String,
    val doctorId: String,
    val appointmentDate: String,
    val status: String,
    val notes: String,
    val isEmergency: Boolean,
    val createdAt: String,
    val updatedAt: String
)


data class ClosestAppointmentResponse(
    val closestAppointmentDate: String
)
