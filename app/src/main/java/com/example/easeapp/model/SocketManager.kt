package com.example.easeapp.model

import android.content.Context
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {
    lateinit var socket: Socket

    fun init(userId: String, context: Context) {
        if (::socket.isInitialized && socket.connected()) return

        val opts = IO.Options().apply {
            forceNew = true
            reconnection = true
        }

        //socket = IO.socket("http://192.168.1.105:3000", opts)
        socket = IO.socket("http://10.0.2.2:2999", opts)
        //socket = IO.socket("http://10.0.2.2:3000", opts)


        socket.on(Socket.EVENT_CONNECT) {
            // subscribe globally _and_ to chats
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
            val summary = data.optString("summary", "A doctor is trying to set an appointment with you. Please check 'My Meeting' page for more details.")
            Log.d("SOCKET", "🔥 Received appointmentCreated with message: $summary")
            AppAlertHandler.showGlobalDialog(context, summary)
        }

        socket.connect()
    }

    fun disconnect() {
        if (::socket.isInitialized) {
            socket.disconnect()
        }
    }
}
