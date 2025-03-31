package com.example.easeapp.model.requests

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface UserApi {
    @GET("/api/users/profile")
    fun getUserProfile(
        @Header("Authorization") token: String,
        @Query("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 5
    ): Call<UserProfileResponse>

}

object RetrofitClientUser {
    private const val BASE_URL = "http://10.0.2.2:3000"

    val userApi: UserApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }

}