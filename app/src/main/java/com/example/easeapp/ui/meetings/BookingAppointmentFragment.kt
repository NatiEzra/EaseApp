package com.example.easeapp.ui.meetings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import com.example.easeapp.model.requests.AvailableSlotsResponse
import com.example.easeapp.model.requests.RetrofitClientAppointments
import com.example.easeapp.ui.adapters.DateAdapter
import com.example.easeapp.ui.adapters.TimeAdapter
import com.example.easeapp.model.AppointmentDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

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

        // Setup RecyclerViews
        dateRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        dateRecyclerView.adapter = dateAdapter

        timeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        timeRecyclerView.adapter = timeAdapter

        generateDateList()

        dateAdapter.setOnDateSelectedListener { selectedDate ->
            fetchAvailableTimeSlots(doctorId, selectedDate)
        }

        continueButton.setOnClickListener {
            val selectedDate = dateAdapter.getSelectedDate()
            val selectedTime = timeAdapter.getSelectedTime()
            if (selectedDate != null && selectedTime != null) {
                // TODO: שלח את הפגישה לשרת או עבור למסך אישור
            }
        }

        return view
    }

    fun fetchAvailableTimeSlots(doctorId: String?, date: String) {
        if (doctorId == null) return
        var accessToken= getAccessToken(requireContext())
        accessToken = accessToken?.replace("refreshToken=", "hello, ")
        accessToken = accessToken?.replace(";", "")
        val call = RetrofitClientAppointments.appointmentsApi.getAvailableSlots(accessToken!!,doctorId, date)
        call.enqueue(object : Callback<AvailableSlotsResponse> {
            override fun onResponse(call: Call<AvailableSlotsResponse>, response: Response<AvailableSlotsResponse>) {
                if (response.isSuccessful) {
                    val slots = response.body()?.slots ?: emptyList()
                    timeAdapter.submitList(slots)
                } else {
                    Log.e("Booking", "Error fetching slots: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AvailableSlotsResponse>, t: Throwable) {
                Log.e("Booking", "Failure: ${t.message}")
            }
        })
    }
    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("access_token_cookie", null)
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
