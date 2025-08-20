package com.example.easeapp.ui.chat

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import java.text.SimpleDateFormat
import java.util.*
import com.example.easeapp.model.ChatMessage
import com.squareup.picasso.Picasso

class ChatMessageAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder>() {

    inner class ChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageText)
        val messageTimeView: TextView = itemView.findViewById(R.id.messageTime)
//        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val rootLayout: LinearLayout = itemView.findViewById(R.id.rootLayout)
        val bubbleLayout: LinearLayout = itemView.findViewById(R.id.bubbleLayout)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatMessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        val message = messages[position]

        holder.messageTextView.text = message.text
        holder.messageTimeView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))


        if (message.fromMe) {
            holder.rootLayout.gravity = Gravity.END

            holder.rootLayout.removeAllViews()
            holder.rootLayout.addView(holder.bubbleLayout)
//            holder.rootLayout.addView(holder.profileImage)

            holder.bubbleLayout.setBackgroundResource(R.drawable.bg_chat_bubble_user)
            holder.messageTextView.setTextColor(Color.BLACK)

        } else {
            holder.rootLayout.gravity = Gravity.START

            holder.rootLayout.removeAllViews()
//            holder.rootLayout.addView(holder.profileImage)
            holder.rootLayout.addView(holder.bubbleLayout)

            holder.bubbleLayout.setBackgroundResource(R.drawable.bg_chat_bubble)
            holder.messageTextView.setTextColor(Color.BLACK)
        }

    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
