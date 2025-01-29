package com.example.skill_swap_app.restapi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object NetworkHelper {

    private const val BASE_URL = "https://api.unsplash.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(UnsplashApiService::class.java)

    // פונקציה לחיפוש תמונות מ-Unsplash
    fun getImages(query: String, apiKey: String, page: Int, callback: (List<Photo>?) -> Unit) {
        val call = apiService.searchPhotos(query, page, 30) // שינוי לקריאה המתאימה

        call.enqueue(object : Callback<UnsplashSearchResponse> { // שינוי ל-UnsplashSearchResponse
            override fun onResponse(call: Call<UnsplashSearchResponse>, response: Response<UnsplashSearchResponse>) {
                if (response.isSuccessful) {
                    val photoList = response.body()?.results // מקבל את התוצאות מה-UnsplashSearchResponse
                    callback(photoList)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<UnsplashSearchResponse>, t: Throwable) {
                callback(null)
            }
        })
    }
}
