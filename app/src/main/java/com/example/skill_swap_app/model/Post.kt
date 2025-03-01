package com.example.skill_swap_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val skillLevel: String,
    val phoneNumber: String,
    val imageUrl: String,
    val userId: Int,  // 🔹 מזהה המשתמש שיצר את הפוסט
    var isFavorite: Boolean = false,
    var favoritedByUserId: Int? = null  // 🔹 מזהה המשתמש שסימן את הפוסט כפייבוריט
)
