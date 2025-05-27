package com.example.easeapp.ui.meeting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.easeapp.model.requests.AppointmentDetails
import com.example.easeapp.repositories.AppointmentRepository
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MyMeetingFragment : Fragment() {

    private lateinit var doctorNameText: TextView
    private lateinit var meetingDateText: TextView
    private lateinit var doctorImage: ImageView
    private lateinit var btnChange: Button
    private lateinit var btnCancel: Button
    private lateinit var btnApprove: Button
    private lateinit var btnScheduleNew: Button
    private lateinit var meetingLayout: View
    private lateinit var emptyLayout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_meeting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        doctorNameText = view.findViewById(R.id.doctorName)
        meetingDateText = view.findViewById(R.id.meetingDate)
        doctorImage = view.findViewById(R.id.doctorImage)
        btnChange = view.findViewById(R.id.changeTimeButton)
        btnCancel = view.findViewById(R.id.cancelMeetingButton)
        btnApprove = view.findViewById(R.id.approveMeetingButton)
        btnScheduleNew = view.findViewById(R.id.scheduleNewMeetingButton)
        meetingLayout = view.findViewById(R.id.meetingCardLayout)
        emptyLayout = view.findViewById(R.id.emptyLayout)

        fetchAndDisplayAppointment()

        btnScheduleNew.setOnClickListener {
            val action = MyMeetingFragmentDirections.actionMyMeetingFragmentToScheduleRoutineMeeting()
            findNavController().navigate(action)
        }
    }

    private fun fetchAndDisplayAppointment() {
        lifecycleScope.launch {
            try {
                val appointments = AppointmentRepository.shared.getUpcomingAppointmentForPatient(requireContext())
                val appointment = appointments?.firstOrNull { appt ->
                    (appt.status == "confirmed" && appt.isEmergency == false)
                            || (appt.status == "pending" && appt.isEmergency == false)
                }
                if (appointment != null) {
                    displayAppointment(appointment)
                } else {
                    displayNoAppointment()
                }
            } catch (e: Exception) {
                displayNoAppointment()
                //Toast.makeText(requireContext(), e.message ?: "Failed to load appointment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayAppointment(appointment: AppointmentDetails) {
        meetingLayout.visibility = View.VISIBLE
        emptyLayout.visibility = View.GONE
        btnChange.visibility = View.VISIBLE
        btnCancel.visibility = View.VISIBLE
        if (appointment.status == "pending") {
            btnApprove.visibility = View.VISIBLE
        } else {
            btnApprove.visibility = View.GONE
        }

        doctorNameText.text = appointment.doctorName ?: "Unknown Doctor"

        val formattedDate = try {
            val instant = Instant.parse(appointment.appointmentDate)
            val localDateTime = instant.atZone(ZoneId.systemDefault())
            localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        } catch (e: Exception) {
            appointment.appointmentDate
        }

        meetingDateText.text = formattedDate

        val imageUrl = appointment.doctorImageUrl ?: "https://example.com/sample.jpg"
        Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.account)
            .error(R.drawable.account)
            .into(doctorImage)

        btnChange.setOnClickListener {
            AppointmentRepository.shared.deleteAppointment(requireContext(), appointment._id) { success, message ->
                if (success) {
                    val action = MyMeetingFragmentDirections.actionMyMeetingFragmentToScheduleRoutineMeeting()
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), message ?: "Failed to cancel meeting", Toast.LENGTH_SHORT).show()
                }
            }
        }
        btnApprove.setOnClickListener{
            val dialogView = layoutInflater.inflate(R.layout.dialog_emergency_confirm, null)

            dialogView.findViewById<TextView>(R.id.textAreYouSure).text = "Are you sure you want to approve this appointment?"
            dialogView.findViewById<Button>(R.id.btnStartEmergency).text = "Approve my appointment"

            val dialog = android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create()

            val confirmButton = dialogView.findViewById<Button>(R.id.btnStartEmergency)
            val cancelButton = dialogView.findViewById<Button>(R.id.btnCancel)

            confirmButton.setOnClickListener {
                AppointmentRepository.shared.approveAppointment(requireContext(), appointment._id) { success, message ->
                    if (success) {
                        btnApprove.visibility = View.GONE
                        Toast.makeText(requireContext(), "Meeting approved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), message ?: "Failed to approve meeting", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
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
                AppointmentRepository.shared.cancelAppointment(requireContext(), appointment._id) { success, message ->
                    if (success) {
                        displayNoAppointment()
                        Toast.makeText(requireContext(), "Meeting canceled", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), message ?: "Failed to cancel meeting", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun displayNoAppointment() {
        meetingLayout.visibility = View.GONE
        emptyLayout.visibility = View.VISIBLE
        btnCancel.visibility = View.GONE
        btnChange.visibility = View.GONE
        btnApprove.visibility = View.GONE
        btnScheduleNew.visibility = View.VISIBLE

        val emptyText = emptyLayout.findViewById<TextView>(R.id.emptyMessage)
        emptyText?.text = "No meeting found. Let's schedule one!"
    }
}
