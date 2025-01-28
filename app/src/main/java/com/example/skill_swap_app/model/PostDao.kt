package com.example.skill_swap_app.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete


@Dao
interface PostDao {
    @Insert
    suspend fun insertPost(post: Post)

    @Query("SELECT * FROM posts")
    suspend fun getAllPosts(): List<Post>

    @Query("SELECT * FROM posts WHERE skillLevel = :skillLevel")  // סינון לפי רמת מיומנות
    suspend fun getPostsBySkillLevel(skillLevel: String): List<Post>

    @Query("SELECT * FROM posts WHERE userId = :userId")
    suspend fun getPostsByUserId(userId: Int): List<Post>

    @Update
    suspend fun updatePost(post: Post)

    @Query("SELECT * FROM posts WHERE isFavorite = 1")
    suspend fun getFavoritePosts(): List<Post>

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Int)
}

