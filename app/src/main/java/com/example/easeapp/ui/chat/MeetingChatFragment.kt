package com.example.easeapp.ui.chat

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.ease.R
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class MeetingChatFragment : Fragment() {

    private lateinit var messageInput: EditText
    private lateinit var sendIcon: ImageView
    private lateinit var messageContainer: LinearLayout
    private lateinit var socket: Socket

    private val args: MeetingChatFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_emergency_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        messageInput = view.findViewById(R.id.messageInput)
        sendIcon = view.findViewById(R.id.sendIcon)
        messageContainer = view.findViewById(R.id.messageContainer)

        connectSocket()

        sendIcon.setOnClickListener {
            sendCurrentMessage()
        }

        // 🔥 שליחה על אנטר
        messageInput.setOnEditorActionListener { _, _, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                sendCurrentMessage()
                true
            } else {
                false
            }
        }

        val doctorId = getDoctorIdFromPrefs()
        Log.d("SOCKET", "📤 Sending message to doctorId: $doctorId")
    }

    private fun connectSocket() {
        val opts = IO.Options().apply {
            forceNew = true
            reconnection = true
        }

        socket = IO.socket("http://192.168.1.105:3000", opts)

        socket.on(Socket.EVENT_CONNECT) {
            Log.d("SOCKET", "Connected ✅")

            val joinData = JSONObject().apply {
                put("meetingId", args.appointmentId)
                put("userId", getUserIdFromPrefs())
                put("role", "patient")
            }
            socket.emit("joinRoom", joinData)
        }

        socket.on("newMessage") { args ->
            val data = args[0] as JSONObject
            val senderId = data.getString("from")
            val currentUserId = getUserIdFromPrefs()

            if (senderId != currentUserId) {
                val msg = data.getString("message")
                activity?.runOnUiThread {
                    addMessageToUI(msg, fromMe = false)
                }
            } else {
                Log.d("SOCKET", "📥 Ignored own message from server")
            }
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("SOCKET", "Disconnected ❌")
        }

        socket.connect()
    }

    private fun sendCurrentMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            sendMessage(messageText)
            messageInput.text.clear()
        }
    }

    private fun sendMessage(messageText: String) {
        val msgData = JSONObject().apply {
            put("meetingId", args.appointmentId)
            put("from", getUserIdFromPrefs())
            put("to", getDoctorIdFromPrefs())
            put("message", messageText)
            put("timestamp", System.currentTimeMillis())
        }

        socket.emit("sendMessage", msgData)
        addMessageToUI(messageText, fromMe = true)
    }

    private fun addMessageToUI(text: String, fromMe: Boolean) {
        val messageView = TextView(requireContext()).apply {
            this.text = text
            setTextColor(if (fromMe) Color.WHITE else Color.BLACK)
            setBackgroundResource(if (fromMe) R.drawable.bg_chat_bubble_user else R.drawable.bg_chat_bubble)
            setPadding(20, 12, 20, 12)
            textSize = 16f

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
                if (fromMe) {
                    gravity = Gravity.END
                    marginEnd = 16
                } else {
                    gravity = Gravity.START
                    marginStart = 16
                }
            }
        }
        messageContainer.addView(messageView)
    }

    private fun getUserIdFromPrefs(): String {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("userId", "") ?: ""
    }

    private fun getDoctorIdFromPrefs(): String {
        val prefs = requireContext().getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        return prefs.getString("doctorId", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        socket.disconnect()
    }
}
