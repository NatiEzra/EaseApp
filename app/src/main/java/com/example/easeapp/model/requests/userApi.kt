package com.example.easeapp.model.requests

import android.content.Context
import com.example.ease.model.User
import com.example.easeapp.model.RetrofitProvider.RetrofitProvider
import retrofit2.Call
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface UserApi {
    @GET("/api/users/profile")
    fun getUserProfile(
       // @Header("Authorization") token: String,
        @Query("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 5
    ): Call<UserProfileResponse>

    @GET("/api/users/")
    fun getAllUsers(): Call<List<User>>

    @Multipart
    @PUT("/api/users/profile")
    fun updateUserProfile(
       // @Header("Authorization") token: String,
        @Query("userId") userId: String,
        @Part("username") username: RequestBody,
        @Part("phoneNumber") phoneNumber: RequestBody,
        @Part("dateOfBirth") dateOfBirth: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part profilePicture: MultipartBody.Part?
    ): Call<UpdateProfileResponse>

    @POST("/auth/refresh")
    fun refreshToken(
        @Body request: RefreshRequest
    ): Call<RefreshResponse>
}

object UserApiClient {
    fun create(context: Context): UserApi {
        return RetrofitProvider.provideRetrofit(context).create(UserApi::class.java)
    }
}