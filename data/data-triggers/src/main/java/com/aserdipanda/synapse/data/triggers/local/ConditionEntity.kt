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
    val operator: Operator,
    val value: String
)

enum class Operator(val displayName: String) {
    EQUALS("EQUALS"),
    CONTAINS("CONTAINS"),
    START_WITH("START WITH"),
    END_WITH("END WITH"),
    DOES_NOT_CONTAINS("DOES NOT CONTAINS");

    fun description(): String {
        return when (this) {
            EQUALS -> "Sender is exactly"
            CONTAINS -> "Sender contains"
            START_WITH -> "Sender starts with"
            END_WITH -> "Sender ends with"
            DOES_NOT_CONTAINS -> "Sender does not contain"
        }
    }
}
