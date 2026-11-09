package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "triggers")
data class TriggerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
