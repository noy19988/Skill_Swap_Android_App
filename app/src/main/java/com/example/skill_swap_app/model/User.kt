package com.example.skill_swap_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var username: String,
    val email: String,
    var phone: String
) : Serializable
