package com.example.ease.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import androidx.navigation.fragment.findNavController


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
        findNavController().navigate(R.id.action_homePageFragment_to_diaryFragment)
    }

    private fun openEmergency() {
        findNavController().navigate(R.id.action_homePageFragment_to_emergencyPageFragment)
    }


    private fun openChat() {
        findNavController().navigate(R.id.chatSelectorFragment)
    }

    private fun showInfo(title: String, message: String) {
        Toast.makeText(requireContext(), "$title Info: $message", Toast.LENGTH_LONG).show()
    }
}
