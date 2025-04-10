package com.example.ease.ui.chat

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.easeapp.ui.chat.MeetingChatFragmentDirections.Companion.actionRoutineFragmentToMeetingChatFragment

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
            val prefs = requireContext().getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
            val hasMeeting = prefs.getString("doctorName", null) != null

            if (hasMeeting) {
                findNavController().navigate(R.id.myMeetingFragment)
            } else {

                findNavController().navigate(R.id.scheduleRoutineMeeting)
            }
        }
        btnMyMeeting.setOnClickListener {
            val action = ChatRoutineFragmentDirections.actionRoutineFragmentToMyMeetingFragment()
            findNavController().navigate(action)
        }

        btnJoinMeeting.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
            val appointmentId = prefs.getString("appointmentId", null)
            Log.d("JOIN_MEETING", "appointmentId from prefs: $appointmentId")

            if (appointmentId != null) {
                val action = ChatRoutineFragmentDirections.actionRoutineFragmentToMeetingChatFragment(appointmentId)
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "No meeting found", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
