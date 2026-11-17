package com.neyvan.desinstalador.accessibility

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class AccessibilityServiceAvailability<T : Any> {
    @Volatile
    private var currentInstance: T? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    @Synchronized
    fun connected(instance: T) {
        currentInstance = instance
        _isRunning.value = true
    }

    @Synchronized
    fun interrupted(instance: T) {
        if (currentInstance === instance) {
            _isRunning.value = true
        }
    }

    @Synchronized
    fun destroyed(instance: T) {
        if (currentInstance === instance) {
            currentInstance = null
            _isRunning.value = false
        }
    }

    fun isRunning(): Boolean = currentInstance != null && _isRunning.value
}
