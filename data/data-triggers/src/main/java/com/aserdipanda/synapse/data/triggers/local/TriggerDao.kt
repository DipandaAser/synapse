package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerDao {
    
    @Query("SELECT * FROM triggers ORDER BY createdAt DESC")
    fun getAllTriggers(): Flow<List<TriggerEntity>>
    
    @Query("SELECT * FROM triggers WHERE isActive = 1")
    suspend fun getActiveTriggers(): List<TriggerEntity>
    
    @Query("SELECT * FROM triggers WHERE id = :id")
    suspend fun getTriggerById(id: Long): TriggerEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrigger(trigger: TriggerEntity): Long
    
    @Update
    suspend fun updateTrigger(trigger: TriggerEntity)
    
    @Delete
    suspend fun deleteTrigger(trigger: TriggerEntity)
    
    @Query("UPDATE triggers SET isActive = :isActive WHERE id = :id")
    suspend fun updateTriggerActiveStatus(id: Long, isActive: Boolean)
}
