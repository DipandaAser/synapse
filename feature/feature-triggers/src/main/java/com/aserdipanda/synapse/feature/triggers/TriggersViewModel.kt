package com.aserdipanda.synapse.feature.triggers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aserdipanda.synapse.data.triggers.TriggersRepository
import com.aserdipanda.synapse.data.triggers.local.TriggerEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TriggersViewModel(
    private val repository: TriggersRepository
) : ViewModel() {
    
    private val _triggers = MutableStateFlow<List<TriggerEntity>>(emptyList())
    val triggers: StateFlow<List<TriggerEntity>> = _triggers.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadTriggers()
    }
    
    private fun loadTriggers() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllTriggers().collect { triggerList ->
                _triggers.value = triggerList
                _isLoading.value = false
            }
        }
    }
    
    fun addTrigger(trigger: TriggerEntity) {
        viewModelScope.launch {
            repository.insertTrigger(trigger)
        }
    }
    
    fun updateTrigger(trigger: TriggerEntity) {
        viewModelScope.launch {
            repository.updateTrigger(trigger)
        }
    }
    
    fun deleteTrigger(trigger: TriggerEntity) {
        viewModelScope.launch {
            repository.deleteTrigger(trigger)
        }
    }
    
    fun toggleTriggerStatus(id: Long, isActive: Boolean) {
        viewModelScope.launch {
            repository.updateTriggerActiveStatus(id, isActive)
        }
    }
}
