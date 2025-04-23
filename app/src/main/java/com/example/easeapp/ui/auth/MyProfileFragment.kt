package com.example.ease.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.ease.ui.activities.MainActivity
import com.example.ease.R
import com.example.ease.viewmodel.AuthViewModel
import com.example.ease.model.local.AppDatabase
import com.example.easeapp.model.SocketManager
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class myProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)

        val editButton: TextView = view.findViewById(R.id.editButton)
        editButton.setOnClickListener {
            (activity as? MainActivity)?.editProfileButtonClicked()
        }

        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        progressBar = view.findViewById(R.id.profileImageProgressBar)

        val profileName = view.findViewById<TextView>(R.id.profileName)
        val profileEmail = view.findViewById<TextView>(R.id.profileEmail)
        val profilePhone = view.findViewById<TextView>(R.id.profileUsername)
        val profileDate = view.findViewById<TextView>(R.id.profileDate)
        val profileGender = view.findViewById<TextView>(R.id.profileGender)

        progressBar.visibility = View.VISIBLE
        profileImage.visibility = View.GONE
        lifecycleScope.launch {
            val user = AppDatabase.getInstance(requireContext()).userDao().getCurrentUser()
            profileName.text = user?.name ?: "Guest"
            profileEmail.text = user?.email ?: ""
            profilePhone.text = user?.phoneNumber?.takeIf { it.isNotEmpty() } ?: "Not provided"
            profileDate.text = user?.dateOfBirth?.takeIf { it.isNotEmpty() }?.let { formatDate(it) } ?: "Not provided"
            profileGender.text = user?.gender?.takeIf { it.isNotEmpty() } ?: "Not provided"

            var imageUrl = user?.profileImageUrl
            if (!imageUrl.isNullOrEmpty()) {
                imageUrl = fixImageUrl(imageUrl)
                Picasso.get()
                    .load(imageUrl)
                    .transform(CropCircleTransformation())
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.account)
            }
            progressBar.visibility = View.GONE
            profileImage.visibility = View.VISIBLE
        }

        val logOutbtn = view.findViewById<Button>(R.id.logoutButton)
        val autViewModel: AuthViewModel by lazy { ViewModelProvider(this)[AuthViewModel::class.java] }
        logOutbtn.setOnClickListener {
            lifecycleScope.launch {
                AppDatabase.getInstance(requireContext()).userDao().clear()
            }
            SocketManager.disconnect()
            autViewModel.signOut(requireContext())
            Toast.makeText(context, "You logged out, have a great day", Toast.LENGTH_LONG).show()
            (activity as? MainActivity)?.navigateToLogin()
        }
        return view
    }

    private fun fixImageUrl(oldUrl: String): String {
        return oldUrl.replace("http://localhost:", "http://10.0.2.2:")
    }

    private fun formatDate(isoDate: String): String {
        return try {
            val localDate = LocalDate.parse(isoDate.substring(0, 10))
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
            localDate.format(formatter)
        } catch (e: Exception) {
            isoDate
        }
    }
}