package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppSettingDao {
    @Query("SELECT * FROM app_settings WHERE \"key\" = :key")
    suspend fun getSetting(key: String): AppSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(appSettingEntity: AppSettingEntity)

    @Delete(entity = AppSettingEntity::class)
    suspend fun delete(appSettingEntity: AppSettingEntity)
}