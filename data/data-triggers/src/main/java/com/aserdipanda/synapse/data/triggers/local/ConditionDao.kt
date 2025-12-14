package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ConditionDao {
    
    @Query("SELECT * FROM conditions WHERE triggerId = :triggerId")
    suspend fun getConditionsForTrigger(triggerId: Long): List<ConditionEntity>
    
    @Query("SELECT * FROM conditions WHERE id = :id")
    suspend fun getConditionById(id: Long): ConditionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCondition(condition: ConditionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConditions(conditions: List<ConditionEntity>): List<Long>
    
    @Update
    suspend fun updateCondition(condition: ConditionEntity)
    
    @Delete
    suspend fun deleteCondition(condition: ConditionEntity)
    
    @Query("DELETE FROM conditions WHERE triggerId = :triggerId")
    suspend fun deleteConditionsForTrigger(triggerId: Long)
}
