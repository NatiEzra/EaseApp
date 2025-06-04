package com.example.easeapp.model.RetrofitProvider

import TokenAuthenticator
import android.content.Context
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.RetrofitProvider.AuthInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date

object RetrofitProvider {
    private const val BASE_URL = "http://10.0.2.2:2999"

    fun provideRetrofit(context: Context): Retrofit {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            // Rfc3339DateJsonAdapter parses ISO-8601 timestamps like "2025-05-21T13:00:55.034Z"
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()


        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context, AuthRepository.shared))
            .authenticator(TokenAuthenticator(context, AuthRepository.shared))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
