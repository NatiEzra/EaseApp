package com.example.ease.repositories

import android.telecom.Call
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.easeapp.model.requests.LoginRequest
import com.example.easeapp.model.requests.LoginResponse
import com.example.easeapp.model.requests.RetrofitClient
import okhttp3.Callback
import retrofit2.Response

class AuthRepository {
    companion object{
        val shared = AuthRepository()
    }
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun registerUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.localizedMessage)
                }
            }
    }

    fun authenticate(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.localizedMessage)
                }
            }
    }
    fun loginUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        val request = LoginRequest(email, password)

        RetrofitClient.instance.login(request).enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(call: retrofit2.Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    onComplete(true, null)
                } else {
                    onComplete(false, response.errorBody()?.string() ?: "Login failed")
                }
            }

            override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                onComplete(false, t.message ?: "Network error")
            }
        })
    }


    fun signOut() {
        auth.signOut()
    }
    fun changePassword(newPassword: String ,onComplete: (Boolean, String?) -> Unit){
        val user = auth.currentUser
        user?.let {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(true, null)
                    } else {
                        onComplete(false, task.exception?.localizedMessage)
                    }
                }
        }
    }


    fun isUserLoggedIn(): Boolean = currentUser != null
    fun getCurrentUserEmail(): String {
        val email: String
        if(currentUser!=null){
            email= currentUser!!.email.toString()
        }
        else{
            email=""
        }
        return email
    }
}

