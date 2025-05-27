package com.example.easeapp.model.requests

import android.content.Context
import com.example.easeapp.model.RetrofitProvider.RetrofitProvider
import com.example.easeapp.model.responses.SessionsByPatientResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// ממשק Retrofit
interface AppointmentsApi {

    @GET("/api/schedule/{doctorId}/free-slots")
    fun getAvailableSlots(
      //  @Header("Authorization") token: String,
        @Path("doctorId") doctorId: String,
        @Query("date") date: String
    ): Call<AvailableSlotsResponse>

    @POST("/api/appointments")
    fun createAppointment(
       // @Header("Authorization") token: String,
        @Body request: AppointmentRequest
    ): Call<AppointmentResponse>

    @GET("/api/schedule/{doctorId}/closest-slot")
    fun getClosestAppointmentByDoctor(
      //  @Header("Authorization") token: String,
        @Path("doctorId") doctorId: String
    ): Call<ClosestAppointmentResponse>

    @HTTP(method = "DELETE", path = "/api/appointments/{appointmentId}", hasBody = true)
    fun cancelAppointment(
       // @Header("Authorization") token: String,
        @Path("appointmentId") appointmentId: String,
        @Body roleBody: RoleRequest
    ): Call<Void>

    @GET("/api/patients/{patientId}/sessions")
    fun getSessionsByPatientId(
       // @Header("Authorization") token: String,
        @Path("patientId") patientId: String
    ): Call<SessionsByPatientResponse>

    @PUT("/api/appointments/{appointmentId}")
    fun updateAppointment(
      //  @Header("Authorization") auth: String,
        @Path("appointmentId") appointmentId: String,
        @Body request: UpdateAppointmentRequest
    ): Call<AppointmentResponse>

    @GET("/api/appointments/{appointmentId}")
    fun getAppointmentDetails(
        @Header("Authorization") token: String,
        @Path("appointmentId") appointmentId: String
    ): Call<AppointmentResponse>






}

// אובייקט Retrofit
object appointmentsApiClient {
    fun create(context: Context): AppointmentsApi {
        return RetrofitProvider.provideRetrofit(context).create(AppointmentsApi::class.java)
    }
}



