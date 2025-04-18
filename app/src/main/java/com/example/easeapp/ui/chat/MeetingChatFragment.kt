package com.example.easeapp.ui.chat

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import com.example.easeapp.model.ChatMessage
import com.squareup.picasso.Picasso
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject


class MeetingChatFragment : Fragment() {

    private lateinit var messageInput: EditText
    private lateinit var sendIcon: ImageView
    private lateinit var messageContainer: RecyclerView
    private lateinit var socket: Socket
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatMessageAdapter

    private val args: MeetingChatFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_emergency_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        messageInput = view.findViewById(R.id.messageInput)
        sendIcon = view.findViewById(R.id.sendIcon)
        messageContainer = view.findViewById(R.id.chatRecyclerView)
        messageContainer.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatMessageAdapter(messages)
        messageContainer.adapter = adapter

        val doctorNameTextView = view.findViewById<TextView>(R.id.doctorName)
        val doctorImageView = view.findViewById<ImageView>(R.id.doctorImage)

        val prefs = requireContext().getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        val doctorName = prefs.getString("doctorName", "Doctor")
        val doctorImageUrl = prefs.getString("doctorImageUrl", null)

        doctorNameTextView.text = doctorName
        if (!doctorImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(doctorImageUrl)
                .placeholder(R.drawable.account)
                .into(doctorImageView)
        }

        connectSocket()

        sendIcon.setOnClickListener {
            sendCurrentMessage()
        }

        messageInput.setOnEditorActionListener { _, _, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                sendCurrentMessage()
                true
            } else {
                false
            }
        }
    }

    private fun connectSocket() {
        val opts = IO.Options().apply {
            forceNew = true
            reconnection = true
        }

        socket = IO.socket("http://10.0.2.2:3000", opts)

        socket.on(Socket.EVENT_CONNECT) {
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
                val timestampString = data.getString("timestamp")
                val formatter = java.time.format.DateTimeFormatter.ISO_INSTANT
                val timestamp = java.time.Instant.from(formatter.parse(timestampString)).toEpochMilli()

                activity?.runOnUiThread {
                    addMessageToUI(msg, fromMe = false, timestamp = timestamp, profileImageUrl = getDoctorImageUrl())
                }

            }
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
        addMessageToUI(messageText, fromMe = true, timestamp = System.currentTimeMillis(), profileImageUrl = null)
    }

    private fun addMessageToUI(text: String, fromMe: Boolean, timestamp: Long, profileImageUrl: String? = null) {
        val newMessage = ChatMessage(text, fromMe, timestamp, profileImageUrl)
        adapter.addMessage(newMessage)
        messageContainer.scrollToPosition(adapter.itemCount - 1)
    }

    private fun getUserIdFromPrefs(): String {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("userId", "") ?: ""
    }

    private fun getDoctorIdFromPrefs(): String {
        val prefs = requireContext().getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        return prefs.getString("doctorId", "") ?: ""
    }

    private fun getDoctorImageUrl(): String? {
        val prefs = requireContext().getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        return prefs.getString("doctorImageUrl", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        socket.disconnect()
    }
}
