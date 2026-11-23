package com.example.property_listing_system.data.network.client

import com.example.property_listing_system.data.network.api.MockPropertyApiInterceptor
import com.example.property_listing_system.data.network.api.PropertyApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.propertylistingsystem.com/"

    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // Member 6: Mock interceptor enables full offline testing for CAT presentations
    // while executing the real OkHttp / Retrofit HTTP processing pipeline.
    private val mockInterceptor: MockPropertyApiInterceptor by lazy {
        MockPropertyApiInterceptor(gson = gson, enabled = true)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(mockInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: PropertyApiService by lazy {
        retrofit.create(PropertyApiService::class.java)
    }
}
