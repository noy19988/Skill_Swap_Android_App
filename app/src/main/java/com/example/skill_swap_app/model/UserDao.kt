package com.example.skill_swap_app.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE users SET username = :username, phone = :phone WHERE id = :id")
    suspend fun updateUser(username: String, phone: String, id: Int)

    @Query("UPDATE users SET profileImageUrl = :profileImageUrl WHERE id = :id")
    suspend fun updateUserProfileImage(id: Int, profileImageUrl: String)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Int)

    @Query("UPDATE users SET username = :username, phone = :phone, profileImageUrl = :imageUrl WHERE id = :userId")
    suspend fun updateUserProfile(userId: Int, username: String, phone: String, imageUrl: String)


}
