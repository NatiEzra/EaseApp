package com.example.ease.model

import android.graphics.Bitmap
import android.util.Log
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.requests.RetrofitClient
import com.example.easeapp.model.requests.RetrofitClientUser
import com.example.easeapp.model.requests.RetrofitClientUser.userApi
import com.example.easeapp.model.requests.UserDetails
import com.example.easeapp.model.requests.UserProfileResponse
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
data class User(
    val _id: String,
    val username: String,
    var profilePicture: String,
    var role: String,
)
class UserRepository {
    var auth = AuthRepository.shared
    @Volatile
    var doctors: MutableList<User> = mutableListOf()

    companion object {
        val shared = UserRepository()
    }


    val db = Firebase.firestore
    val cloudinaryModel = CloudinaryModel()
    fun getAllDoctors( onComplete: (MutableList<User>) -> Unit){
        userApi.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(
                call: Call<List<User>>,
                response: Response<List<User>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    var allUsers=response.body()!!.toMutableList()
                    doctors= mutableListOf()
                    allUsers.forEach { user ->
                        if (user.role == "doctor") {
                            doctors.add(user)
                        }
                    }
                    onComplete(doctors)
                } else {
                    onComplete(mutableListOf())
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                onComplete(mutableListOf())
            }
        })


    }

    fun getUser(
        accessToken: String,
        userId: String,
        page: Int = 1,
        onComplete: (Boolean, UserDetails?, String?) -> Unit
    ) {
        val authHeader = "Bearer $accessToken"

        RetrofitClientUser.userApi.getUserProfile(authHeader, userId, page).enqueue(object :
            Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    onComplete(true, response.body()!!.user, null)
                } else {
                    onComplete(false, null, response.errorBody()?.string() ?: "Failed to fetch profile")
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                onComplete(false, null, t.message ?: "Network error")
            }
        })
    }

    fun getUserByEmail(email: String, onComplete: (Map<String, Any>?) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDocument = documents.documents[0]
                    val userData = userDocument.data
                    onComplete(userData)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                onComplete(null)
            }
    }
    fun getProfileImage(onComplete: (String?) -> Unit) {

    }

    fun editUser(currentPassword: String, name: String, password: String, image: Bitmap?, onComplete: (Boolean, String?) -> Unit) {
        val userEmail = auth.currentUser?.email
        if (userEmail != null && currentPassword.isNotEmpty()) {
            auth.authenticate(userEmail, currentPassword) { success, error ->
                if (success) {
                    db.collection("users")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val userDocument = documents.documents[0]
                                val documentReference = userDocument.reference
                                var updateCount = 0
                                var totalUpdates = 0
                                var hasError = false

                                // Count the number of updates needed
                                if (password.isNotEmpty()) totalUpdates++
                                if (name.isNotEmpty()) totalUpdates++
                                if (image != null) totalUpdates++

                                // Function to check completion
                                fun checkCompletion() {
                                    if (updateCount == totalUpdates && !hasError) {
                                        onComplete(true, null)
                                    } else if (hasError) {
                                        onComplete(false, "An error occurred while updating the profile.")
                                    }
                                }

                                // Update password if it's not empty
                                if (password.isNotEmpty()) {
                                    auth.changePassword(password) { success, error ->
                                        if (success) {
                                            Log.d("Firestore", "Password changed")
                                        } else {
                                            hasError = true
                                        }
                                        updateCount++
                                        checkCompletion()
                                    }
                                }

                                // Update name if it's not empty
                                if (name.isNotEmpty()) {
                                    documentReference.update("name", name)
                                        .addOnSuccessListener {
                                            Log.d("Firestore", "Name updated")
                                            updateCount++
                                            checkCompletion()
                                        }
                                        .addOnFailureListener { e ->
                                            hasError = true
                                            updateCount++
                                            checkCompletion()
                                        }
                                }

                                // Update image if it's not null
                                if (image != null) {
                                    val previousImageUrl = userDocument.getString("image")
                                    val uploadImage = { bitmap: Bitmap ->
                                        uploadImageToCloudinary(bitmap, auth.getCurrentUserEmail(), { uri ->
                                            if (!uri.isNullOrBlank()) {
                                                documentReference.update("image", uri)
                                                    .addOnSuccessListener {
                                                        Log.d("Firestore", "Image updated")
                                                        updateCount++
                                                        checkCompletion()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        hasError = true
                                                        updateCount++
                                                        checkCompletion()
                                                    }
                                            } else {
                                                hasError = true
                                                updateCount++
                                                checkCompletion()
                                            }
                                        }, { error ->
                                            hasError = true
                                            updateCount++
                                            checkCompletion()
                                        })
                                    }

                                    if (!previousImageUrl.isNullOrEmpty()) {
                                        cloudinaryModel.deleteImage(previousImageUrl) { deleteSuccess, deleteError ->
                                            if (deleteSuccess) {
                                                uploadImage(image)
                                            } else {
                                                hasError = true
                                                updateCount++
                                                checkCompletion()
                                            }
                                        }
                                    } else {
                                        uploadImage(image)
                                    }
                                }

                                // If no updates were needed, return early
                                if (totalUpdates == 0) {
                                    onComplete(false, "No changes were made")
                                }
                            } else {
                                onComplete(false, "User not found")
                            }
                        }
                        .addOnFailureListener { e ->
                            onComplete(false, e.localizedMessage)
                        }
                } else {
                    onComplete(false, error)
                }
            }

        } else {
            onComplete(false, "User email is null")
        }
    }


    fun uploadImageToCloudinary(
        bitmap: Bitmap,
        name: String,
        onSuccess: (String?) -> Unit,
        onError: (String?) -> Unit
    ) {
        cloudinaryModel.uploadImage(bitmap, name, onSuccess, onError)
    }
}



