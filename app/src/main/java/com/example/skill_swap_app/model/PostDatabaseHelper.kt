package com.example.skill_swap_app.model

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

@Database(entities = [Post::class], version = 8, exportSchema = false) // ✅ עדכון לגרסה 8
@TypeConverters(Converters::class)
abstract class PostDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var INSTANCE: PostDatabase? = null

        fun getDatabase(context: Context): PostDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PostDatabase::class.java,
                    "skill_swap_post_db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8) // ✅ עדכון לגרסה 8
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE posts ADD COLUMN firestoreId TEXT")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE posts ADD COLUMN favoritedByUsers TEXT NOT NULL DEFAULT '[]'")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS posts")
                database.execSQL(
                    """
                    CREATE TABLE posts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        description TEXT NOT NULL,
                        skillLevel TEXT NOT NULL,
                        phoneNumber TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        userId INTEGER NOT NULL,
                        firestoreId TEXT,
                        favoritedByUsers TEXT NOT NULL DEFAULT '[]'
                    )
                    """.trimIndent()
                )
            }
        }

        // ✅ מחיקה מחדש בגרסה 8
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DELETE FROM posts") // מחיקה חוזרת מ-Room
            }
        }
    }
}
