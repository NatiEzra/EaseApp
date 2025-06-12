package com.example.easeapp.model.RetrofitProvider

import TokenAuthenticator
import android.content.Context
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.RetrofitProvider.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    private const val BASE_URL = "https://ease.cs.colman.ac.il/"
    fun provideRetrofit(context: Context): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context, AuthRepository.shared))
            .authenticator(TokenAuthenticator(context, AuthRepository.shared))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
