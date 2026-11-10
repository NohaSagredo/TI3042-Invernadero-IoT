package com.example.appinvernaderoautonomo.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FailSafeLockoutManager {

    const val MAX_FAILED_ATTEMPTS = 3
    const val LOCKOUT_DURATION_MS = 3 * 60 * 1000L // 3 minutos (180 segundos)
    private const val PREFS_NAME = "failsafe_lockout_prefs"
    private const val KEY_LOCKOUT_UNTIL = "lockout_until_timestamp"
    private const val KEY_FAILED_ATTEMPTS = "consecutive_failed_attempts"

    private var prefs: SharedPreferences? = null

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _lockoutUntil = MutableStateFlow(0L)
    val lockoutUntil: StateFlow<Long> = _lockoutUntil.asStateFlow()

    fun init(context: Context) {
        val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        val storedUntil = p.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val storedAttempts = p.getInt(KEY_FAILED_ATTEMPTS, 0)

        val now = System.currentTimeMillis()
        if (now < storedUntil) {
            _lockoutUntil.value = storedUntil
            _failedAttempts.value = storedAttempts
        } else {
            // Ya expiró el bloqueo anterior
            _lockoutUntil.value = 0L
            _failedAttempts.value = 0
            p.edit().remove(KEY_LOCKOUT_UNTIL).remove(KEY_FAILED_ATTEMPTS).apply()
        }
    }

    fun isLockedOut(): Boolean {
        val now = System.currentTimeMillis()
        if (now < _lockoutUntil.value) {
            return true
        }
        if (_lockoutUntil.value != 0L && now >= _lockoutUntil.value) {
            reset()
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
            val until = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            _lockoutUntil.value = until
            prefs?.edit()
                ?.putLong(KEY_LOCKOUT_UNTIL, until)
                ?.putInt(KEY_FAILED_ATTEMPTS, newCount)
                ?.apply()
            return true // Bloqueo activado
        } else {
            prefs?.edit()?.putInt(KEY_FAILED_ATTEMPTS, newCount)?.apply()
            return false
        }
    }

    fun reset() {
        _failedAttempts.value = 0
        _lockoutUntil.value = 0L
        prefs?.edit()?.clear()?.apply()
    }
}
