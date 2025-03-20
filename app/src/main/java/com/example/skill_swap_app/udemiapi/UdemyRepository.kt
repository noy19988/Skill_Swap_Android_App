package com.example.skill_swap_app.repository

import android.content.Context
import android.util.Log
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.CourseResponse
import com.example.skill_swap_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Base64


class UdemyRepository(private val context: Context) {

    private val apiService = RetrofitClient.getUdemyApiService(context)

    suspend fun fetchCourses(searchQuery: String): CourseResponse? {
        return try {
            val clientId = context.getString(R.string.udemy_client_id)
            val clientSecret = context.getString(R.string.udemy_client_secret)
            val credentials = "$clientId:$clientSecret"
            val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

            Log.d("UdemyRepository", "Fetching courses for: $searchQuery with Auth Header: $authHeader")

            val response = withContext(Dispatchers.IO) {
                apiService.getCourses(authHeader, searchQuery)
            }

            Log.d("UdemyRepository", "Fetched ${response.results.size} courses")
            response
        } catch (e: Exception) {
            Log.e("UdemyRepository", "Error fetching courses: ${e.message}", e)
            null
        }
    }


}
