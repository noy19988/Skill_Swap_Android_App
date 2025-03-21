package com.example.skill_swap_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val skillLevel: String,
    val phoneNumber: String,
    val imageUrl: String,
    val userId: Int,
    val firestoreId: String? = null,

    @TypeConverters(Converters::class)
    var favoritedByUsers: List<String> = emptyList()

){
    constructor() : this(0, "", "", "", "", 0, null, emptyList())
}