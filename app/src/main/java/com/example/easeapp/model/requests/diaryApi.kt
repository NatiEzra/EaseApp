package com.example.easeapp.model.requests

import TokenAuthenticator
import android.content.Context
import retrofit2.http.DELETE
import com.example.ease.model.User
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.DiaryModel
import com.example.easeapp.model.DiaryResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface DiaryApi {
    @POST("/api/diary")
    suspend fun addDiary(
        @Body request: DiaryModel,
        @Header("Authorization") token: String
    ): Response<DiaryResponse>

    @GET("/api/diary")
    suspend fun getUserDiaries(
        @Header("Authorization") token: String,
        @Query("patientId") userId: String
    ): Response<List<DiaryModel>>

    @DELETE("/api/deleteDiary/{id}")
    suspend fun deleteDiary(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<DiaryResponse>
    @PUT("/api/diary/{id}")
    suspend fun updateDiary(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: DiaryModel
    ): Response<DiaryResponse>
}
object RetrofitClientDiary  {
//    private const val BASE_URL = "http://10.100.102.175:3000"
    private const val BASE_URL = "http://10.0.2.2:2999"


    fun create(context: Context): Retrofit {
        val client = OkHttpClient.Builder()
            .authenticator(TokenAuthenticator(context, AuthRepository.shared))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}