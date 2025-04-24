package com.example.easeapp.repositories

import android.content.Context
import android.util.Log
import com.example.ease.model.local.AppDatabase
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.requests.AppointmentDetails
import com.example.easeapp.model.requests.AppointmentRequest
import com.example.easeapp.model.requests.AppointmentResponse
import com.example.easeapp.model.requests.ClosestAppointmentResponse
import com.example.easeapp.model.requests.RetrofitClientAppointments
import com.example.easeapp.model.requests.RoleRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentRepository {
    companion object {
        val shared = AppointmentRepository()
    }



    fun createAppointment(
        context: Context,
        request: AppointmentRequest,
        onComplete: (Boolean, AppointmentDetails?, String?) -> Unit
    ) {
        var token= AuthRepository.shared.getAccessToken(context) ?: return onComplete(false, null, "No token found")
        token = token.replace("refreshToken=", "")
        token = token.replace(";", "")
        RetrofitClientAppointments.appointmentsApi.createAppointment("Bearer $token", request)
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
        RetrofitClientAppointments.appointmentsApi.getClosestAppointmentByDoctor("Bearer $token", doctorId)
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

    fun cancelAppointment(
        context: Context,
        appointmentId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        var token = AuthRepository.shared.getAccessToken(context) ?: return onComplete(false, "No token found")

        token = token.replace("refreshToken=", "")
        token = token.replace(";", "")

        RetrofitClientAppointments.appointmentsApi
            .cancelAppointment("Bearer $token", appointmentId, RoleRequest("patient"))
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

    suspend fun getUpcomingAppointmentForPatient(context: Context): AppointmentDetails? {
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
                val response = RetrofitClientAppointments.appointmentsApi
                    .getSessionsByPatientId(bearerToken, user._id)
                    .execute()

                Log.d("AppointmentDebug", "Response success: ${response.isSuccessful}")

                if (!response.isSuccessful) {
                    val error = response.errorBody()?.string()
                    Log.e("AppointmentDebug", "API error: $error")
                    throw Exception(error ?: "API error")
                }

                val body = response.body()
                Log.d("AppointmentDebug", "API response body: $body")

                return@withContext body?.sessions?.find { it.status == "confirmed" || it.status == "pending" }
            } catch (e: Exception) {
                Log.e("AppointmentDebug", "Retrofit call failed: ${e.message}", e)
                throw e
            }
        }
    }


}