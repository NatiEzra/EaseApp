package com.example.easeapp.model

data class ChatMessage(
    val text: String,
    val fromMe: Boolean,
    val timestamp: Long,
    val profileImageUrl: String? = null
)

data class MeetingHistoryResponse(
    val meetingId: String,
    val history: String,
    val aiMessages: List<String>
)
