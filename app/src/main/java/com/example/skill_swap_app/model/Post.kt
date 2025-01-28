package com.example.skill_swap_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,       // תיאור הפוסט
    val skillLevel: String,        // רמת מיומנות (סביר, טוב, מומחה)
    val phoneNumber: String,       // מספר הטלפון של המשתמש
    val imageUrl: String, // כתובת התמונה (או נתיב התמונה)
    var isFavorite: Boolean = false
)
