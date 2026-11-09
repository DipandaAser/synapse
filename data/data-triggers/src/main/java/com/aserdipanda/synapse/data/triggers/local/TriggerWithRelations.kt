package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class that combines a trigger with its associated conditions and actions.
 * Used for queries that need the complete trigger configuration.
 */
data class TriggerWithRelations(
    @Embedded
    val trigger: TriggerEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "triggerId"
    )
    val conditions: List<ConditionEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "triggerId"
    )
    val actions: List<ActionEntity>
)
