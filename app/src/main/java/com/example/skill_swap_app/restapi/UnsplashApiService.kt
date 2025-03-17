package com.example.skill_swap_app.restapi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface UnsplashApiService {

    @GET("search/photos")
    @Headers("Authorization: Client-ID wvkCDK2QL0_HjAKf6ByReLYJ7n_-lrJ2Lmn96t4cb98")
    fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Call<UnsplashSearchResponse>
}
