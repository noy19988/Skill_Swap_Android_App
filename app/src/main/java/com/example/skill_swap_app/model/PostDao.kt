package com.example.skill_swap_app.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PostDao {

    @Insert
    suspend fun insertPost(post: Post): Long // שינוי: החזרת Long (id)

    @Query("SELECT * FROM posts")
    suspend fun getAllPosts(): List<Post>

    @Query("SELECT * FROM posts WHERE skillLevel = :skillLevel")
    suspend fun getPostsBySkillLevel(skillLevel: String): List<Post>

    @Query("SELECT * FROM posts WHERE userId = :userId")
    suspend fun getPostsByUserId(userId: Int): List<Post>

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: Int): Post

    @Query("SELECT favoritedByUsers FROM posts WHERE id = :postId")
    suspend fun getFavoritedByUsers(postId: Int): List<String>

    @Query("SELECT * FROM posts WHERE favoritedByUsers LIKE '%' || :userEmail || '%'")
    suspend fun getFavoritePosts(userEmail: String): List<Post>

    @Update
    suspend fun updatePost(post: Post)

    @Query("DELETE FROM posts WHERE firestoreId = :firestoreId")
    suspend fun deletePostByFirestoreId(firestoreId: String)


    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Int)

    @Query("UPDATE posts SET description = :description, skillLevel = :skillLevel, phoneNumber = :phoneNumber, imageUrl = :imageUrl WHERE firestoreId = :firestoreId")
    suspend fun updatePostByFirestoreId(firestoreId: String, description: String, skillLevel: String, phoneNumber: String, imageUrl: String)
}

