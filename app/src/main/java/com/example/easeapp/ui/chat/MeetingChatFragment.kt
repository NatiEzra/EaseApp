package com.example.easeapp.ui.chat

import ChatApiService
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.compose.ui.test.isEnabled
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import com.example.easeapp.model.AppAlertHandler
import com.example.easeapp.model.ChatMessage
import com.example.easeapp.model.SocketManager
import com.example.easeapp.repositories.AppointmentRepository
import com.example.easeapp.repository.ChatRepository
import com.squareup.picasso.Picasso
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.ease.model.UserRepository

class MeetingChatFragment : Fragment() {

    private lateinit var messageInput: EditText
    private lateinit var sendIcon: ImageView
    private lateinit var messageContainer: RecyclerView
    private lateinit var socket: Socket
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatMessageAdapter
    private var doctorIdForThisChat: String? = null
    private var progressBar: ProgressBar? = null
    private val args: MeetingChatFragmentArgs by navArgs()
    private var loadingLayout : LinearLayout? = null
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
        progressBar = view.findViewById(R.id.progressBar_EmergencyChat)
        loadingLayout = view.findViewById<LinearLayout>(R.id.loadingLayout)
        val doctorNameTextView = view.findViewById<TextView>(R.id.doctorName)
        val doctorImageView = view.findViewById<ImageView>(R.id.doctorImage)
        var isEmergencyMeeting :Boolean = false

        lifecycleScope.launch {
            try {
                //progressBar?.visibility = View.VISIBLE
                loadingLayout?.visibility = View.VISIBLE
                messageInput.isEnabled = false
                val thisAppointmentId = args.appointmentId

                val appointments = AppointmentRepository.shared.getUpcomingAppointmentForPatient(requireContext())

                val appointment = appointments?.firstOrNull { appt ->
                    appt._id == thisAppointmentId
                }
                if (appointment == null || appointment.status != "confirmed"&& !appointment.isEmergency) {
                    showBlockedChatDialog("You cannot access the chat because the appointment is not active.")
                    return@launch
                }
                isEmergencyMeeting = appointment.isEmergency
                doctorIdForThisChat= appointment.doctorId

                /*
                                val apiService = Retrofit.Builder()
                                    //.baseUrl("http://192.168.1.105:3000/")
                                    //.baseUrl("http://10.0.2.2:2999/")
                                    .baseUrl("http://10.0.2.2:3000/")
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()
                                    .create(ChatApiService::class.java)

                                val repository = ChatRepository(apiService)

                                val historyMessages = repository.fetchChatHistory(
                                    args.appointmentId,
                                    getUserId(),
                                    appointment.doctorId ?: "",
                                    requireContext()
                                )
                */
                val chatRepo = ChatRepository(requireContext())

                val historyMessages = chatRepo.fetchChatHistory(
                    meetingId       = args.appointmentId,
                    doctorImageUrl  = appointment.doctorId
                )
                if (appointment.doctorName!=null){
                    doctorNameTextView.text = appointment.doctorName
                }
                if (appointment.doctorId!=null){
                    doctorIdForThisChat= appointment.doctorId
                }


                messages.addAll(historyMessages)
                adapter.notifyDataSetChanged()
                messageContainer.scrollToPosition(messages.size - 1)

                connectSocket()
            } catch (e: Exception) {
                Log.e("Chat", "Error fetching appointment or history: ${e.message}")
                if (e.message!="Job was cancelled")
                    showBlockedChatDialog("An error occurred while loading the chat.")
            }
        }

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

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, /* enabled = */ true) {
                if (isEmergencyMeeting) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Leave Emergency Chat?")
                        .setMessage("This action will end the session. Are you sure you want to leave?")
                        .setCancelable(true)
                        .setPositiveButton("Yes - End Chat") { _, _ ->
                            isEnabled = false
                            val payload = JSONObject().apply {
                                put("meetingId", args.appointmentId)
                                put("doctorId", doctorIdForThisChat ?: "")
                            }
                            socket.emit("endConsultation", payload)
                            requireActivity().onBackPressed()
                        }
                        .setNegativeButton("No - Stay") { dlg, _ ->
                            dlg.dismiss()
                        }
                        .show()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }


    }

    private fun connectSocket() {
        // ── get the current access-token you store in AuthRepository ──
        val token = com.example.ease.repositories.AuthRepository
            .shared.getAccessToken(requireContext())

        val opts = IO.Options().apply {
            forceNew     = true
            reconnection = true
            query        = "token=$token"   // 👈 token goes in the handshake
            // If you prefer an HTTP header instead:
            // extraHeaders = mapOf("Authorization" to listOf("Bearer $token"))
        }

        socket = IO.socket("http://10.0.2.2:2999", opts)
        //socket = IO.socket("http://192.168.1.105:3000", opts)

        // ── if the server emits “unauthorized”, refresh & reconnect ──
        socket.on("unauthorized") {
            lifecycleScope.launch {
                val newToken = com.example.ease.repositories.AuthRepository
                    .shared.refreshAccessToken(requireContext())

                if (newToken.isNotEmpty()) {
                    socket.disconnect()
                    connectSocket()   // reconnect with fresh token (query updated)
                } else {
                    showBlockedChatDialog("Session expired. Please log in again.")
                }
            }
        }

        socket.on("userJoined") { args ->
            val data = args.getOrNull(0) as? JSONObject ?: return@on
            val joinedUserId = data.optString("userId")
            UserRepository.getInstance(requireContext()).getUser("", joinedUserId){success, userDetails, errorMessage ->
                if (success && userDetails != null) {
                    if (userDetails.role == "doctor") {
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Your doctor has joined the chat!",
                                Toast.LENGTH_LONG
                            ).show()
                            val doctorImageView = view?.findViewById<ImageView>(R.id.doctorImage)
                            val doctorNameTextView = view?.findViewById<TextView>(R.id.doctorName)
                            if (doctorNameTextView!= null && doctorImageView != null) {
                                Picasso.get().load(userDetails.profilePicture).into(doctorImageView)
                                doctorNameTextView.text = userDetails.username
                                doctorIdForThisChat = userDetails._id
                            }
                            //progressBar?.visibility = View.GONE
                            loadingLayout?.visibility = View.GONE
                            messageInput.isEnabled = true

                        }

                    }

                }

            }
        }

        socket.on(Socket.EVENT_CONNECT) {
            val joinData = JSONObject().apply {
                put("meetingId", args.appointmentId)
                put("userId", getUserId())
                put("role", "patient")
            }
            socket.emit("joinRoom", joinData)
            socket.emit("getRoomUsers", JSONObject().apply {
                put("meetingId", args.appointmentId)
            })

        }

        socket.on("consultationEnded") { args ->
            val data = args[0] as JSONObject
            val summary = data.optString("summary", "The consultation has ended.")

            activity?.runOnUiThread {
                handleSessionEnd(summary)
            }
        }
        socket.on("roomUsers") { args ->
            val data = args.getOrNull(0) as? JSONObject ?: return@on
            val users = data.optJSONArray("users") ?: return@on

            var doctorPresent = false
            for (i in 0 until users.length()) {
                val user = users.getJSONObject(i)
                if (user.optString("role") == "doctor") {
                    doctorPresent = true
                    break
                }
            }

            activity?.runOnUiThread {
                if (doctorPresent) {
                    loadingLayout?.visibility = View.GONE
                    messageInput.isEnabled = true
                } else {
                    loadingLayout?.visibility = View.VISIBLE
                    messageInput.isEnabled = false
                }
            }
        }
        socket.on("newMessage") { args ->
            val data = args[0] as JSONObject
            val senderId = data.getString("from")
            val currentUserId = getUserId()

            if (senderId != currentUserId) {
                val msg = data.getString("message")
                val timestampString = data.getString("timestamp")
                val formatter = java.time.format.DateTimeFormatter.ISO_INSTANT
                val timestamp = java.time.Instant.from(formatter.parse(timestampString)).toEpochMilli()

                activity?.runOnUiThread {
                    addMessageToUI(msg, fromMe = false, timestamp = timestamp, profileImageUrl = null)
                }
            }
        }

        socket.connect()
        //progressBar?.visibility = View.GONE
//        loadingLayout?.visibility = View.GONE
//
//        messageInput.isEnabled = true
    }

    private fun sendCurrentMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            sendMessage(messageText)
            messageInput.text.clear()
        }
    }

    private fun handleSessionEnd(summary: String) {
        messageInput.isEnabled = false
        sendIcon.isEnabled = false

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Session Ended")
            .setMessage("The consultation has ended. You can no longer send messages.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .create()
        dialog.show()

        socket.disconnect()
    }

    private fun sendMessage(messageText: String) {
        val msgData = JSONObject().apply {
            put("meetingId", args.appointmentId)
            put("from", getUserId())
            put("to", "doctor")
            put("message", messageText)
            put("timestamp", System.currentTimeMillis())
        }

        if (::socket.isInitialized) {
            socket.emit("sendMessage", msgData)
        }
        addMessageToUI(messageText, fromMe = true, timestamp = System.currentTimeMillis(), profileImageUrl = null)
    }

    private fun addMessageToUI(text: String, fromMe: Boolean, timestamp: Long, profileImageUrl: String? = null) {
        val newMessage = ChatMessage(text, fromMe, timestamp, profileImageUrl)
        adapter.addMessage(newMessage)
        messageContainer.scrollToPosition(adapter.itemCount - 1)
    }

    private fun showBlockedChatDialog(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Chat Unavailable")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                if (isAdded) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
            .show()
    }


    private fun getUserId(): String {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("userId", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::socket.isInitialized) {
            socket.disconnect()
        }
    }

}
