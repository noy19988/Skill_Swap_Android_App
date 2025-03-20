package com.example.skill_swap_app.network

import com.example.skill_swap_app.model.CourseResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UdemyApiService {

    @GET("courses")
    suspend fun getCourses(
        @Header("Authorization") auth: String,
        @Query("search") query: String
    ): CourseResponse
}
