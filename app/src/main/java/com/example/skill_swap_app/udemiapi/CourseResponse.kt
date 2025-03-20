package com.example.skill_swap_app.model

data class CourseResponse(
    val results: List<Course>
)

data class Course(
    val id: Int,
    val title: String,
    val url: String,
    val image_480x270: String,
    val price: String
)
