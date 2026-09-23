package com.example.edicalories.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Meal::class, DayTotal::class, WeightEntry::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun weightDao(): WeightDao

    companion object {
        private const val DATABASE_NAME: String = "meals.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE meals ADD COLUMN minutesOfDay INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS day_totals (" +
                        "epochDay INTEGER NOT NULL PRIMARY KEY, " +
                        "totalCalories INTEGER NOT NULL" +
                        ")",
                )
                db.execSQL(
                    "INSERT INTO day_totals (epochDay, totalCalories) " +
                        "SELECT epochDay, SUM(calories) FROM meals GROUP BY epochDay",
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS weights (" +
                        "epochDay INTEGER NOT NULL PRIMARY KEY, " +
                        "tenthsOfKg INTEGER NOT NULL" +
                        ")",
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            val existing = instance
            if (existing != null) {
                return existing
            }
            return synchronized(this) {
                val created = instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                instance = created
                created
            }
        }
    }
}
