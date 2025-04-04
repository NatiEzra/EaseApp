package com.example.easeapp.ui.meetings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class BookingAppointmentFragment : Fragment() {

    private var doctorId: String? = null
    private lateinit var dateRecyclerView: RecyclerView
    private lateinit var timeRecyclerView: RecyclerView
    private lateinit var continueButton: Button

    private val dateAdapter = DateAdapter()
    private val timeAdapter = TimeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doctorId = arguments?.getString("doctorId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_booking_appointment, container, false)

        dateRecyclerView = view.findViewById(R.id.dateRecyclerView)
        timeRecyclerView = view.findViewById(R.id.timeRecyclerView)
        continueButton = view.findViewById(R.id.continueButton)

        // Setup Date RecyclerView
        dateRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        dateRecyclerView.adapter = dateAdapter

        // Setup Time RecyclerView
        timeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        timeRecyclerView.adapter = timeAdapter

        generateDateList()
        generateTimeSlots()

        continueButton.setOnClickListener {
            val selectedDate = dateAdapter.getSelectedDate()
            val selectedTime = timeAdapter.getSelectedTime()
            if (selectedDate != null && selectedTime != null) {
                // TODO: Proceed with doctorId, selectedDate, and selectedTime
            }
        }

        return view
    }

    private fun generateDateList() {
        val today = LocalDate.now()
        val days = mutableListOf<AppointmentDate>()

        for (i in 0..6) {
            val date = today.plusDays(i.toLong())
            val dayLetter = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())[0].toString()
            days.add(AppointmentDate(dayLetter, date.dayOfMonth.toString(), date.toString()))
        }

        dateAdapter.submitList(days)
    }

    private fun generateTimeSlots() {
        val times = listOf(
            "08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM",
            "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 AM"
        )
        timeAdapter.submitList(times)
    }

    companion object {
        @JvmStatic
        fun newInstance(doctorId: String) =
            BookingAppointmentFragment().apply {
                arguments = Bundle().apply {
                    putString("doctorId", doctorId)
                }
            }
    }
}
