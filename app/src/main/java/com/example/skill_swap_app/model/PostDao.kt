package com.example.skill_swap_app.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PostDao {
    @Insert
    suspend fun insertPost(post: Post)

    @Query("SELECT * FROM posts")
    suspend fun getAllPosts(): List<Post>
}
