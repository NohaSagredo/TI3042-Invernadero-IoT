package com.example.appinvernaderoautonomo.security

import com.example.appinvernaderoautonomo.model.Role
import com.example.appinvernaderoautonomo.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom

object SessionManager {

    private const val INACTIVITY_TIMEOUT_MS = 15 * 60 * 1000L // 15 minutos

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var sessionToken: String? = null
    private var lastActivityTimestamp: Long = 0L

    val isAuthenticated: Boolean
        get() {
            val user = _currentUser.value ?: return false
            if (System.currentTimeMillis() - lastActivityTimestamp > INACTIVITY_TIMEOUT_MS) {
                logout()
                return false
            }
            return sessionToken != null
        }

    val currentRole: Role?
        get() = _currentUser.value?.role

    fun login(user: User): String {
        _currentUser.value = user
        lastActivityTimestamp = System.currentTimeMillis()
        val tokenBytes = ByteArray(32)
        SecureRandom().nextBytes(tokenBytes)
        val token = PasswordHasher.bytesToHex(tokenBytes)
        sessionToken = token
        return token
    }

    fun recordActivity() {
        if (_currentUser.value != null) {
            lastActivityTimestamp = System.currentTimeMillis()
        }
    }

    fun logout() {
        _currentUser.value = null
        sessionToken = null
        lastActivityTimestamp = 0L
    }
}
