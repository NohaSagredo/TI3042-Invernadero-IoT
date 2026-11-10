package com.example.appinvernaderoautonomo.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FailSafeLockoutManager {

    const val MAX_FAILED_ATTEMPTS = 3
    const val LOCKOUT_DURATION_MS = 3 * 60 * 1000L // 3 minutos (180 segundos)

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _lockoutUntil = MutableStateFlow(0L)
    val lockoutUntil: StateFlow<Long> = _lockoutUntil.asStateFlow()

    fun isLockedOut(): Boolean {
        val now = System.currentTimeMillis()
        if (now < _lockoutUntil.value) {
            return true
        }
        if (_lockoutUntil.value != 0L && now >= _lockoutUntil.value) {
            // Se cumplió el tiempo de bloqueo, reiniciamos el estado
            _lockoutUntil.value = 0L
            _failedAttempts.value = 0
        }
        return false
    }

    fun getRemainingSeconds(): Int {
        val diff = _lockoutUntil.value - System.currentTimeMillis()
        return if (diff > 0) (diff / 1000).toInt() else 0
    }

    fun registerFailedAttempt(): Boolean {
        val newCount = _failedAttempts.value + 1
        _failedAttempts.value = newCount
        if (newCount >= MAX_FAILED_ATTEMPTS) {
            _lockoutUntil.value = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            return true // Bloqueo activado
        }
        return false
    }

    fun reset() {
        _failedAttempts.value = 0
        _lockoutUntil.value = 0L
    }
}
