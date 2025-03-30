package com.example.ease.ui.posts

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.ease.ui.activities.MainActivity
import com.example.ease.R
import com.example.ease.model.PostModel
import com.example.ease.model.User
import com.example.ease.model.local.AppDatabase
import com.example.ease.viewmodel.PostViewModel
import com.example.ease.viewmodel.UserViewModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [addPostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class addPostFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var isEdit: Boolean = false
    private var postId: String? = null


    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarFragment: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            isEdit= it.getBoolean(ARG_IS_EDIT, false)
            postId= it.getString(ARG_POST_ID)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view= inflater.inflate(R.layout.fragment_add_post, container, false)
        var profileName=view.findViewById<TextView>(R.id.profileName)
        progressBar = view.findViewById(R.id.profileImageAddPostProgressBar)
        var postText=view.findViewById<EditText>(R.id.addPostEditText)
        var postButton=view.findViewById<TextView>(R.id.postButton)
        val userViewModel: UserViewModel by viewModels()
//        profileName.text = (activity as? MainActivity)?.getUserName()
        progressBarFragment=view.findViewById<ProgressBar>(R.id.addPostProgressBar)


//        var email = (activity as? MainActivity)?.getUserEmail().toString()
        var addMediaButton=view.findViewById<TextView>(R.id.addMediaButton)
        var postImage=view.findViewById<ImageView>(R.id.postImage)
        var profileImage=view.findViewById<ImageView>(R.id.profileImage)
        var deleteImageButton=view.findViewById<ImageButton>(R.id.deleteImageButton)
        deleteImageButton.visibility=View.GONE
        var addedImageToPost: Boolean = false
        val postViewModel: PostViewModel by viewModels()


        val userDao = AppDatabase.getInstance(requireContext()).userDao()
        var email=""
        var imageUrl : String=""
        lifecycleScope.launch {
            val user = userDao.getCurrentUser()
            profileName.text = user?.name ?: "Guest"
            email= user?.email ?: ""
            imageUrl= user?.profileImageUrl ?: ""
            progressBar.visibility = View.VISIBLE
            profileImage.visibility=View.GONE
            if (imageUrl != null && imageUrl.isNotEmpty()) {
                Picasso.get().load(imageUrl).transform(CropCircleTransformation()).into(profileImage)
                progressBar.visibility = View.GONE
                profileImage.visibility=View.VISIBLE
            }
            else{
                progressBar.visibility = View.GONE
                profileImage.visibility=View.VISIBLE
            }
        }


        postButton.setOnClickListener {
            progressBarFragment.visibility = View.VISIBLE

            var postTextString=postText.text.toString()
            if(postTextString.isNotEmpty()){
                if (addedImageToPost){


                    postImage.isDrawingCacheEnabled = true
                    postImage.buildDrawingCache()
                    val bitmap = (postImage.drawable as BitmapDrawable).bitmap

                    postViewModel.createPost(bitmap ,postTextString)


                    postText.text.clear()
                }
                else{
                    postViewModel.createPost(null ,postTextString)

                    postText.text.clear()
                }

            }
            else{
                Toast.makeText(context,"Post empty is invalid",Toast.LENGTH_LONG).show()
            }
        }

        postViewModel.postCreateState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                postText.text.clear()
                Toast.makeText(context, "Your post was shared successfully!", Toast.LENGTH_LONG).show()
                (activity as? MainActivity)?.homePageButtonClicked()
                progressBarFragment.visibility = View.GONE
            }
            result.onFailure {
                Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show()
                progressBarFragment.visibility = View.GONE
            }
        }
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if(bitmap!=null) {
                postImage.setImageBitmap(bitmap)
                addedImageToPost = true
                deleteImageButton.visibility = View.VISIBLE
            }
            //binding?.imageView?.setImageBitmap(bitmap)
        }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    val maxWidth = 200
                    val maxHeight = 200

                    val resizedBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)

                    postImage.setImageBitmap(resizedBitmap)
                    addedImageToPost = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
                deleteImageButton.visibility = View.VISIBLE
            }
        }



        deleteImageButton.setOnClickListener {
            postImage.setImageResource(0)
            addedImageToPost = false
            deleteImageButton.visibility = View.GONE
        }
            addMediaButton.setOnClickListener(){
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
//        progressBar.visibility = View.VISIBLE
//        profileImage.visibility=View.GONE
//        userServer.getProfileImage { uri ->
//            if (uri != null) {
//                Picasso.get().load(uri).transform(CropCircleTransformation()).into(profileImage)
//                progressBar.visibility = View.GONE
//                profileImage.visibility=View.VISIBLE
//            }
//            else{
//                progressBar.visibility = View.GONE
//                profileImage.visibility=View.VISIBLE
//            }
//        }



        if(isEdit){
            postButton.text = "Edit"
            postViewModel.getPostById(postId!!)
            postViewModel.singlePost.observe(viewLifecycleOwner) { post ->
                post?.let {
                    postText.setText(it.textPost)

                    if (it.imagePost.isNotEmpty()) {
                        deleteImageButton.visibility = View.VISIBLE
                        Picasso.get()
                            .load(it.imagePost)
                            .resize(200, 200)
                            .centerInside()
                            .into(postImage)

                        addedImageToPost = true
                    }
                }
            }


            postButton.setOnClickListener(){
                var postTextString=postText.text.toString()
                if(postTextString.isNotEmpty()){
                    if (addedImageToPost){
                        postImage.isDrawingCacheEnabled = true
                        postImage.buildDrawingCache()
                        val bitmap = (postImage.drawable as BitmapDrawable).bitmap
                        postViewModel.updatePost(postId?:"" ,bitmap ,postTextString)


                        postText.text.clear()
                    }
                    else{
                        postViewModel.updatePost(postId?:"",null ,postTextString)

                        postText.text.clear()
                    }

                }
                else{
                    Toast.makeText(context,"Post empty is invalid",Toast.LENGTH_LONG).show()
                }

            }


            postViewModel.postOperationState.observe(viewLifecycleOwner) { result ->
                result.onSuccess {
                    postText.text.clear()
                    Toast.makeText(context, "Your post was updated successfully!", Toast.LENGTH_LONG).show()
                    (activity as? MainActivity)?.MyPostsButtonClicked()
                }
                result.onFailure {
                    Toast.makeText(context, "Connection failed", Toast.LENGTH_LONG).show()
                }
            }


        }

        return view;
    }

        private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
            val width = bitmap.width
            val height = bitmap.height
            val aspectRatio = width.toFloat() / height.toFloat()

            val newWidth: Int
            val newHeight: Int

            if (width > height) {
                newWidth = maxWidth
                newHeight = (maxWidth / aspectRatio).toInt()
            } else {
                newHeight = maxHeight
                newWidth = (maxHeight * aspectRatio).toInt()
            }

            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment addPostFragment.
         */
        private const val ARG_IS_EDIT = "isEdit"
        private const val ARG_POST_ID = "postId"
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(isEdit: Boolean, postId: String?) =
            addPostFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_EDIT, isEdit)
                    putString(ARG_POST_ID, postId)
                }
            }
    }
}