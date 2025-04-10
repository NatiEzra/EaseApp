package com.example.easeapp.ui.chat

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.ease.R
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

class MeetingChatFragment : Fragment() {

    private lateinit var messageInput: EditText
    private lateinit var sendIcon: ImageView
    private lateinit var messageContainer: LinearLayout
    private lateinit var webSocket: WebSocket

    private val args: MeetingChatFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_emergency_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        messageInput = view.findViewById(R.id.messageInput)
        sendIcon = view.findViewById(R.id.sendIcon)
        messageContainer = view.findViewById(R.id.messageContainer)

        startWebSocket(args.appointmentId)

        sendIcon.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val userId = getUserIdFromPrefs()
                val doctorId = getDoctorIdFromPrefs()

                val json = JSONObject().apply {
                    put("meetingId", args.appointmentId)
                    put("from", userId)
                    put("to", doctorId)
                    put("message", messageText)
                    put("timestamp", System.currentTimeMillis())
                }

                webSocket.send(json.toString())
                addMessageToUI(messageText, fromMe = true)
                messageInput.text.clear()
            }
        }

    }

    private fun startWebSocket(appointmentId: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("ws://192.168.1.105:3000/ws/$appointmentId")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                activity?.runOnUiThread {
                    addMessageToUI(text, fromMe = false)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Connection error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
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
        webSocket.close(1000, null)
    }
}
