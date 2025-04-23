package com.example.easeapp.ui.meeting

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.easeapp.viewmodel.AppointmentViewModel
import com.squareup.picasso.Picasso

class MyMeetingFragment : Fragment() {

    private lateinit var doctorNameText: TextView
    private lateinit var meetingDateText: TextView
    private lateinit var doctorImage: ImageView
    private lateinit var btnChange: Button
    private lateinit var btnCancel: Button
    private lateinit var btnScheduleNew: Button
    private lateinit var meetingLayout: View
    private lateinit var emptyLayout: View
    private lateinit var appointmentViewModel: AppointmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_meeting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        appointmentViewModel = ViewModelProvider(this)[AppointmentViewModel::class.java]

        doctorNameText = view.findViewById(R.id.doctorName)
        meetingDateText = view.findViewById(R.id.meetingDate)
        doctorImage = view.findViewById(R.id.doctorImage)
        btnChange = view.findViewById(R.id.changeTimeButton)
        btnCancel = view.findViewById(R.id.cancelMeetingButton)
        btnScheduleNew = view.findViewById(R.id.scheduleNewMeetingButton)
        meetingLayout = view.findViewById(R.id.meetingCardLayout)
        emptyLayout = view.findViewById(R.id.emptyLayout)

        val prefs = requireContext().getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
        val doctorName = prefs.getString("doctorName", null)
        val date = prefs.getString("date", null)
        val time = prefs.getString("time", null)
        val appointmentId = prefs.getString("appointmentId", null)
        val meetingEnded = prefs.getBoolean("meetingEnded", false)

        if (doctorName != null && date != null && time != null && !meetingEnded) {
            // יש פגישה שלא הסתיימה
            meetingLayout.visibility = View.VISIBLE
            emptyLayout.visibility = View.GONE
            btnChange.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE

            doctorNameText.text = doctorName
            meetingDateText.text = "$date at $time"
            Picasso.get().load("https://example.com/sample.jpg").into(doctorImage)

            btnChange.setOnClickListener {
                val prefs = requireContext().getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
                val appointmentId = prefs.getString("appointmentId", null)

                if (appointmentId != null) {
                    appointmentViewModel.cancelAppointment(requireContext(), appointmentId)

                    appointmentViewModel.cancelAppointmentStatus.observe(viewLifecycleOwner) { result ->
                        result.onSuccess {
                            prefs.edit().clear().apply()

                            val action = MyMeetingFragmentDirections.actionMyMeetingFragmentToScheduleRoutineMeeting()
                            findNavController().navigate(action)
                        }

                        result.onFailure {
                            Toast.makeText(requireContext(), "Failed to cancel meeting", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            btnCancel.setOnClickListener {
                val dialogView = layoutInflater.inflate(R.layout.dialog_emergency_confirm, null)

                dialogView.findViewById<TextView>(R.id.textAreYouSure).text = "Are you sure you want to cancel this appointment?"
                dialogView.findViewById<Button>(R.id.btnStartEmergency).text = "Cancel my appointment"

                val dialog = android.app.AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setCancelable(true)
                    .create()

                val confirmButton = dialogView.findViewById<Button>(R.id.btnStartEmergency)
                val cancelButton = dialogView.findViewById<Button>(R.id.btnCancel)

                confirmButton.setOnClickListener {
                    val prefs = requireContext().getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
                    val appointmentId = prefs.getString("appointmentId", null)

                    if (appointmentId != null) {
                        appointmentViewModel.cancelAppointment(requireContext(), appointmentId)
                    }

                    prefs.edit().clear().apply()
                    Toast.makeText(requireContext(), "Meeting canceled", Toast.LENGTH_SHORT).show()

                    meetingLayout.visibility = View.GONE
                    emptyLayout.visibility = View.VISIBLE
                    btnCancel.visibility = View.GONE
                    btnChange.visibility = View.GONE

                    dialog.dismiss()
                }

                cancelButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }

        } else {
            meetingLayout.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        }

        btnScheduleNew.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("meeting_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("meetingEnded", false).apply() // איפוס כשקובעים חדשה

            val action = MyMeetingFragmentDirections.actionMyMeetingFragmentToScheduleRoutineMeeting()
            findNavController().navigate(action)
        }

        appointmentViewModel.cancelAppointmentStatus.observe(viewLifecycleOwner) { result ->
            result.onFailure {
                Toast.makeText(requireContext(), "Failed to cancel meeting", Toast.LENGTH_SHORT).show()
            }
        }
    }
}