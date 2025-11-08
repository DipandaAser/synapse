package com.aserdipanda.synapse.feature.triggers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aserdipanda.synapse.data.triggers.TriggersRepository

class TriggersViewModelFactory(
    private val repository: TriggersRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TriggersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TriggersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

