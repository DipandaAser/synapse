package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "actions",
    foreignKeys = [
        ForeignKey(
            entity = TriggerEntity::class,
            parentColumns = ["id"],
            childColumns = ["triggerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["triggerId"])]
)
data class ActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerId: Long,
    val type: String,
    val arg: String
)
