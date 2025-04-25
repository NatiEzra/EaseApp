package com.example.ease.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.easeapp.repositories.AppointmentRepository
import kotlinx.coroutines.launch

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
            lifecycleScope.launch {
                try {
                    val appointment = AppointmentRepository.shared.getUpcomingAppointmentForPatient(requireContext())
                    if (appointment != null) {
                        val action = ChatRoutineFragmentDirections.actionRoutineFragmentToMyMeetingFragment()
                        findNavController().navigate(action)
                    } else {
                        val action = ChatRoutineFragmentDirections.actionChatRoutineFragmentToScheduleRoutineMeeting()
                        findNavController().navigate(action)
                    }
                } catch (e: Exception) {
                    val action = ChatRoutineFragmentDirections.actionChatRoutineFragmentToScheduleRoutineMeeting()
                    findNavController().navigate(action)
                }
            }
        }

        btnMyMeeting.setOnClickListener {
            val action = ChatRoutineFragmentDirections.actionRoutineFragmentToMyMeetingFragment()
            findNavController().navigate(action)
        }

        btnJoinMeeting.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val appointment = AppointmentRepository.shared.getUpcomingAppointmentForPatient(requireContext())

                    if (appointment != null && appointment.status == "confirmed") {
                        val action = ChatRoutineFragmentDirections
                            .actionRoutineFragmentToMeetingChatFragment(appointment._id)
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(requireContext(), "No active meeting found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error checking meeting status", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
