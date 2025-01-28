package com.example.skill_swap_app.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 2, exportSchema = false)  // שיניתי את הגרסה ל-2
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skill_swap_db"
                )
                    .fallbackToDestructiveMigration()  // אם הסכימה השתנתה, נמחק את הנתונים הישנים
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
