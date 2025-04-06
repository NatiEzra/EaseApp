package com.example.ease.repositories

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.ease.model.local.UserEntity
import com.example.ease.model.local.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.example.ease.model.UserRepository
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

data class RefreshRequest(
    val refreshToken: String
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)

class AuthRepository {
    companion object {
        val shared = AuthRepository()
    }

    fun registerUser(
        context: Context,
        username: String,
        email: String,
        password: String,
        bitmap: Bitmap?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val imageFile = bitmap?.let { bitmapToFile(it, context) }
        val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val passwordPart = password.toRequestBody("text/plain".toMediaTypeOrNull())

        val call = if (imageFile != null) {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("profilePicture", imageFile.name, requestFile)
            RetrofitClient.authApi.registerUser(usernamePart, emailPart, passwordPart, multipartBody)
        } else {
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

    fun loginUser(
        context: Context,
        email: String,
        password: String,
        onComplete: (Boolean, String?, Any?) -> Unit
    ) {
        val request = LoginRequest(email, password)
        RetrofitClient.authApi.login(request).enqueue(object : Callback<LoginResponse> {
            var callbackCalled = false

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val setCookieHeaders = response.headers()["Set-Cookie"]
                    setCookieHeaders?.let {
                        saveRefreshTokenLocally(context, it)
                        saveAccessTokenLocally(context, it)
                    }
                    val loginResponse = response.body()!!
                    val accessToken = loginResponse.accessToken
                    if (accessToken.isNotEmpty()) {
                        val userEntity = UserEntity(
                            _id = loginResponse._id,
                            email = loginResponse.email,
                            name = loginResponse.username,
                            profileImageUrl = loginResponse.profilePicture,
                            accessToken = accessToken,
                            phoneNumber = loginResponse.phoneNumber,
                            dateOfBirth = loginResponse.dateOfBirth,
                            gender = loginResponse.gender
                        )
                        GlobalScope.launch {
                            AppDatabase.getInstance(context).userDao().insert(userEntity)
                        }
                        if (!callbackCalled) {
                            onComplete(true, null, loginResponse)
                            callbackCalled = true
                        }
                        return
                    } else {
                        if (!callbackCalled) {
                            onComplete(false, "Invalid login response", null)
                            callbackCalled = true
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (!callbackCalled) {
                        onComplete(false, errorBody ?: "Login failed", null)
                        callbackCalled = true
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                if (!callbackCalled) {
                    onComplete(false, t.message ?: "Network error", null)
                    callbackCalled = true
                }
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

    fun signOut(context: Context, onComplete: (Boolean, String?) -> Unit) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("access_token_cookie").remove("refresh_token_cookie").apply()
        onComplete(true, null)
    }

    fun isUserLoggedIn(context: Context): Boolean = getAccessToken(context) != null

    suspend fun refreshAccessToken(context: Context): String {
        val refreshToken = getRefreshToken(context) ?: return ""
        return try {
            val call = RetrofitClient.authApi.refreshToken(RefreshRequest(refreshToken))
            val response = call.execute()
            if (response.isSuccessful && response.body() != null) {
                val newAccessToken = response.body()!!.accessToken
                saveAccessTokenLocally(context, newAccessToken)
                newAccessToken
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
