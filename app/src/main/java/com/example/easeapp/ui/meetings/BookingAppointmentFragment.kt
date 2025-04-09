
package com.example.easeapp.ui.meetings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.ease.model.local.AppDatabase
import com.example.easeapp.model.AppointmentDate
import com.example.easeapp.model.DisplayableSlot
import com.example.easeapp.model.requests.AvailableSlotsResponse
import com.example.easeapp.model.requests.RetrofitClientAppointments
import com.example.easeapp.ui.adapters.DateAdapter
import com.example.easeapp.ui.adapters.TimeAdapter
import com.example.easeapp.viewmodel.AppointmentViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class BookingAppointmentFragment : Fragment() {

    private var doctorId: String? = null
    private var doctorName: String? = null
    private var selectedDate: String? = null
    private var selectedSlot: DisplayableSlot? = null


    private lateinit var dateRecyclerView: RecyclerView
    private lateinit var timeRecyclerView: RecyclerView
    private lateinit var continueButton: Button

    private val appointmentViewModel: AppointmentViewModel by viewModels()
    private val dateAdapter = DateAdapter()
    private val timeAdapter = TimeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doctorId = arguments?.getString("doctorId")
        doctorName = arguments?.getString("doctorName")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_booking_appointment, container, false)

        dateRecyclerView = view.findViewById(R.id.dateRecyclerView)
        timeRecyclerView = view.findViewById(R.id.timeRecyclerView)
        continueButton = view.findViewById(R.id.continueButton)

        dateRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        dateRecyclerView.adapter = dateAdapter

        timeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        timeRecyclerView.adapter = timeAdapter

        val spacing = resources.getDimensionPixelSize(R.dimen.time_slot_spacing)
        timeRecyclerView.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.set(spacing, spacing, spacing, spacing)
                }
            }
        )

        generateDateList()

        dateAdapter.setOnDateSelectedListener { selected ->
            selectedDate = selected
            fetchAvailableTimeSlots(doctorId, selected)
        }

        appointmentViewModel.createAppointmentStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                if (selectedDate != null && selectedSlot != null && doctorName != null) {
                    val action = BookingAppointmentFragmentDirections
                        .actionBookingAppointmentFragmentToAppointmentConfirmationFragment(
                            doctorName = doctorName!!,
                            date = selectedDate!!,
                            time = selectedSlot!!.original

                        )
                    findNavController().navigate(action)
                }
            }
            result.onFailure {
                Toast.makeText(context, it.message ?: "Appointment not set :(", Toast.LENGTH_SHORT).show()
            }
        }

        continueButton.setOnClickListener {
            selectedDate = dateAdapter.getSelectedDate()
            selectedSlot = timeAdapter.getSelectedSlot()


            if (selectedDate != null && selectedSlot != null) {
                lifecycleScope.launch {
                    val user = AppDatabase.getInstance(requireContext()).userDao().getCurrentUser()
                    val patientId = user?._id ?: "default_patient_id"
                    doctorId?.let { docId ->
                        appointmentViewModel.createAppointment(
                            requireContext(),
                            patientId,
                            docId,
                            selectedSlot!!.original,
                            false
                        )
                    }
                }
            } else {
                Toast.makeText(context, "Please select a date and time", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun fetchAvailableTimeSlots(doctorId: String?, date: String) {
        if (doctorId == null) return
        var accessToken = getAccessToken(requireContext())?.replace("refreshToken=", "hello, ")?.replace(";", "") ?: return
        val call = RetrofitClientAppointments.appointmentsApi.getAvailableSlots(accessToken, doctorId, date)
        call.enqueue(object : Callback<AvailableSlotsResponse> {
            override fun onResponse(call: Call<AvailableSlotsResponse>, response: Response<AvailableSlotsResponse>) {
                if (response.isSuccessful) {
                    val slots = response.body()?.slots ?: emptyList()
                    val filteredSlots = slots.filter {
                        try {
                            Instant.parse(it).isAfter(Instant.now())
                        } catch (e: Exception) { false }
                    }
                    val displayableSlots = filteredSlots.map {
                        DisplayableSlot(
                            original = it,
                            display = formatTime(it)
                        )
                    }
                    timeAdapter.submitList(displayableSlots)
                } else {
                    Log.e("Booking", "Error fetching slots: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<AvailableSlotsResponse>, t: Throwable) {
                Log.e("Booking", "Failure: ${t.message}")
            }
        })
    }

    private fun generateDateList() {
        val today = LocalDate.now()
        val days = mutableListOf<AppointmentDate>()
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
        for (i in 0..6) {
            val date = today.plusDays(i.toLong())
            val displayDate = date.format(formatter)
            days.add(AppointmentDate(displayDate, date.dayOfMonth.toString(), date.toString()))
        }
        dateAdapter.submitList(days)
    }

    private fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("access_token_cookie", null)
    }

    private fun formatTime(instantString: String): String {
        return try {
            val instant = Instant.parse(instantString)
            val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
            localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            instantString
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(doctorId: String, doctorName: String) =
            BookingAppointmentFragment().apply {
                arguments = Bundle().apply {
                    putString("doctorId", doctorId)
                    putString("doctorName", doctorName)
                }
            }
    }
}
