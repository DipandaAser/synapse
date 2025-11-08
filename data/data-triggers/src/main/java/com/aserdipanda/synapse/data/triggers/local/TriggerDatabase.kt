package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TriggerEntity::class, AppSettingEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TriggerDatabase : RoomDatabase() {
    abstract fun triggerDao(): TriggerDao
    abstract fun appSettingDao(): AppSettingDao
}
