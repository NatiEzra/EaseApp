package com.example.ease.repositories

import android.content.Context
import android.graphics.Bitmap
import com.example.ease.model.local.UserEntity
import com.example.ease.model.local.AppDatabase
import com.example.easeapp.model.requests.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import com.example.easeapp.model.RetrofitProvider.RetrofitProvider

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
            AuthApiClient.create(context).registerUser(usernamePart, emailPart, passwordPart, multipartBody)
        } else {
            val emptyRequestFile = "".toRequestBody("text/plain".toMediaTypeOrNull())
            val emptyPart = MultipartBody.Part.createFormData("profilePicture", "", emptyRequestFile)
            AuthApiClient.create(context).registerUser(usernamePart, emailPart, passwordPart, emptyPart)
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
        AuthApiClient.create(context).login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // שמירה של הטוקנים (access ו-refresh) מתוך הגוף של התשובה
                    saveAccessToken(context, loginResponse.accessToken)
                    saveRefreshToken(context, loginResponse.refreshToken)

                    val userEntity = UserEntity(
                        _id = loginResponse._id,
                        email = loginResponse.email,
                        name = loginResponse.username,
                        profileImageUrl = loginResponse.profilePicture,
                        accessToken = loginResponse.accessToken,
                        phoneNumber = loginResponse.phoneNumber,
                        dateOfBirth = loginResponse.dateOfBirth,
                        gender = loginResponse.gender
                    )

                    GlobalScope.launch {
                        AppDatabase.getInstance(context).userDao().insert(userEntity)
                    }

                    onComplete(true, null, loginResponse)
                } else {
                    val errorBody = response.errorBody()?.string()
                    onComplete(false, errorBody ?: "Login failed", null)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onComplete(false, t.message ?: "Network error", null)
            }
        })
    }

    // שמירת טוקנים (גוף רגיל - לא Set-Cookie)
    fun saveAccessToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("access_token", token).apply()
    }

    fun saveRefreshToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("refresh_token", token).apply()
    }

    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("access_token", null)
    }

    fun getRefreshToken(context: Context): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("refresh_token", null)
    }

    fun signOut(context: Context, onComplete: (Boolean, String?) -> Unit) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("access_token").remove("refresh_token").apply()
        onComplete(true, null)
    }

    fun isUserLoggedIn(context: Context): Boolean {
        return getAccessToken(context) != null
    }

    suspend fun refreshAccessToken(context: Context): String {
        val refreshToken = getRefreshToken(context) ?: return ""

        try {
            val retrofit = RetrofitProvider.provideRetrofit(context)


            val userApi = retrofit.create(UserApi::class.java)
            val response = userApi.refreshToken(RefreshRequest(refreshToken)).execute()

            return if (response.isSuccessful) {
                val newToken = response.body()?.accessToken ?: ""
                saveAccessToken(context, newToken)
                newToken
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}
