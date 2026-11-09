package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ActionDao {
    
    @Query("SELECT * FROM actions WHERE triggerId = :triggerId")
    suspend fun getActionsForTrigger(triggerId: Long): List<ActionEntity>
    
    @Query("SELECT * FROM actions WHERE id = :id")
    suspend fun getActionById(id: Long): ActionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: ActionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActions(actions: List<ActionEntity>): List<Long>
    
    @Update
    suspend fun updateAction(action: ActionEntity)
    
    @Delete
    suspend fun deleteAction(action: ActionEntity)
    
    @Query("DELETE FROM actions WHERE triggerId = :triggerId")
    suspend fun deleteActionsForTrigger(triggerId: Long)
}
