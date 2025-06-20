package com.example.ease.ui.auth

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.example.ease.ui.activities.LoginRegisterActivity
import com.example.ease.R
import com.example.ease.repositories.AuthRepository
import com.example.ease.model.User
import com.example.ease.viewmodel.AuthViewModel
import com.example.ease.viewmodel.UserViewModel
import com.example.easeapp.ui.activities.PrivacyPolicyDialogFragment
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var emailField: TextView
    private lateinit var passwordField: TextView
    private lateinit var confirmPasswordField: TextView
    private lateinit var registerButton: Button
    private lateinit var name: TextView
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var phoneField: EditText
    private lateinit var editIcon: ImageView
    private lateinit var profileImage: ImageView
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private var addedProfileImage: Boolean=false
    private val authViewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val backButton: Button = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            // Call the activity's method to replace the fragment
            (activity as? LoginRegisterActivity)?.onBackButtonClicked(it)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailField = view.findViewById(R.id.email_field)
        passwordField = view.findViewById(R.id.password_field)
        confirmPasswordField = view.findViewById(R.id.confirm_password_field)
        registerButton = view.findViewById(R.id.Register_button)
        name = view.findViewById(R.id.username_field)
        editIcon = view.findViewById<ImageView>(R.id.edit_profile_image_icon)
        profileImage = view.findViewById<ImageView>(R.id.profile_image_register)
        genderRadioGroup = view.findViewById(R.id.gender_radio_group)
        phoneField = view.findViewById(R.id.phone_field)
        authViewModel.authState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                val progressBar = view?.findViewById<ProgressBar>(R.id.registerProgressBar)
                progressBar?.visibility = View.GONE
                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                (activity as? LoginRegisterActivity)?.onBackButtonClicked(view)




            }
            result.onFailure {
                Toast.makeText(context, it.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                view?.findViewById<ProgressBar>(R.id.registerProgressBar)?.visibility = View.GONE
            }
        }



        setupListeners()

    }

    private fun setupListeners() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            //binding?.imageView?.setImageBitmap(bitmap)
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap)
                val uri = Uri.parse(MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, null, null))
                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
                addedProfileImage = true
            }

        }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
                addedProfileImage = true
            }
        }
        editIcon.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            val customTitle = layoutInflater.inflate(R.layout.dialog_title, null)
            val builder = AlertDialog.Builder(requireContext())
            builder.setCustomTitle(customTitle)
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> cameraLauncher?.launch(null)
                    1 -> galleryLauncher?.launch("image/*")
                }
            }
            builder.show()
        }
        val privacyText= view?.findViewById<TextView>(R.id.privacy_text)
        privacyText?.setOnClickListener {
            PrivacyPolicyDialogFragment().show(parentFragmentManager, "PrivacyPolicyDialog")
        }
        registerButton.setOnClickListener {

            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()
            val privacyPolicyCheckbox = view?.findViewById<androidx.appcompat.widget.AppCompatCheckBox>(R.id.privacy_checkbox)
            val username = name.text.toString()
            val bitmap: Bitmap?
            val progressBar = view?.findViewById<ProgressBar>(R.id.registerProgressBar)

            if(addedProfileImage){
                bitmap  = (profileImage.drawable as BitmapDrawable).bitmap
            }
            else{
                bitmap= null
            }
            if (privacyPolicyCheckbox != null) {
                if (!privacyPolicyCheckbox.isChecked) {
                    Toast.makeText(context, "Please confirm that you have read and agree to our Privacy Policy", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            if (password != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val emailPattern = Patterns.EMAIL_ADDRESS
            if (!emailPattern.matcher(email).matches()) {
                Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usernamePattern = Regex("^[a-zA-Z0-9_]{3,}$")
            if (!usernamePattern.matches(username)) {
                Toast.makeText(context, "Username must be at least 3 characters and contain only letters, numbers, and underscores", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val phone = phoneField.text.toString()

            val selectedGenderId = genderRadioGroup.checkedRadioButtonId
            val gender = when(selectedGenderId) {
                R.id.radio_male -> "male"
                R.id.radio_female -> "female"
                R.id.radio_other -> "other"
                else -> ""
            }
            progressBar?.visibility = View.VISIBLE
            authViewModel.register(requireContext(), username, email, password, bitmap, gender, phone)

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    override fun onResume() {
        super.onResume()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }
}