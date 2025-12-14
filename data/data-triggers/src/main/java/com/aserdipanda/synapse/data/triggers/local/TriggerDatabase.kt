package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TriggerEntity::class,
        ConditionEntity::class,
        ActionEntity::class,
        AppSettingEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TriggerDatabase : RoomDatabase() {
    abstract fun triggerDao(): TriggerDao
    abstract fun conditionDao(): ConditionDao
    abstract fun actionDao(): ActionDao
    abstract fun appSettingDao(): AppSettingDao
}
