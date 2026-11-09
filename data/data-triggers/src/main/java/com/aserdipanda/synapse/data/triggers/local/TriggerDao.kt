package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerDao {
    
    @Query("SELECT * FROM triggers ORDER BY createdAt DESC")
    fun getAllTriggers(): Flow<List<TriggerEntity>>
    
    @Query("SELECT * FROM triggers WHERE enabled = 1")
    suspend fun getActiveTriggers(): List<TriggerEntity>
    
    @Query("SELECT * FROM triggers WHERE id = :id")
    suspend fun getTriggerById(id: Long): TriggerEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrigger(trigger: TriggerEntity): Long
    
    @Update
    suspend fun updateTrigger(trigger: TriggerEntity)
    
    @Delete
    suspend fun deleteTrigger(trigger: TriggerEntity)
    
    @Query("UPDATE triggers SET enabled = :enabled WHERE id = :id")
    suspend fun updateTriggerEnabledStatus(id: Long, enabled: Boolean)
    
    @Transaction
    @Query("SELECT * FROM triggers ORDER BY createdAt DESC")
    fun getAllTriggersWithRelations(): Flow<List<TriggerWithRelations>>
    
    @Transaction
    @Query("SELECT * FROM triggers WHERE enabled = 1")
    suspend fun getActiveTriggersWithRelations(): List<TriggerWithRelations>
    
    @Transaction
    @Query("SELECT * FROM triggers WHERE id = :id")
    suspend fun getTriggerWithRelationsById(id: Long): TriggerWithRelations?
}
