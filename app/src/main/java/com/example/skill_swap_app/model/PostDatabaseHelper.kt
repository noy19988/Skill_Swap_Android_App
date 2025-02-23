package com.example.skill_swap_app.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Post::class], version = 3, exportSchema = false)  // עדכון גרסה ל-3
abstract class PostDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var INSTANCE: PostDatabase? = null

        // פונקציה לקבלת האינסטנס של ה-DB
        fun getDatabase(context: Context): PostDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PostDatabase::class.java,
                    "skill_swap_post_db"
                )
                    .fallbackToDestructiveMigration()  // אם הסכימה השתנתה, נמחק את הנתונים הישנים
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
