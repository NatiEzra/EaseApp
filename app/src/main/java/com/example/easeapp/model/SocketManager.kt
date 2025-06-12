package com.example.easeapp.model

import android.content.Context
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {
    private lateinit var socket: Socket
    private var onNewNotificationCallback: (() -> Unit)? = null

    fun init(userId: String, context: Context) {
        if (::socket.isInitialized && socket.connected()) return

        val opts = IO.Options().apply {
            forceNew = true
            reconnection = true
        }

        socket = IO.socket("https://ease.cs.colman.ac.il/", opts)

        socket.on(Socket.EVENT_CONNECT) {
            Log.d("SOCKET", "✅ Connected to socket")
            socket.emit("joinUser", JSONObject().put("userId", userId))
            socket.emit("joinRoom", JSONObject().apply {
                put("userId", userId)
                put("meetingId", JSONObject.NULL)
                put("role", "patient")
            })
        }

        socket.on("appointmentCanceled") { args ->
            val data = args[0] as JSONObject
            val reason = data.optString("reason", "Your appointment was canceled.")
            AppAlertHandler.showGlobalDialog(context, "Your meeting has been canceled by the doctor. Please reschedule a new meeting.")
        }

        socket.on("appointmentCreated") { args ->
            val data = args[0] as JSONObject
            val summary = data.optString("summary", "A doctor is trying to set an appointment with you.")
            Log.d("SOCKET", "🔥 Received appointmentCreated: $summary")
            AppAlertHandler.showGlobalDialog(context, summary)
        }


        socket.on("newNotification") { args ->
            val data = args[0] as JSONObject
            Log.d("SOCKET", "📩 New notification received: $data")
            onNewNotificationCallback?.invoke()
        }

        socket.connect()
    }

    fun setOnNewNotificationCallback(callback: () -> Unit) {
        onNewNotificationCallback = callback
    }

    fun disconnect() {
        if (::socket.isInitialized) {
            socket.disconnect()
        }
    }
}
