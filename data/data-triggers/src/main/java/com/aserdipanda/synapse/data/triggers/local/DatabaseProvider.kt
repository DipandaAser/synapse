package com.aserdipanda.synapse.data.triggers.local

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: TriggerDatabase? = null

    private const val TAG = "DatabaseProvider"

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            Log.d(TAG, "Starting migration from version 2 to version 3")
            
            // Create conditions table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS conditions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    triggerId INTEGER NOT NULL,
                    field TEXT NOT NULL,
                    operator TEXT NOT NULL,
                    value TEXT NOT NULL,
                    FOREIGN KEY (triggerId) REFERENCES triggers(id) ON DELETE CASCADE
                )
            """)
            
            // Create index on triggerId
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_conditions_triggerId 
                ON conditions(triggerId)
            """)
            
            // Create actions table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS actions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    triggerId INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    arg TEXT NOT NULL,
                    FOREIGN KEY (triggerId) REFERENCES triggers(id) ON DELETE CASCADE
                )
            """)
            
            // Create index on triggerId
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS index_actions_triggerId 
                ON actions(triggerId)
            """)
            
            // Migrate existing trigger data to conditions
            // For each trigger, create conditions from senderPattern and messagePattern
            db.execSQL("""
                INSERT INTO conditions (triggerId, field, operator, value)
                SELECT id, 'sender', 'CONTAINS', senderPattern
                FROM triggers
                WHERE senderPattern IS NOT NULL AND senderPattern != ''
            """)
            
            db.execSQL("""
                INSERT INTO conditions (triggerId, field, operator, value)
                SELECT id, 'message_body', 'CONTAINS', messagePattern
                FROM triggers
                WHERE messagePattern IS NOT NULL AND messagePattern != ''
            """)
            
            // Migrate webhook configuration to actions
            db.execSQL("""
                INSERT INTO actions (triggerId, type, arg)
                SELECT 
                    id, 
                    'WEBHOOK',
                    '{"url":"' || webhookUrl || '","method":"' || COALESCE(webhookMethod, 'POST') || '"' ||
                    CASE 
                        WHEN webhookBody IS NOT NULL THEN ',"body":"' || replace(replace(webhookBody, '\', '\\'), '"', '\"') || '"'
                        ELSE ''
                    END || '}'
                FROM triggers
                WHERE webhookUrl IS NOT NULL AND webhookUrl != ''
            """)
            
            // Create new triggers table with updated schema
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS triggers_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    enabled INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """)
            
            // Copy data to new table (rename isActive to enabled)
            db.execSQL("""
                INSERT INTO triggers_new (id, name, enabled, createdAt, updatedAt)
                SELECT id, name, isActive, createdAt, updatedAt
                FROM triggers
            """)
            
            // Drop old table and rename new one
            db.execSQL("DROP TABLE triggers")
            db.execSQL("ALTER TABLE triggers_new RENAME TO triggers")
            
            Log.d(TAG, "Migration from version 2 to version 3 completed successfully")
        }
    }

    private val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
        }
    }

    fun getDatabase(context: Context): TriggerDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                TriggerDatabase::class.java,
                "synapse_database"
            )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .addCallback(callback)
                .build()
            INSTANCE = instance
            Log.d(TAG, "Database instance initialized")
            instance
        }
    }
}

