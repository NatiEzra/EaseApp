package com.example.easeapp.model.requests

import com.example.ease.model.User
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface appointmentsApi {
    @GET("/api/schedule/{doctorId}")
    fun getAppointmentDetails(
        @Header("Authorization") token: String,
        @Path("doctorId") doctorId: String
    ): Call<ScheduleResponse>


}

object RetrofitClientAppointments {
    private const val BASE_URL = "http://10.0.2.2:2999"

    val appointmentsApi:   appointmentsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(appointmentsApi::class.java)
    }

}