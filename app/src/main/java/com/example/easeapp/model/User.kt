package com.example.ease.model

import android.content.Context
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.requests.RetrofitClientUser
import com.example.easeapp.model.requests.UserApi
import com.example.easeapp.model.requests.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class User(
    val _id: String,
    val username: String,
    var profilePicture: String,
    var role: String,
)

class UserRepository(private val context: Context) {
    var auth = AuthRepository.shared
    @Volatile
    var doctors: MutableList<User> = mutableListOf()

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(context: Context): UserRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(context.applicationContext).also { INSTANCE = it }
            }
    }

    private val userApi: UserApi by lazy {
        RetrofitClientUser.create(context).create(UserApi::class.java)
    }

    fun getAllDoctors(onComplete: (MutableList<User>) -> Unit) {
        userApi.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful && response.body() != null) {
                    val allUsers = response.body()!!.toMutableList()
                    doctors = mutableListOf()
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
        onComplete: (Boolean, com.example.easeapp.model.requests.UserDetails?, String?) -> Unit
    ) {
        val authHeader = "Bearer $accessToken"
        userApi.getUserProfile(authHeader, userId, page).enqueue(object : Callback<com.example.easeapp.model.requests.UserProfileResponse> {
            override fun onResponse(
                call: Call<com.example.easeapp.model.requests.UserProfileResponse>,
                response: Response<com.example.easeapp.model.requests.UserProfileResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    onComplete(true, response.body()!!.user, null)
                } else {
                    onComplete(false, null, response.errorBody()?.string() ?: "Failed to fetch profile")
                }
            }

            override fun onFailure(call: Call<com.example.easeapp.model.requests.UserProfileResponse>, t: Throwable) {
                onComplete(false, null, t.message ?: "Network error")
            }
        })
    }
}