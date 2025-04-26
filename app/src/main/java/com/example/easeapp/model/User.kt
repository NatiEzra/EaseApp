package com.example.ease.model

import android.content.Context
import android.util.Log
import com.example.ease.model.local.AppDatabase
import com.example.ease.model.local.UserEntity
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.requests.AppointmentDetails
import com.example.easeapp.model.requests.RetrofitClient
import com.example.easeapp.model.requests.RetrofitClientUser
import com.example.easeapp.model.requests.UserApi
import com.example.easeapp.model.requests.UserProfileResponse
import com.example.easeapp.repositories.AppointmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class User(
    val _id: String,
    val username: String,
    var profilePicture: String,
    var role: String,
    var isLastDoctor: Boolean?,
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

    fun getAllDoctors(context: Context, onComplete: (MutableList<User>) -> Unit) {
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
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val meetings = AppointmentRepository
                                .shared
                                .getAllMyMeetings(context)
                                ?: emptyList()
                            val passedMeetings = mutableListOf<AppointmentDetails>()
                            for (meeting in meetings){
                                if (meeting.status=="passed"){
                                    passedMeetings.add(meeting)
                                }
                            }
                            var lastMeeting: AppointmentDetails = passedMeetings[0]
                            for (meeting in passedMeetings){
                                if (lastMeeting.appointmentDate<meeting.appointmentDate){
                                    lastMeeting=meeting
                                }
                            }
                            for (doctor in doctors) {
                                if (lastMeeting.doctorId == doctor._id) {
                                    doctor.isLastDoctor = true
                                } else {
                                    doctor.isLastDoctor = false
                                }
                            }
                            doctors.sortByDescending { it.isLastDoctor }
                            onComplete(doctors)


                        } catch (e: Exception) {}
                        onComplete(doctors)
                    }
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

    suspend fun fetchUserProfile(
        context: Context,
        doctorId: String
    ): UserProfileResponse = suspendCancellableCoroutine { cont ->
        // 1) Grab & sanitize token
        val rawToken = AuthRepository.shared.getAccessToken(context)
            ?: return@suspendCancellableCoroutine cont.resumeWithException(Exception("No token"))
        val bearer = "Bearer " + rawToken
            .replace("refreshToken=", "")
            .replace(";", "")

        // 2) Enqueue the Retrofit call
        val call: Call<UserProfileResponse> =
            RetrofitClient.authApi.getUserProfile(bearer, doctorId)

        call.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(
                call: Call<UserProfileResponse>,
                response: Response<UserProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        cont.resume(body)
                    } else {
                        cont.resumeWithException(
                            Exception("Empty response body")
                        )
                    }
                } else {
                    // Extract error message if possible
                    val errorMsg = runCatching { response.errorBody()?.string() }
                        .getOrNull()
                        ?: "Unknown API error"
                    cont.resumeWithException(Exception(errorMsg))
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                // Network or unexpected failure
                cont.resumeWithException(t)
            }
        })

        // 3) If the coroutine is cancelled, cancel the Retrofit call
        cont.invokeOnCancellation {
            call.cancel()
        }
    }
}