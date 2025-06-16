package com.example.ease.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.easeapp.repositories.AppointmentRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatRoutineFragment : Fragment() {
    private lateinit var progressBar: ProgressBar

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
        progressBar = view.findViewById(R.id.progressBar2)

        btnSchedule.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val appointments = AppointmentRepository.shared.getUpcomingAppointmentForPatient(requireContext())
                    val appointment = appointments?.find { (it.status == "confirmed" || it.status == "pending" )&& !it.isEmergency }
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
                progressBar.visibility = View.GONE
            }
        }

        btnMyMeeting.setOnClickListener {
            val action = ChatRoutineFragmentDirections.actionRoutineFragmentToMyMeetingFragment()
            findNavController().navigate(action)
        }

        btnJoinMeeting.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    var tooEarly: Boolean =false
                    val appointments = AppointmentRepository.shared.getUpcomingAppointmentForPatient(requireContext())
                    if (appointments != null) {
                        for (appointment in appointments) {
                            if (appointment.status == "confirmed") {
                                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                                formatter.timeZone = TimeZone.getTimeZone("UTC")
                                val appointmentTime = formatter.parse(appointment.appointmentDate)
                                val currentTime = Date()

                                // Calculate time difference in minutes
                                val diffInMillis = appointmentTime.time - currentTime.time
                                val diffInMinutes = diffInMillis / (60 * 1000)
                                if ( diffInMinutes<=10) {
                                    val action = ChatRoutineFragmentDirections
                                        .actionRoutineFragmentToMeetingChatFragment(appointment._id)
                                    findNavController().navigate(action)
                                    return@launch
                                }
                                else{
                                    Toast.makeText(requireContext(), "You may join the meeting 10 minutes before it begins", Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.GONE
                                    tooEarly=true
                                }

//                                val action = ChatRoutineFragmentDirections
//                                    .actionRoutineFragmentToMeetingChatFragment(appointment._id)
//                                findNavController().navigate(action)
//                                return@launch
                            }
                        }
                    }
                    progressBar.visibility = View.GONE
                    if (!tooEarly)
                    {
                        Toast.makeText(requireContext(), "No active meeting found", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error checking meeting status", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
