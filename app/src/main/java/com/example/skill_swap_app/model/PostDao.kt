package com.example.skill_swap_app.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PostDao {
    @Insert
    suspend fun insertPost(post: Post)

    @Query("SELECT * FROM posts")
    suspend fun getAllPosts(): List<Post>

    @Query("SELECT * FROM posts WHERE skillLevel = :skillLevel")
    suspend fun getPostsBySkillLevel(skillLevel: String): List<Post>

    @Query("SELECT * FROM posts WHERE userId = :userId")
    suspend fun getPostsByUserId(userId: Int): List<Post>

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: Int): Post

    @Update
    suspend fun updatePost(post: Post)

    // ✅ טעינת פוסטים מועדפים לפי המשתמש שסימן אותם
    @Query("SELECT * FROM posts WHERE isFavorite = 1 AND favoritedByUserId = :userId")
    suspend fun getFavoritePosts(userId: Int): List<Post>

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Int)
}
