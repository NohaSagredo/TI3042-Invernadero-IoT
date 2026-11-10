package com.example.appinvernaderoautonomo.security

import com.example.appinvernaderoautonomo.model.Role
import com.example.appinvernaderoautonomo.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.security.SecureRandom

object SessionManager {

    const val INACTIVITY_TIMEOUT_MS = 15 * 60 * 1000L // 15 minutos

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isSessionExpired = MutableStateFlow(false)
    val isSessionExpired: StateFlow<Boolean> = _isSessionExpired.asStateFlow()

    private var sessionToken: String? = null
    private var lastActivityTimestamp: Long = 0L

    private val scope = CoroutineScope(Dispatchers.Default)
    private var watchdogJob: Job? = null

    val isAuthenticated: Boolean
        get() {
            val user = _currentUser.value ?: return false
            if (System.currentTimeMillis() - lastActivityTimestamp > INACTIVITY_TIMEOUT_MS) {
                expirarSesion()
                return false
            }
            return sessionToken != null
        }

    val currentRole: Role?
        get() = _currentUser.value?.role

    fun login(user: User): String {
        _currentUser.value = user
        _isSessionExpired.value = false
        lastActivityTimestamp = System.currentTimeMillis()

        val tokenBytes = ByteArray(32)
        SecureRandom().nextBytes(tokenBytes)
        val token = PasswordHasher.bytesToHex(tokenBytes)
        sessionToken = token

        iniciarWatchdogInactividad()
        return token
    }

    fun recordActivity() {
        if (_currentUser.value != null) {
            lastActivityTimestamp = System.currentTimeMillis()
        }
    }

    fun logout() {
        watchdogJob?.cancel()
        _currentUser.value = null
        sessionToken = null
        lastActivityTimestamp = 0L
        _isSessionExpired.value = false
    }

    private fun expirarSesion() {
        watchdogJob?.cancel()
        _currentUser.value = null
        sessionToken = null
        lastActivityTimestamp = 0L
        _isSessionExpired.value = true
    }

    private fun iniciarWatchdogInactividad() {
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            while (isActive) {
                delay(10000L) // Chequeo cada 10 segundos
                if (_currentUser.value != null) {
                    val inactivo = System.currentTimeMillis() - lastActivityTimestamp
                    if (inactivo > INACTIVITY_TIMEOUT_MS) {
                        expirarSesion()
                        break
                    }
                } else {
                    break
                }
            }
        }
    }
}
