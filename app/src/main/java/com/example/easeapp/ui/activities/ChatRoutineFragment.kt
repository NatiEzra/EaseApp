package com.example.ease.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ease.R

class ChatRoutineFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_routine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val backIcon = view.findViewById<ImageView>(R.id.backIcon)
        val menuIcon = view.findViewById<ImageView>(R.id.menuIcon)
        val btnSchedule = view.findViewById<Button>(R.id.btnSchedule)
        val btnMyMeeting = view.findViewById<Button>(R.id.btnMyMeeting)
        val btnJoinMeeting = view.findViewById<Button>(R.id.btnJoinMeeting)

        backIcon.setOnClickListener {
            findNavController().navigate(R.id.chatSelectorFragment)
        }

        menuIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Menu clicked (future feature)", Toast.LENGTH_SHORT).show()
        }

        btnSchedule.setOnClickListener {
            Toast.makeText(requireContext(), "Schedule clicked", Toast.LENGTH_SHORT).show()
        }

        btnMyMeeting.setOnClickListener {
            Toast.makeText(requireContext(), "My Meeting clicked", Toast.LENGTH_SHORT).show()
        }

        btnJoinMeeting.setOnClickListener {
            Toast.makeText(requireContext(), "Join Meeting clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
