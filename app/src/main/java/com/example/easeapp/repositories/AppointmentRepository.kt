package com.example.easeapp.repositories

import android.content.Context
import android.util.Log
import com.example.ease.model.UserRepository
import com.example.ease.model.local.AppDatabase
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.requests.AppointmentDetails
import com.example.easeapp.model.requests.AppointmentRequest
import com.example.easeapp.model.requests.AppointmentResponse
import com.example.easeapp.model.requests.ClosestAppointmentResponse
import com.example.easeapp.model.requests.RoleRequest
import com.example.easeapp.model.requests.UpdateAppointmentRequest
import com.example.easeapp.model.requests.appointmentsApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentRepository {
    companion object {
        val shared = AppointmentRepository()
    }
    fun approveAppointment( context: Context, appointmentId: String, onComplete: (Boolean, String?) -> Unit){
        var token = AuthRepository.shared.getAccessToken(context) ?: return onComplete(false, "No token found")

        token = token.replace("refreshToken=", "")
        token = token.replace(";", "")

        val body = UpdateAppointmentRequest(status = "confirmed")

        // 3) call the API
        appointmentsApiClient.create(context).updateAppointment(appointmentId, body)
            .enqueue(object : Callback<AppointmentResponse> {
                override fun onResponse(
                    call: Call<AppointmentResponse>,
                    response: Response<AppointmentResponse>
                ) {
                    if (response.isSuccessful) {
                        onComplete(true, response.body()?.appointment?._id)
                    } else {
                        onComplete(false, null)
                    }
                }

                override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                    onComplete(false, t.message)
                }
            })
    }



    fun createAppointment(
        context: Context,
        request: AppointmentRequest,
        onComplete: (Boolean, AppointmentDetails?, String?) -> Unit
    ) {
        var token= AuthRepository.shared.getAccessToken(context) ?: return onComplete(false, null, "No token found")
        token = token.replace("refreshToken=", "")
        token = token.replace(";", "")
        appointmentsApiClient.create(context).createAppointment(request)
            .enqueue(object : Callback<AppointmentResponse> {
                override fun onResponse(call: Call<AppointmentResponse>, response: Response<AppointmentResponse>) {
                    if (response.isSuccessful) {
                        onComplete(true, response.body()?.appointment, null)
                    } else {
                        onComplete(false, null, response.errorBody()?.string() ?: "Unknown error")
                    }
                }

                override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                    onComplete(false, null, t.message ?: "Network error")
                }
            })
    }


    fun getClosestAppointment(
        context: Context,
        doctorId: String,
        onComplete: (Boolean, String?, String?) -> Unit
    ) {
        var token= AuthRepository.shared.getAccessToken(context) ?: return onComplete(false, null, "No token found")
        token = token.replace("refreshToken=", "")
        token = token.replace(";", "")
        appointmentsApiClient.create(context).getClosestAppointmentByDoctor( doctorId)
            .enqueue(object : Callback<ClosestAppointmentResponse> {
                override fun onResponse(
                    call: Call<ClosestAppointmentResponse>,
                    response: Response<ClosestAppointmentResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val appointmentDate = response.body()?.closestAppointmentDate
                        onComplete(true, appointmentDate, null)
                    } else {
                        onComplete(false, null, response.errorBody()?.string() ?: "No available slots")
                    }
                }

                override fun onFailure(call: Call<ClosestAppointmentResponse>, t: Throwable) {
                    onComplete(false, null, t.message ?: "Network error")
                }
            })
    }

    fun deleteAppointment(
        context: Context,
        appointmentId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        var token = AuthRepository.shared.getAccessToken(context) ?: return onComplete(false, "No token found")

        token = token.replace("refreshToken=", "")
        token = token.replace(";", "")

        appointmentsApiClient.create(context)
            .cancelAppointment( appointmentId, RoleRequest("patient"))
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        onComplete(true, null)
                    } else {
                        val error = response.errorBody()?.string() ?: "Unknown error"
                        onComplete(false, error)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    onComplete(false, t.message ?: "Network failure")
                }
            })
    }

    fun cancelAppointment(
        context: Context,
        appointmentId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        var token = AuthRepository.shared.getAccessToken(context) ?: return onComplete(false, "No token found")

        token = token.replace("refreshToken=", "")
        token = token.replace(";", "")

        val body = UpdateAppointmentRequest(status = "canceled")

        // 3) call the API
        appointmentsApiClient.create(context)
            .updateAppointment(appointmentId, body)
            .enqueue(object : Callback<AppointmentResponse> {
                override fun onResponse(
                    call: Call<AppointmentResponse>,
                    response: Response<AppointmentResponse>
                ) {
                    if (response.isSuccessful) {
                        onComplete(true, response.body()?.appointment?._id)
                    } else {
                        onComplete(false, null)
                    }
                }

                override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                    onComplete(false, t.message)
                }
            })
    }

    suspend fun getUpcomingAppointmentForPatient(context: Context): MutableList<AppointmentDetails>? {
        return withContext(Dispatchers.IO) {
            // --- auth setup omitted for brevity; keep your token+DB code here ---
            val tokenRaw = AuthRepository.shared.getAccessToken(context)
                ?: throw Exception("No token")
            val bearer = "Bearer " + tokenRaw
                .replace("refreshToken=", "")
                .replace(";", "")

            val user = AppDatabase.getInstance(context)
                .userDao()
                .getCurrentUser()
                ?: throw Exception("No user")

            val response = appointmentsApiClient.create(context)
                .getSessionsByPatientId(user._id)
                .execute()

            if (!response.isSuccessful) {
                throw Exception(response.errorBody()?.string() ?: "API error")
            }

            val sessions = response.body()?.sessions.orEmpty()

            // 3) Sequentially await each doctor‐profile load:
            for (session in sessions) {
                // this suspend‐call will block this coroutine until the callback resumes
                val doctorId= session.doctorId ?:continue;
                val profile = UserRepository
                    .getInstance(context)
                    .fetchUserProfile(context, doctorId)
                session.doctorName = profile.user.username
            }
            val details = mutableListOf<AppointmentDetails>()
            for (session in sessions) {
                if (session.status == "confirmed" || session.status == "pending") {
                    details.add(session)
                }
            }
            return@withContext details
            // finally, return the first “confirmed” or “pending”
            //return@withContext sessions.find { it.status == "confirmed" || it.status == "pending" }
        }
    }

    suspend fun getAppointmentDetails(
        appointmentId: String
    ){

    }

    suspend fun getAllMyMeetings(context: Context): List<AppointmentDetails>? {
        return withContext(Dispatchers.IO) {
            var token = AuthRepository.shared.getAccessToken(context)
                ?: throw Exception("No token")

            token = token.replace("refreshToken=", "")
            token = token.replace(";", "")

            val bearerToken = "Bearer $token"
            Log.d("AppointmentDebug", "Token: $bearerToken")

            val user = AppDatabase.getInstance(context).userDao().getCurrentUser()
                ?: throw Exception("No user")

            Log.d("AppointmentDebug", "User ID: ${user._id}")

            try {
                Log.d("AppointmentDebug", "Calling API getSessionsByPatientId")
                val response = appointmentsApiClient.create(context)
                    .getSessionsByPatientId( user._id)
                    .execute()

                Log.d("AppointmentDebug", "Response success: ${response.isSuccessful}")

                if (!response.isSuccessful) {
                    val error = response.errorBody()?.string()
                    Log.e("AppointmentDebug", "API error: $error")
                    throw Exception(error ?: "API error")
                }

                val body = response.body()
                Log.d("AppointmentDebug", "API response body: $body")

                return@withContext body?.sessions
            } catch (e: Exception) {
                Log.e("AppointmentDebug", "Retrofit call failed: ${e.message}", e)
                throw e
            }
        }
    }


}