package com.aserdipanda.synapse.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aserdipanda.synapse.data.triggers.TriggersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: TriggersRepository
) : ViewModel() {

    // Null until the saved value loads, so the app doesn't show the wrong first screen.
    private val _isOnboardingCompleted = MutableStateFlow<Boolean?>(null)
    val isOnboardingCompleted: StateFlow<Boolean?> = _isOnboardingCompleted.asStateFlow()

    private val _hasRequestedSmsPermission = MutableStateFlow(false)
    val hasRequestedSmsPermission: StateFlow<Boolean> = _hasRequestedSmsPermission.asStateFlow()

    init {
        viewModelScope.launch {
            // Loaded first, so it's ready by the time the first screen shows.
            _hasRequestedSmsPermission.value = repository.hasRequestedSmsPermission()
            _isOnboardingCompleted.value = repository.isOnboardingCompleted()
        }
    }

    fun completeOnboarding() {
        if (_isOnboardingCompleted.value == true) return
        _isOnboardingCompleted.value = true
        viewModelScope.launch { repository.setOnboardingCompleted() }
    }

    fun onSmsPermissionRequested() {
        if (_hasRequestedSmsPermission.value) return
        _hasRequestedSmsPermission.value = true
        viewModelScope.launch { repository.setSmsPermissionRequested() }
    }
}

class OnboardingViewModelFactory(
    private val repository: TriggersRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
