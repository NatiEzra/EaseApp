package com.example.easeapp.model.requests

import android.content.Context

import com.example.easeapp.model.RetrofitProvider.RetrofitProvider
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface AuthApi {
    @POST("/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @Multipart
    @POST("/auth/register")
    fun registerUser(
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part profilePicture: MultipartBody.Part
    ): Call<RegisterResponse>

    @POST("/auth/refresh")
    fun refreshToken(@Body request: RefreshRequest): Call<RefreshResponse>

    @GET("/api/users/profile")
    fun getUserProfile(
        //@Header("Authorization") token: String,
        @Query("userId") userId: String,
    ): Call<UserProfileResponse>
}

object AuthApiClient {
    fun create(context: Context): AuthApi {
        return RetrofitProvider.provideRetrofit(context).create(AuthApi::class.java)
    }
}
