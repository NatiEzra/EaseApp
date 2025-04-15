package com.example.easeapp.repository

import ChatApiService
import com.example.easeapp.model.ChatMessage
import java.time.format.DateTimeFormatter
import java.time.Instant

class ChatRepository(private val api: ChatApiService) {

    suspend fun fetchChatHistory(
        meetingId: String,
        currentUserId: String,
        doctorImageUrl: String?
    ): List<ChatMessage> {
        val response = api.getChatHistory(meetingId)
        val lines = response.split("\n").filter { it.isNotBlank() }

        return lines.mapNotNull { line ->
            try {
                val timeStart = line.indexOf("[") + 1
                val timeEnd = line.indexOf("]")
                val timestampStr = line.substring(timeStart, timeEnd)
                val timestamp = Instant.parse(timestampStr).toEpochMilli()

                val rest = line.substring(timeEnd + 2)
                val parts = rest.split(" -> ", ": ")

                val from = parts[0].trim()
                val to = parts[1].trim()
                val message = parts[2].trim()

                ChatMessage(
                    text = message,
                    fromMe = from == currentUserId,
                    timestamp = timestamp,
                    profileImageUrl = if (from != currentUserId) doctorImageUrl else null
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
