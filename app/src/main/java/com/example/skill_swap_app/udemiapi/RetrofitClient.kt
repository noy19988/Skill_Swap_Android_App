package com.example.skill_swap_app.network

import android.content.Context
import android.util.Base64
import com.example.skill_swap_app.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var apiService: UdemyApiService? = null

    fun getUdemyApiService(context: Context): UdemyApiService {
        if (apiService == null) {
            val clientId = context.getString(R.string.udemy_client_id)
            val clientSecret = context.getString(R.string.udemy_client_secret)
            val credentials = "$clientId:$clientSecret"
            val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.udemy.com/api-2.0/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(UdemyApiService::class.java)
        }
        return apiService!!
    }
}
