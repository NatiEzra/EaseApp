package com.example.ease.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.ease.R

class HomePageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_homepage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val diaryCard = view.findViewById<CardView>(R.id.cardDiary)
        val emergencyCard = view.findViewById<CardView>(R.id.cardEmergency)
        val chatCard = view.findViewById<CardView>(R.id.cardChat)

        val diaryInfo = view.findViewById<ImageView>(R.id.diaryInfo)
        val emergencyInfo = view.findViewById<ImageView>(R.id.emergencyInfo)
        val chatInfo = view.findViewById<ImageView>(R.id.chatInfo)

        diaryCard.setOnClickListener {
            openDiary()
        }

        emergencyCard.setOnClickListener {
            openEmergency()
        }

        chatCard.setOnClickListener {
            openChat()
        }

        diaryInfo.setOnClickListener {
            showInfo("Diary", "כתיבה אישית לשחרור רגשי וארגון מחשבות.")
        }

        emergencyInfo.setOnClickListener {
            showInfo("Emergency", "גישה מהירה לעזרה ראשונה או אנשי קשר.")
        }

        chatInfo.setOnClickListener {
            showInfo("Chat", "שיחה עם מומחים או תמיכה.")
        }
    }

    private fun openDiary() {
        Toast.makeText(requireContext(), "Opening Diary...", Toast.LENGTH_SHORT).show()
        // TODO: ניווט למסך Diary (כשתיצור)
    }

    private fun openEmergency() {
        Toast.makeText(requireContext(), "Opening Emergency...", Toast.LENGTH_SHORT).show()
        // TODO: ניווט למסך Emergency (כשתיצור)
    }

    private fun openChat() {
        Toast.makeText(requireContext(), "Opening Chat...", Toast.LENGTH_SHORT).show()
        // TODO: ניווט למסך Chat (כשתיצור)
    }

    private fun showInfo(title: String, message: String) {
        Toast.makeText(requireContext(), "$title Info: $message", Toast.LENGTH_LONG).show()
    }
}
