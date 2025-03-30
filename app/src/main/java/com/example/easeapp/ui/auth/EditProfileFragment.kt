package com.example.ease.ui.auth

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.ease.ui.activities.MainActivity
import com.example.ease.R
import com.example.ease.model.User
import com.example.ease.model.local.AppDatabase
import com.example.ease.viewmodel.UserViewModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private var cameraLauncher: ActivityResultLauncher<Void?>? = null
private var galleryLauncher: ActivityResultLauncher<String>? = null


/**
 * A simple [Fragment] subclass.
 * Use the [editProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class editProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view= inflater.inflate(R.layout.fragment_edit_profile, container, false)
        var editIcon = view.findViewById<ImageView>(R.id.editIcon)
        var profileImage = view.findViewById<ImageView>(R.id.profileImage)
        var addedImageToProfile: Boolean = false
        var saveButton = view.findViewById<Button>(R.id.saveButton)
        val currentPassword=view.findViewById<EditText>(R.id.editProfileVerifyPassword)
        progressBar = view.findViewById(R.id.editProfileProgressBar)

        val userViewModel: UserViewModel by viewModels()
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            //binding?.imageView?.setImageBitmap(bitmap)
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap)
                val uri = Uri.parse(MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, null, null))
                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
                addedImageToProfile = true
            }

        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
                addedImageToProfile = true
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
        val editProfilePassword=view.findViewById<EditText>(R.id.editProfilePassword)
        val editProfileName=view.findViewById<EditText>(R.id.editProfileName)
        val editProfileConfirmPass=view.findViewById<EditText>(R.id.editProfileConfirmPassword)

        saveButton.setOnClickListener {

            val bitmap: Bitmap?
            if(addedImageToProfile)
            {
                 bitmap = (profileImage.drawable as BitmapDrawable).bitmap
            }
            else{
                bitmap= null
            }
            if(editProfilePassword.text.toString()!=editProfileConfirmPass.text.toString()){
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
            else if(currentPassword.text.toString().length==0){
                Toast.makeText(context, "Please enter your current password", Toast.LENGTH_SHORT).show()
            }
            else if(editProfilePassword.text.toString().length<6 && editProfilePassword.text.toString().isNotEmpty()){
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
            else {
                progressBar.visibility = View.VISIBLE
                profileImage.isDrawingCacheEnabled = true
                profileImage.buildDrawingCache()
                userViewModel.editUser(
                    currentPassword.text.toString(),
                    editProfileName.text.toString(),
                    editProfilePassword.text.toString(),
                    bitmap
                )


            }

        }

        userViewModel.editUserResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.refreshProfile()
                (activity as? MainActivity)?.homePageButtonClicked()
            }.onFailure {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
        val CancelButtonEditProfile=view.findViewById<Button>(R.id.CancelButtonEditProfile)
        CancelButtonEditProfile.setOnClickListener {
            (activity as? MainActivity)?.myProfilePageButtonClicked()
        }
        val userDao = AppDatabase.getInstance(requireContext()).userDao()
        lifecycleScope.launch {
            val user = userDao.getCurrentUser()
            val imageUrl = user?.profileImageUrl
            if (imageUrl != null){
                Picasso.get().load(imageUrl).transform(CropCircleTransformation()).into(profileImage)
            }
            else{
                profileImage.setImageResource(R.drawable.account)
            }
        }
        /*
        userServer.getProfileImage { uri ->
            if (uri != null) {
                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
            }
        }*/



        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment editProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
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