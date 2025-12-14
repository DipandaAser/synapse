package com.aserdipanda.synapse.data.triggers

import com.aserdipanda.synapse.core.common.Constants
import com.aserdipanda.synapse.data.triggers.local.ActionDao
import com.aserdipanda.synapse.data.triggers.local.ActionEntity
import com.aserdipanda.synapse.data.triggers.local.AppSettingDao
import com.aserdipanda.synapse.data.triggers.local.AppSettingEntity
import com.aserdipanda.synapse.data.triggers.local.ConditionDao
import com.aserdipanda.synapse.data.triggers.local.ConditionEntity
import com.aserdipanda.synapse.data.triggers.local.TriggerDao
import com.aserdipanda.synapse.data.triggers.local.TriggerEntity
import com.aserdipanda.synapse.data.triggers.local.TriggerWithRelations
import kotlinx.coroutines.flow.Flow

class TriggersRepository(
    private val triggerDao: TriggerDao,
    private val conditionDao: ConditionDao,
    private val actionDao: ActionDao,
    private val appSettingDao: AppSettingDao
) {

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

    suspend fun createTriggerWithRelations(
        trigger: TriggerEntity,
        conditions: List<ConditionEntity>,
        actions: List<ActionEntity>
    ): Long {
        val triggerId = triggerDao.insertTrigger(trigger)
        
        val conditionsWithTriggerId = conditions.map { it.copy(triggerId = triggerId) }
        conditionDao.insertConditions(conditionsWithTriggerId)
        
        val actionsWithTriggerId = actions.map { it.copy(triggerId = triggerId) }
        actionDao.insertActions(actionsWithTriggerId)
        
        return triggerId
    }

    suspend fun updateTriggerWithRelations(
        trigger: TriggerEntity,
        conditions: List<ConditionEntity>,
        actions: List<ActionEntity>
    ) {
        triggerDao.updateTrigger(trigger)
        
        conditionDao.deleteConditionsForTrigger(trigger.id)
        actionDao.deleteActionsForTrigger(trigger.id)
        
        val conditionsWithTriggerId = conditions.map { it.copy(triggerId = trigger.id) }
        conditionDao.insertConditions(conditionsWithTriggerId)
        
        val actionsWithTriggerId = actions.map { it.copy(triggerId = trigger.id) }
        actionDao.insertActions(actionsWithTriggerId)
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

    suspend fun isSmsListenerEnabled(): Boolean {
        val setting = appSettingDao.getSetting(Constants.SETTING_KEY_SMS_LISTENER_ENABLED)
        return setting?.value?.toBoolean() ?: false
    }

    suspend fun setSmsListenerEnabled(enabled: Boolean) {
        val setting = AppSettingEntity(
            key = Constants.SETTING_KEY_SMS_LISTENER_ENABLED,
            value = enabled.toString()
        )
        appSettingDao.upsert(setting)
    }
}
