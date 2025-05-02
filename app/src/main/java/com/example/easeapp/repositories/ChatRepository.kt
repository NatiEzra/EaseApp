package com.example.easeapp.repository

import ChatApiService
import android.content.Context
import com.example.ease.model.local.AppDatabase
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.ChatMessage
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.format.DateTimeParseException

class ChatRepository(private val api: ChatApiService) {

    suspend fun fetchChatHistory(
        meetingId: String,
        currentUserId: String,
        doctorImageUrl: String?,
        context: Context,
    ): List<ChatMessage> {
        var token = AuthRepository.shared.getAccessToken(context)
        token = token!!.replace("refreshToken=", "hello ")
        token = token.replace(";", "")
        val db = AppDatabase.getInstance(context)
        val user = db.userDao().getCurrentUser()
        val userId= user?._id
        val response = api.getChatHistory(meetingId)
        val chatHistory=parseChatHistory(response.body()!!.history, userId!!, doctorImageUrl)
        return if (response.isSuccessful) {
            chatHistory
        } else {
            throw Exception("Error fetching chat history: ${response.errorBody()?.string() ?: "Unknown error"}")
        }

    }

    fun parseChatHistory(
        historyString: String,
        currentUserId: String,
        defaultProfileImageUrl: String? = null
    ): List<ChatMessage> {
        // Define a regex that captures:
        //   1. The timestamp in brackets: [timestamp]
        //   2. The sender (non whitespace)
        //   3. The receiver (non whitespace)
        //   4. The message text (rest of the line)
        val regex = Regex("""^\[(.*?)\]\s+(\S+)\s+->\s+(\S+):\s+(.*)$""")
        val messages = mutableListOf<ChatMessage>()

        // Split the history string into individual lines
        val lines = historyString.split("\n")
        for (line in lines) {
            if (line.trim().isEmpty()) continue

            val matchResult = regex.find(line)
            if (matchResult != null) {
                val (timestampStr, sender, receiver, messageText) = matchResult.destructured

                // Parse the ISO timestamp string into a Long (milliseconds)
                val timestampMillis = try {
                    Instant.parse(timestampStr).toEpochMilli()
                } catch (e: DateTimeParseException) {
                    // If there's an error, skip this message or use 0L as fallback
                    0L
                }

                // Determine if the message is from the current user
                val fromMe = sender == currentUserId

                // Set profileImageUrl: For example, if the sender is not the current user, use the default provided.
                val profileImageUrl = if (fromMe) null else defaultProfileImageUrl

                messages.add(ChatMessage(
                    text = messageText,
                    fromMe = fromMe,
                    timestamp = timestampMillis,
                    profileImageUrl = profileImageUrl
                ))
            }
        }
        return messages
    }

}
