package com.example.skill_swap_app.restapi

data class Photo(
    val id: String,
    val urls: Urls
)

data class Urls(
    val regular: String
)
