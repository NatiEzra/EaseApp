package com.example.easeapp.ui.meetings
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.ease.R
import com.squareup.picasso.Picasso

class MyMeetingFragment : Fragment() {

    private lateinit var doctorName: TextView
    private lateinit var meetingDate: TextView
    private lateinit var doctorImage: ImageView
    private lateinit var changeTimeButton: Button
    private lateinit var cancelMeetingButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_meeting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        doctorName = view.findViewById(R.id.doctorName)
        meetingDate = view.findViewById(R.id.meetingDate)
        doctorImage = view.findViewById(R.id.doctorImage)
        changeTimeButton = view.findViewById(R.id.changeTimeButton)
        cancelMeetingButton = view.findViewById(R.id.cancelMeetingButton)

        doctorName.text = "Dr. Eva Elfie"
        meetingDate.text = "February 21 at 02:00 PM"
        Picasso.get().load("https://example.com/doctor.jpg").into(doctorImage)

        changeTimeButton.setOnClickListener {
            // לוגיקה לשינוי שעה
        }

        cancelMeetingButton.setOnClickListener {
            // לוגיקה לביטול פגישה
        }
    }
}
