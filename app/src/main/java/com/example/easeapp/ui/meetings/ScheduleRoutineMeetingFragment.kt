package com.example.easeapp.ui.meetings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import com.example.ease.model.User
import com.example.ease.model.UserRepository
import com.example.ease.viewmodel.UserViewModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class DoctorsViewHolder(
    itemView: View,
    private val onBookClick: (User) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val doctorNameTextView: TextView = itemView.findViewById(R.id.doctorName)
    private val experienceTextView: TextView = itemView.findViewById(R.id.doctorExperience)
    private val doctorImageView: ImageView = itemView.findViewById(R.id.doctorImage)
    private val bookButton: Button = itemView.findViewById(R.id.bookNowBtn)

    private fun fixImageUrl(oldUrl: String): String {
        return oldUrl.replace("http://localhost:", "http://10.0.2.2:")
    }

    fun bind(doctor: User) {
        doctorNameTextView.text = doctor.username
        experienceTextView.text = "15 Years experience"
        val profilePicture = fixImageUrl(doctor.profilePicture)
        Picasso.get()
            .load(profilePicture)
            .transform(CropCircleTransformation())
            .into(doctorImageView)

        bookButton.setOnClickListener {
            onBookClick(doctor)
        }
    }
}

class DoctorRecycleAdapter(
    private var originalList: List<User>,
    private val onBookClick: (User) -> Unit
) : RecyclerView.Adapter<DoctorsViewHolder>() {

    private var doctors: MutableList<User> = originalList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.doctor_item, parent, false)
        return DoctorsViewHolder(view, onBookClick)
    }

    override fun getItemCount(): Int = doctors.size

    fun set(newList: List<User>) {
        originalList = newList
        doctors = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        doctors = if (query.isEmpty()) {
            originalList.toMutableList()
        } else {
            originalList.filter {
                it.username.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: DoctorsViewHolder, position: Int) {
        holder.bind(doctors[position])
    }
}

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ScheduleRoutineMeeting : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var userRepository: UserRepository
    private var doctors: MutableList<User> = ArrayList()
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    val userViewModel: UserViewModel by viewModels()

    private lateinit var adapter: DoctorRecycleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        userRepository = UserRepository.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule_routine_meeting, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.doctorRecyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = DoctorRecycleAdapter(doctors) { selectedDoctor ->
            val action = ScheduleRoutineMeetingDirections.actionScheduleRoutineMeetingToBookingAppointmentFragment(selectedDoctor._id)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter

        userViewModel.allDoctors.observe(viewLifecycleOwner) { fetchedUsers ->
            doctors.clear()
            doctors.addAll(fetchedUsers)
            adapter.set(doctors)
            adapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        getAllDoctors()

        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        getAllDoctors()
    }

    private fun getAllDoctors() {
        userViewModel.fetchAlldoctors()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ScheduleRoutineMeeting().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}