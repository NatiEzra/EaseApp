package com.example.easeapp.model.requests

import TokenAuthenticator
import android.content.Context
import com.example.ease.model.User
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.DiaryModel
import com.example.easeapp.model.DiaryResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface DiaryApi {
    @POST("/api/diary")
    fun addDiary(
        @Body request: DiaryModel,
        @Header("Authorization") token: String
    ): Call<DiaryResponse>
}
object RetrofitClientDiary  {
    private const val BASE_URL = "http://10.0.2.2:3000"

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