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

        val btnSchedule = view.findViewById<Button>(R.id.btnSchedule)
        val btnMyMeeting = view.findViewById<Button>(R.id.btnMyMeeting)
        val btnJoinMeeting = view.findViewById<Button>(R.id.btnJoinMeeting)

        btnSchedule.setOnClickListener {
            findNavController().navigate(R.id.scheduleRoutineMeeting)
        }

        btnMyMeeting.setOnClickListener {
            val action = ChatRoutineFragmentDirections.actionRoutineFragmentToMyMeetingFragment()
            findNavController().navigate(action)
        }

        btnJoinMeeting.setOnClickListener {
            Toast.makeText(requireContext(), "Join Meeting clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
