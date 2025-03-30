package com.example.ease.model

import android.graphics.Bitmap
import android.util.Log
import com.example.ease.repositories.AuthRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


data class Post(
    val postId: String,
    val profileName: String,
    var ProfileImage: String,
    var textPost: String,
    var imagePost: String,
    var date: java.util.Date,

    )

class PostModel  private constructor(){
    val db= Firebase.firestore
    @Volatile
    var posts: MutableList<Post> = mutableListOf()
    var userServer= User.shared
    val cloudinaryModel= CloudinaryModel()
    val authServer= AuthRepository.shared

    companion object{
        val shared =PostModel()
    }
    init {
        getPosts { retrievedPosts ->
            posts = retrievedPosts
        }
        /*
        var postsSize=posts.size
        for (i  in 0..5){
            val post=Post(  "postId$i","profileName$i", "ProfileImage$i", "textPost$i", "imagePost$i")
            posts.add(post)
        }*/


    }
    fun addPost(email: String, image: Bitmap?, postText: String, onComplete: (Boolean, String?) -> Unit) {
        val post = hashMapOf(
            "email" to email,
            "postText" to postText,
            "date" to System.currentTimeMillis(),
            "imagePost" to ""
        )

        db.collection("posts")
            .add(post)
            .addOnSuccessListener { documentReference ->
                image?.let {
                    Log.d("Firestore", "Image upload started ofek")
                    uploadImageToCloudinary(it, documentReference.id, { uri ->
                        Log.d("Firestore", "Image upload completed ofek")
                        if (!uri.isNullOrBlank()) {
                            db.collection("posts").document(documentReference.id)
                                .update("imagePost", uri)
                                .addOnSuccessListener {
                                    onComplete(true, null)
                                }
                                .addOnFailureListener { e ->
                                    onComplete(false, e.localizedMessage)
                                }
                        } else {
                            Log.d("Firestore", "Image upload failed -ofek")
                            onComplete(false, "Image upload failed")
                        }
                    }, { error ->
                        onComplete(false, error)
                    })
                } ?: onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding post ofek", e)
                onComplete(false, e.localizedMessage)
            }
    }
    fun uploadImageToCloudinary(bitmap: Bitmap, name : String, onSuccess: (String?) -> Unit, onError: (String?) -> Unit) {
        cloudinaryModel.uploadImage(bitmap, name, onSuccess, onError)
    }
    fun getPosts(onComplete: (MutableList<Post>) -> Unit) {

        var username : String ="";


        db.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val posts : MutableList<Post> = mutableListOf()
                var pendingCallbacks = documents.size() // Number of documents to process

                if (pendingCallbacks == 0) {
                    // If there are no documents, complete immediately
                    onComplete(posts)
                    return@addOnSuccessListener
                }
                for (document in documents) {
                    val email = document.getString("email") ?: "email"
                    userServer.getUserByEmail(email) { user ->
                        val post = Post(
                            postId = document.id,
                            profileName = user?.get("name") as? String ?: "name",
                            ProfileImage=user?.get("image") as? String ?: "image",
                            textPost = document.getString("postText") ?: "post",
                            imagePost = document.getString("imagePost") ?: "",
                            date = java.util.Date(document.getLong("date") ?: 0)
                        )
                        posts.add(post)
                        pendingCallbacks--

                        // When all callbacks are complete, sort and return the list
                        if (pendingCallbacks == 0) {
                            posts.sortByDescending { it.date }
                            Log.d("Firestore", "Successfully fetched ${posts.size} posts.")
                            onComplete(posts)
                        }
                    }



                    /*val post = Post(
                        postId=document.id,
                        profileName = username,
                        ProfileImage = "image",
                        textPost = document.getString("postText") ?: "post",
                        imagePost = "image",
                        date=java.util.Date(document.getLong("date") ?: 0)
                        //date = document.getData()["date"] as java.util.Date
                    )
                    posts.add(post)

                     */
                }
                posts.sortByDescending { it.date }
                Log.d("Firestore", "Successfully fetched ${posts.size} posts.")
                onComplete(posts)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching posts", e)
                onComplete(mutableListOf()) // Return an empty list on failure
            }
    }
    fun getMyposts(onComplete: (MutableList<Post>) -> Unit) {
        var email=authServer.getCurrentUserEmail();

        db.collection("posts")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                val posts : MutableList<Post> = mutableListOf()
                var pendingCallbacks = documents.size() // Number of documents to process

                if (pendingCallbacks == 0) {
                    // If there are no documents, complete immediately
                    onComplete(posts)
                    return@addOnSuccessListener
                }
                for (document in documents) {
                    userServer.getUserByEmail(email) { user ->
                        val post = Post(
                            postId = document.id,
                            profileName = user?.get("name") as? String ?: "name",
                            ProfileImage=user?.get("image") as? String ?: "image",
                            textPost = document.getString("postText") ?: "post",
                            imagePost = document.getString("imagePost") ?: "",
                            date = java.util.Date(document.getLong("date") ?: 0)
                        )
                        posts.add(post)
                        pendingCallbacks--

                        // When all callbacks are complete, sort and return the list
                        if (pendingCallbacks == 0) {
                            posts.sortByDescending { it.date }
                            Log.d("Firestore", "Successfully fetched ${posts.size} posts.")
                            onComplete(posts)
                        }
                    }

                }
                posts.sortByDescending { it.date }
                Log.d("Firestore", "Successfully fetched ${posts.size} posts.")
                onComplete(posts)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching posts", e)
                onComplete(mutableListOf()) // Return an empty list on failure
            }


    }
    //get post by id
    fun getPostById(postId: String, onComplete: (Post?) -> Unit) {
        db.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val email = document.getString("email") ?: "email"
                    userServer.getUserByEmail(email) { user ->
                        val post = Post(
                            postId = document.id,
                            profileName = user?.get("name") as? String ?: "name",
                            ProfileImage=user?.get("image") as? String ?: "image",
                            textPost = document.getString("postText") ?: "post",
                            imagePost = document.getString("imagePost") ?: "",
                            date = java.util.Date(document.getLong("date") ?: 0)
                        )
                        onComplete(post)
                    }
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting post by ID", e)
                onComplete(null)
            }
    }
    fun editPost(postId: String, image: Bitmap?, postText: String, onComplete: (Boolean, String?) -> Unit) {
        val post = hashMapOf(
            "postText" to postText,
            "imagePost" to ""
        )

        db.collection("posts").document(postId)
            .update(post as Map<String, Any>)
            .addOnSuccessListener {
                image?.let {
                    Log.d("Firestore", "Image upload started")
                    uploadImageToCloudinary(it, postId, { uri ->
                        Log.d("Firestore", "Image upload completed")
                        if (!uri.isNullOrBlank()) {
                            db.collection("posts").document(postId)
                                .update("imagePost", uri)
                                .addOnSuccessListener {
                                    onComplete(true, null)
                                }
                                .addOnFailureListener { e ->
                                    onComplete(false, e.localizedMessage)
                                }
                        } else {
                            Log.d("Firestore", "Image upload failed")
                            onComplete(false, "Image upload failed")
                        }
                    }, { error ->
                        onComplete(false, error)
                    })
                } ?: onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error editing post", e)
                onComplete(false, e.localizedMessage)
            }
    }
    fun deletePost(postId: String, onComplete: (Boolean, String?) -> Unit) {
        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting post", e)
                onComplete(false, e.localizedMessage)
            }
    }
}
