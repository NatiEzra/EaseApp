package com.example.ease.ui.auth

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.ease.model.local.AppDatabase
import com.example.ease.model.local.UserEntity
import com.example.ease.ui.activities.MainActivity
import com.example.ease.viewmodel.AuthViewModel
import com.example.ease.viewmodel.UserViewModel
import com.example.easeapp.model.requests.UserApi
import com.example.easeapp.model.requests.UpdateProfileResponse
import com.example.easeapp.model.requests.UserApiClient
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private var cameraLauncher: ActivityResultLauncher<Void?>? = null
private var galleryLauncher: ActivityResultLauncher<String>? = null
private lateinit var dateTextView: TextView
private lateinit var genderSpinner: Spinner
private lateinit var phoneEditText: EditText
class editProfileFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var progressBar: ProgressBar
    private var addedImageToProfile: Boolean = false

    private lateinit var authViewModel: AuthViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val editIcon = view.findViewById<ImageView>(R.id.editIcon)
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        progressBar = view.findViewById(R.id.editProfileProgressBar)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val cancelButton = view.findViewById<Button>(R.id.CancelButtonEditProfile)

        val editProfileName = view.findViewById<EditText>(R.id.editProfileName)
        phoneEditText = view.findViewById(R.id.editProfilePhone)
        dateTextView = view.findViewById(R.id.editProfileDateOfBirth)
        genderSpinner = view.findViewById(R.id.editProfileGender)

        val genders = listOf("Select gender", "male", "female", "other")
        val genderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        dateTextView.setOnClickListener { showDatePicker() }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap)
                val uri = Uri.parse(
                    MediaStore.Images.Media.insertImage(
                        requireContext().contentResolver,
                        bitmap,
                        null,
                        null
                    )
                )
                val fixedUri = fixImageUrl(uri.toString()) + "?t=" + System.currentTimeMillis()
                Picasso.get().load(fixedUri).transform(CropCircleTransformation()).into(profileImage)
                addedImageToProfile = true
            }
        }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val fixedUri = fixImageUrl(uri.toString()) + "?t=" + System.currentTimeMillis()
                Picasso.get().load(fixedUri).transform(CropCircleTransformation()).into(profileImage)
                addedImageToProfile = true
            }
        }
        editIcon.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Choose an option")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher?.launch(null)
                    1 -> galleryLauncher?.launch("image/*")
                }
            }
            builder.show()
        }

        lifecycleScope.launch {
            val user = AppDatabase.getInstance(requireContext()).userDao().getCurrentUser()
            user?.let {
                editProfileName.setText(it.name)
                phoneEditText.setText(it.phoneNumber ?: "")
                dateTextView.text = if (it.dateOfBirth.isNullOrEmpty()) "Select date" else formatDate(it.dateOfBirth)
                val genderPos = genders.indexOf(it.gender)
                if (genderPos >= 0) {
                    genderSpinner.setSelection(genderPos)
                }
                val imageUrl = it.profileImageUrl
                if (!imageUrl.isNullOrEmpty()) {
                    val fixedUrl = fixImageUrl(imageUrl) + "?t=" + System.currentTimeMillis()
                    Picasso.get().load(fixedUrl).transform(CropCircleTransformation()).into(profileImage)
                } else {
                    profileImage.setImageResource(R.drawable.account)
                }
            }
        }

        saveButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val newName = editProfileName.text.toString().trim()
            val newPhone = phoneEditText.text.toString().trim()
            val newDate = dateTextView.text.toString().trim()
            val newGender = genderSpinner.selectedItem.toString()

            val bitmap: Bitmap? = if (addedImageToProfile) {
                (profileImage.drawable as? BitmapDrawable)?.bitmap
            } else null

            lifecycleScope.launch {
                val currentUser = AppDatabase.getInstance(requireContext()).userDao().getCurrentUser()
                if (currentUser != null) {
                    val nameBody = newName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val phoneBody = newPhone.toRequestBody("text/plain".toMediaTypeOrNull())
                    val dobBody = newDate.toRequestBody("text/plain".toMediaTypeOrNull())
                    val genderBody = newGender.toRequestBody("text/plain".toMediaTypeOrNull())

                    val imagePart: MultipartBody.Part? = if (bitmap != null) {
                        val imageFile = bitmapToFile(bitmap, requireContext())
                        MultipartBody.Part.createFormData(
                            "profilePicture",
                            imageFile.name,
                            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                        )
                    } else null

                    val authHeader = "Bearer " + currentUser.accessToken?.trim()

                    val userApi: UserApi = UserApiClient.create(requireContext())

                    userApi.updateUserProfile(
                        userId = currentUser._id,
                        username = nameBody,
                        phoneNumber = phoneBody,
                        dateOfBirth = dobBody,
                        gender = genderBody,
                        profilePicture = imagePart
                    ).enqueue(object : retrofit2.Callback<UpdateProfileResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<UpdateProfileResponse>,
                            response: retrofit2.Response<UpdateProfileResponse>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                val updatedUser = response.body()?.user
                                updatedUser?.let { u ->
                                    lifecycleScope.launch {
                                        AppDatabase.getInstance(requireContext()).userDao().insert(
                                            UserEntity(
                                                _id = u._id,
                                                email = u.email,
                                                name = u.username,
                                                profileImageUrl = u.profilePicture,
                                                accessToken = currentUser.accessToken,
                                                phoneNumber = u.phoneNumber,
                                                dateOfBirth = u.dateOfBirth,
                                                gender = u.gender
                                            )
                                        )
                                    }
                                    (activity as? MainActivity)?.refreshProfile(requireContext())
                                    findNavController().popBackStack()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                            progressBar.visibility = View.GONE
                        }

                        override fun onFailure(call: retrofit2.Call<UpdateProfileResponse>, t: Throwable) {
                            Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }

        cancelButton.setOnClickListener {
            (activity as? MainActivity)?.myProfilePageButtonClicked()
        }

        return view
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = String.format("%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear)
                dateTextView.text = date
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun bitmapToFile(bitmap: Bitmap, context: android.content.Context, fileName: String = "temp_image.jpg"): File {
        val file = File(context.cacheDir, fileName)
        file.outputStream().use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
    }

    fun fixImageUrl(oldUrl: String): String {
        return oldUrl.replace("http://localhost:", "http://10.0.2.2:")
    }

    fun formatDate(isoDate: String): String {
        return try {
            val localDate = LocalDate.parse(isoDate.substring(0, 10))
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
            localDate.format(formatter)
        } catch (e: Exception) {
            isoDate
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            editProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}