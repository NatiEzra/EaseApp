package com.example.easeapp.model.requests

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ממשק Retrofit
interface AppointmentsApi {

    @GET("/api/schedule/{doctorId}/free-slots")
    fun getAvailableSlots(
        @Header("Authorization") token: String,
        @Path("doctorId") doctorId: String,
        @Query("date") date: String
    ): Call<AvailableSlotsResponse>

    @POST("/api/appointments")
    fun createAppointment(
        @Header("Authorization") token: String,
        @Body request: AppointmentRequest
    ): Call<AppointmentResponse>

    @GET("/api/schedule/{doctorId}/closest-slot")
    fun getClosestAppointmentByDoctor(
        @Header("Authorization") token: String,
        @Path("doctorId") doctorId: String
    ): Call<ClosestAppointmentResponse>

    @HTTP(method = "DELETE", path = "/api/appointments/{appointmentId}", hasBody = true)
    fun cancelAppointment(
        @Header("Authorization") token: String,
        @Path("appointmentId") appointmentId: String,
        @Body roleBody: RoleRequest
    ): Call<Void>



}

// אובייקט Retrofit
object RetrofitClientAppointments {
    private const val BASE_URL = "http://10.0.2.2:2999"
    val appointmentsApi: AppointmentsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AppointmentsApi::class.java)
    }
}



