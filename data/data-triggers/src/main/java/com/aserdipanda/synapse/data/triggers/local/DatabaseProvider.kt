package com.aserdipanda.synapse.data.triggers.local

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: TriggerDatabase? = null

    private const val TAG = "DatabaseProvider"

    private val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d(TAG, "Database created successfully")
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            Log.d(TAG, "Database opened successfully")
        }
    }

    fun getDatabase(context: Context): TriggerDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                TriggerDatabase::class.java,
                "synapse_database"
            )
                .fallbackToDestructiveMigration()
                .addCallback(callback)
                .build()
            INSTANCE = instance
            Log.d(TAG, "Database instance initialized")
            instance
        }
    }
}

