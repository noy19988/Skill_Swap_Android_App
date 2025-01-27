package com.example.skill_swap_app.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE users SET username = :username, phone = :phone WHERE id = :id")
    suspend fun updateUser(username: String, phone: String, id: Int)
    // מחיקת משתמש לפי אובייקט User
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Int)  // מחיקה לפי מזהה המשתמש (id)
}