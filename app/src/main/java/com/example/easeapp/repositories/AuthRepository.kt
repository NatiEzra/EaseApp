package com.example.ease.repositories

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.ease.base.MyApplication
import com.example.ease.model.User
import com.example.ease.model.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.easeapp.model.requests.LoginRequest
import com.example.easeapp.model.requests.LoginResponse
import com.example.easeapp.model.requests.RegisterResponse
import com.example.easeapp.model.requests.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AuthRepository {
    companion object{
        val shared = AuthRepository()
    }
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository= UserRepository.shared

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun registerUser(context: Context, username: String, email: String, password: String, bitmap: Bitmap?, onComplete: (Boolean, String?) -> Unit) {
        val imageFile = bitmap?.let { bitmapToFile(it, context) }

        val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val passwordPart = password.toRequestBody("text/plain".toMediaTypeOrNull())

        val call = if (imageFile != null) {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("profilePicture", imageFile.name, requestFile)

            RetrofitClient.authApi.registerUser(usernamePart, emailPart, passwordPart, multipartBody)
        } else {
            // Send an empty multipart form field with an empty filename and body
            val emptyRequestFile = "".toRequestBody("text/plain".toMediaTypeOrNull())
            val emptyPart = MultipartBody.Part.createFormData("profilePicture", "", emptyRequestFile)

            RetrofitClient.authApi.registerUser(usernamePart, emailPart, passwordPart, emptyPart)
        }

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    onComplete(true, response.body()?.message)
                } else {
                    onComplete(false, response.errorBody()?.string() ?: "Unknown error")
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                onComplete(false, t.message ?: "Network error")
            }
        })

    }

    fun bitmapToFile(bitmap: Bitmap, context: Context, fileName: String = "temp_image.jpg"): File {
        val file = File(context.cacheDir, fileName)
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
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
    fun loginUser(context: Context, email: String, password: String, onComplete: (Boolean, String?, Any?) -> Unit) {
        val request = LoginRequest(email, password)

        RetrofitClient.authApi.login(request).enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(call: retrofit2.Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {

                    val setCookieHeaders = response.headers()["Set-Cookie"]

                    setCookieHeaders?.let {
                        saveRefreshTokenLocally(context,it)
                        saveAccessTokenLocally(context,it)
                    }
                    val loginResponse = response.body()
                    val accessToken = loginResponse?.accessToken
                    val userId = loginResponse?._id
                    if (accessToken != null && userId != null) {
                        userRepository.getUser(
                            accessToken = accessToken,
                            userId = userId,
                            page = 1
                        ) { success, user, error ->
                            if (success && user != null) {
                                user.accessToken=loginResponse.accessToken;
                                onComplete(true, null, user)
                                Log.d("USER", "Username: ${user.username}, Profile Pic: ${user.profilePicture}")
                            } else {
                                Log.e("ERROR", error ?: "Unknown error")
                            }
                        }
                    }

                    onComplete(true, null, null)
                } else {
                    onComplete(false, response.errorBody()?.string() ?: "Login failed", null)
                }
            }

            override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                onComplete(false, t.message ?: "Network error", null)
            }
        })
    }


    fun saveRefreshTokenLocally(context: Context, cookie: String) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("refresh_token_cookie", cookie).apply()
    }
    fun saveAccessTokenLocally(context: Context, cookie: String) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("access_token_cookie", cookie).apply()
    }

    fun getRefreshToken(context: Context): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("refresh_token_cookie", null)
    }
    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("access_token_cookie", null)
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

