package com.example.easeapp.ui.chat

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R

class EmergencyChatFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_emergency_chat, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sendIcon = view.findViewById<ImageView>(R.id.sendIcon)
        val messageInput = view.findViewById<EditText>(R.id.messageInput)
        val messageContainer = view.findViewById<RecyclerView>(R.id.chatRecyclerView)

        sendIcon.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val newMessage = TextView(requireContext()).apply {
                    text = messageText
                    setBackgroundResource(R.drawable.bg_chat_bubble_user)
                    setPadding(20, 12, 20, 12)
                    setTextColor(resources.getColor(android.R.color.white))
                    textSize = 16f

                    // מרווח סביב ההודעה
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = 16
                    params.marginEnd = 16
                    params.gravity = Gravity.END
                    layoutParams = params
                }

                messageContainer.addView(newMessage)
                messageInput.text.clear()
            }
        }
    }


}
