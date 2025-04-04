package com.example.easeapp.model.requests

data class ScheduleResponse(
    val schedules: List<Schedule>
)

data class Schedule(
    val _id: String,
    val doctorId: String,
    val dayOfWeek: Int
)
