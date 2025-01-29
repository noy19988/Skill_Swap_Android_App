package com.example.skill_swap_app.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [User::class], version = 4, exportSchema = false)  // שדרוג גרסה ל-4
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
                    .addMigrations(MIGRATION_3_4)  // הוספת מיגרציה
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // מיגרציה לגרסה 4: הוספת עמודה profileImageUrl בטבלת users
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN profileImageUrl TEXT")
            }
        }
    }
}
