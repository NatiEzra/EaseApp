package com.example.ease.model

import android.graphics.Bitmap
import android.util.Log
import com.example.ease.repositories.AuthRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class User {
    var auth = AuthRepository.shared

    companion object {
        val shared = User()
    }

    val db = Firebase.firestore
    val cloudinaryModel = CloudinaryModel()
    fun createUser(name: String, email: String,bitmap: Bitmap?, onComplete: (Boolean, String?) -> Unit) {
        var imageUrl: String? = null
        if(bitmap!=null){
            uploadImageToCloudinary(bitmap, auth.getCurrentUserEmail(), { uri ->
                if (!uri.isNullOrBlank()) {
                    imageUrl = uri
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "image" to imageUrl
                    )
                    db.collection("users")
                        .add(user)
                        .addOnSuccessListener { documentReference ->
                            onComplete(true, null)
                        }
                        .addOnFailureListener { e ->
                            onComplete(false, e.localizedMessage)
                        }

                } else {
                    onComplete(false, "Error uploading image")
                }
            }, { error ->
                onComplete(false, error)
            })
        }
        else{
            val user = hashMapOf(
                "name" to name,
                "email" to email,
            )
            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    onComplete(true, null)
                }
                .addOnFailureListener { e ->
                    onComplete(false, e.localizedMessage)
                }
        }


    }

    fun getUser(onComplete: (Map<String, Any>?) -> Unit) {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDocument = documents.documents[0]
                        val user = userDocument.data
                        onComplete(user)
                    } else {
                        onComplete(null)
                    }
                }
                .addOnFailureListener { e ->
                    onComplete(null)
                }
        } else {
            onComplete(null)
        }
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
        getUser { user ->
            if (user != null) {
                val image = user["image"] as? String
                onComplete(image)
            } else {
                onComplete(null)
            }
        }
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



