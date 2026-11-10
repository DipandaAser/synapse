package com.aserdipanda.synapse.data.triggers

import androidx.room.withTransaction
import com.aserdipanda.synapse.core.common.Constants
import com.aserdipanda.synapse.data.triggers.local.ActionEntity
import com.aserdipanda.synapse.data.triggers.local.AppSettingEntity
import com.aserdipanda.synapse.data.triggers.local.ConditionEntity
import com.aserdipanda.synapse.data.triggers.local.TriggerDatabase
import com.aserdipanda.synapse.data.triggers.local.TriggerEntity
import com.aserdipanda.synapse.data.triggers.local.TriggerWithRelations
import kotlinx.coroutines.flow.Flow

class TriggersRepository(
    private val database: TriggerDatabase
) {

    private val triggerDao = database.triggerDao()
    private val conditionDao = database.conditionDao()
    private val actionDao = database.actionDao()
    private val appSettingDao = database.appSettingDao()

    fun getAllTriggers(): Flow<List<TriggerEntity>> {
        return triggerDao.getAllTriggers()
    }

    suspend fun getActiveTriggers(): List<TriggerEntity> {
        return triggerDao.getActiveTriggers()
    }

    suspend fun getTriggerById(id: Long): TriggerEntity? {
        return triggerDao.getTriggerById(id)
    }

    suspend fun insertTrigger(trigger: TriggerEntity): Long {
        return triggerDao.insertTrigger(trigger)
    }

    suspend fun updateTrigger(trigger: TriggerEntity) {
        triggerDao.updateTrigger(trigger)
    }

    suspend fun deleteTrigger(trigger: TriggerEntity) {
        triggerDao.deleteTrigger(trigger)
    }

    suspend fun updateTriggerEnabledStatus(id: Long, enabled: Boolean) {
        triggerDao.updateTriggerEnabledStatus(id, enabled)
    }

    fun getAllTriggersWithRelations(): Flow<List<TriggerWithRelations>> {
        return triggerDao.getAllTriggersWithRelations()
    }

    suspend fun getActiveTriggersWithRelations(): List<TriggerWithRelations> {
        return triggerDao.getActiveTriggersWithRelations()
    }

    suspend fun getTriggerWithRelationsById(id: Long): TriggerWithRelations? {
        return triggerDao.getTriggerWithRelationsById(id)
    }

    // Runs in one transaction, so a trigger is never stored without its conditions and actions.
    suspend fun createTriggerWithRelations(
        trigger: TriggerEntity,
        conditions: List<ConditionEntity>,
        actions: List<ActionEntity>
    ): Long = database.withTransaction {
        val triggerId = triggerDao.insertTrigger(trigger)

        val conditionsWithTriggerId = conditions.map { it.copy(triggerId = triggerId) }
        conditionDao.insertConditions(conditionsWithTriggerId)

        val actionsWithTriggerId = actions.map { it.copy(triggerId = triggerId) }
        actionDao.insertActions(actionsWithTriggerId)

        triggerId
    }

    // Replaces all conditions and actions in one transaction, so an interrupted save
    // cannot leave the trigger with none.
    suspend fun updateTriggerWithRelations(
        trigger: TriggerEntity,
        conditions: List<ConditionEntity>,
        actions: List<ActionEntity>
    ) {
        database.withTransaction {
            triggerDao.updateTrigger(trigger)

            conditionDao.deleteConditionsForTrigger(trigger.id)
            actionDao.deleteActionsForTrigger(trigger.id)

            val conditionsWithTriggerId = conditions.map { it.copy(triggerId = trigger.id) }
            conditionDao.insertConditions(conditionsWithTriggerId)

            val actionsWithTriggerId = actions.map { it.copy(triggerId = trigger.id) }
            actionDao.insertActions(actionsWithTriggerId)
        }
    }

    suspend fun getConditionsForTrigger(triggerId: Long): List<ConditionEntity> {
        return conditionDao.getConditionsForTrigger(triggerId)
    }

    suspend fun insertCondition(condition: ConditionEntity): Long {
        return conditionDao.insertCondition(condition)
    }

    suspend fun deleteCondition(condition: ConditionEntity) {
        conditionDao.deleteCondition(condition)
    }

    suspend fun getActionsForTrigger(triggerId: Long): List<ActionEntity> {
        return actionDao.getActionsForTrigger(triggerId)
    }

    suspend fun insertAction(action: ActionEntity): Long {
        return actionDao.insertAction(action)
    }

    suspend fun deleteAction(action: ActionEntity) {
        actionDao.deleteAction(action)
    }

    suspend fun isSmsListenerEnabled(): Boolean =
        getBooleanSetting(Constants.SETTING_KEY_SMS_LISTENER_ENABLED)

    suspend fun setSmsListenerEnabled(enabled: Boolean) =
        setBooleanSetting(Constants.SETTING_KEY_SMS_LISTENER_ENABLED, enabled)

    suspend fun isOnboardingCompleted(): Boolean =
        getBooleanSetting(Constants.SETTING_KEY_ONBOARDING_COMPLETED)

    suspend fun setOnboardingCompleted() =
        setBooleanSetting(Constants.SETTING_KEY_ONBOARDING_COMPLETED, true)

    // Android reports the same state before the first SMS request and after a permanent
    // denial, so Synapse remembers that it has asked.
    suspend fun hasRequestedSmsPermission(): Boolean =
        getBooleanSetting(Constants.SETTING_KEY_SMS_PERMISSION_REQUESTED)

    suspend fun setSmsPermissionRequested() =
        setBooleanSetting(Constants.SETTING_KEY_SMS_PERMISSION_REQUESTED, true)

    private suspend fun getBooleanSetting(key: String): Boolean =
        appSettingDao.getSetting(key)?.value?.toBoolean() ?: false

    private suspend fun setBooleanSetting(key: String, value: Boolean) {
        appSettingDao.upsert(AppSettingEntity(key = key, value = value.toString()))
    }
}
