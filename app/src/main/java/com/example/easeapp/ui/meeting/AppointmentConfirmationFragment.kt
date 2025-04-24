package com.example.easeapp.ui.meeting

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AppointmentConfirmationFragment : Fragment() {

    private var doctorName: String? = null
    private var date: String? = null
    private var time: String? = null
    private var appointmentId: String? = null
    private var doctorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            appointmentId = it.getString("appointmentId")
            doctorName = it.getString("doctorName")
            date = it.getString("date")
            time = it.getString("time")
            doctorId = it.getString("doctorId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_appointment_confirmation, container, false)

        val formattedTime = try {
            val instant = Instant.parse(time)
            val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
            localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            time ?: "Unknown Time"
        }

        Log.d("CONFIRM", "✅ Confirmed appointment: $appointmentId with $doctorId at $time")

        // טקסט התצוגה
        view.findViewById<TextView>(R.id.doctorText).text =
            "You booked an appointment with Dr. $doctorName on $date at $formattedTime"

        // כפתור Done
        view.findViewById<Button>(R.id.doneButton).setOnClickListener {
            val action = AppointmentConfirmationFragmentDirections
                .actionAppointmentConfirmationFragmentToHomePageFragment()
            findNavController().navigate(action)
        }

        return view
    }

    companion object {
        fun newInstance(doctorName: String, date: String, time: String, appointmentId: String) =
            AppointmentConfirmationFragment().apply {
                arguments = Bundle().apply {
                    putString("appointmentId", appointmentId)
                    putString("doctorName", doctorName)
                    putString("date", date)
                    putString("time", time)
                }
            }
    }
}