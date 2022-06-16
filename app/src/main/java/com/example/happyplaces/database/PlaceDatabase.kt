package com.example.happyplaces.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PlaceEntity::class], version = 2)
abstract class PlaceDatabase: RoomDatabase() {

    abstract fun placeDao(): PlaceDao
    companion object {

        private val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `places-table` ADD COLUMN isFavorite INTEGER " +
                        "NOT NULL DEFAULT(0)")
            }
        }

        @Volatile
        private var INSTANCE: PlaceDatabase? = null

        fun getInstance(context: Context): PlaceDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PlaceDatabase::class.java,
                        "place_database"
                    ).fallbackToDestructiveMigration()
                        .addMigrations(migration_1_2)
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }

}