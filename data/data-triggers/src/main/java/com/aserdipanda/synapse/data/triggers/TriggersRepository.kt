package com.aserdipanda.synapse.data.triggers

import com.aserdipanda.synapse.core.common.Constants
import com.aserdipanda.synapse.data.triggers.local.AppSettingDao
import com.aserdipanda.synapse.data.triggers.local.AppSettingEntity
import com.aserdipanda.synapse.data.triggers.local.TriggerDao
import com.aserdipanda.synapse.data.triggers.local.TriggerEntity
import kotlinx.coroutines.flow.Flow

class TriggersRepository(
    private val triggerDao: TriggerDao,
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
    
    suspend fun updateTriggerActiveStatus(id: Long, isActive: Boolean) {
        triggerDao.updateTriggerActiveStatus(id, isActive)
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
