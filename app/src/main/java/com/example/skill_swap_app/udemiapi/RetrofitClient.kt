package com.example.skill_swap_app.network

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var apiService: UdemyApiService? = null

    fun getUdemyApiService(context: Context): UdemyApiService {
        if (apiService == null) {

            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.udemy.com/api-2.0/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(UdemyApiService::class.java)
        }
        return apiService!!
    }
}
