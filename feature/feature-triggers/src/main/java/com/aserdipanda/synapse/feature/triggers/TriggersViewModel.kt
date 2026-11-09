package com.aserdipanda.synapse.feature.triggers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aserdipanda.synapse.data.triggers.TriggersRepository
import com.aserdipanda.synapse.data.triggers.local.ActionEntity
import com.aserdipanda.synapse.data.triggers.local.ConditionEntity
import com.aserdipanda.synapse.data.triggers.local.TriggerEntity
import com.aserdipanda.synapse.data.triggers.local.TriggerWithRelations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TriggersViewModel(
    private val repository: TriggersRepository
) : ViewModel() {
    
    private val _triggers = MutableStateFlow<List<TriggerWithRelations>>(emptyList())
    val triggers: StateFlow<List<TriggerWithRelations>> = _triggers.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isSmsListenerEnabled = MutableStateFlow(false)
    val isSmsListenerEnabled: StateFlow<Boolean> = _isSmsListenerEnabled.asStateFlow()

    init {
        loadTriggers()
        loadServiceState()
    }
    
    private fun loadTriggers() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllTriggersWithRelations().collect { triggerList ->
                _triggers.value = triggerList
                _isLoading.value = false
            }
        }
    }
    
    private fun loadServiceState() {
        viewModelScope.launch {
            _isSmsListenerEnabled.value = repository.isSmsListenerEnabled()
        }
    }

    fun setSmsListenerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSmsListenerEnabled(enabled)
            _isSmsListenerEnabled.value = enabled
        }
    }

    fun addTrigger(trigger: TriggerEntity) {
        viewModelScope.launch {
            repository.insertTrigger(trigger)
        }
    }
    
    fun createTriggerWithRelations(
        name: String,
        enabled: Boolean,
        conditions: List<ConditionEntity>,
        actions: List<ActionEntity>
    ) {
        viewModelScope.launch {
            val trigger = TriggerEntity(
                name = name,
                enabled = enabled
            )
            repository.createTriggerWithRelations(trigger, conditions, actions)
        }
    }
    
    fun updateTrigger(trigger: TriggerEntity) {
        viewModelScope.launch {
            repository.updateTrigger(trigger)
        }
    }
    
    fun updateTriggerWithRelations(
        trigger: TriggerEntity,
        conditions: List<ConditionEntity>,
        actions: List<ActionEntity>
    ) {
        viewModelScope.launch {
            repository.updateTriggerWithRelations(trigger, conditions, actions)
        }
    }
    
    fun deleteTrigger(trigger: TriggerEntity) {
        viewModelScope.launch {
            repository.deleteTrigger(trigger)
        }
    }
    
    fun toggleTriggerStatus(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateTriggerEnabledStatus(id, enabled)
        }
    }
    
    suspend fun getTriggerWithRelationsById(id: Long): TriggerWithRelations? {
        return repository.getTriggerWithRelationsById(id)
    }
}
