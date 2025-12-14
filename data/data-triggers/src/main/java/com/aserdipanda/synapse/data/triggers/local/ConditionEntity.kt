package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conditions",
    foreignKeys = [
        ForeignKey(
            entity = TriggerEntity::class,
            parentColumns = ["id"],
            childColumns = ["triggerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["triggerId"])],
)
data class ConditionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerId: Long,
    val field: String,
    val operator: String,
    val value: String
)
