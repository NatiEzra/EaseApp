package com.example.easeapp.repositories

import android.content.Context
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.requests.AppointmentDetails
import com.example.easeapp.model.requests.AppointmentRequest
import com.example.easeapp.model.requests.AppointmentResponse
import com.example.easeapp.model.requests.ClosestAppointmentResponse
import com.example.easeapp.model.requests.RetrofitClientAppointments
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
            .cancelAppointment("Bearer $token", appointmentId)
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



}