package com.example.appinvernaderoautonomo.model

enum class Role(val displayTitle: String, val badgeColorHex: Long) {
    ADMIN("Administrador", 0xFF1B5E20),
    OPERATOR("Operador Agrícola", 0xFF2E7D32),
    AUDITOR("Auditor / Sistema", 0xFF0277BD);

    fun canEditThresholds(): Boolean = this == ADMIN
    fun canOverrideActuators(): Boolean = this == ADMIN
    fun canChangeRoutines(): Boolean = this == ADMIN || this == OPERATOR
    fun canViewAuditLogs(): Boolean = this == ADMIN || this == AUDITOR
}

data class User(
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val saltHex: String,
    val role: Role,
    val fullName: String,
    val failedAttempts: Int = 0,
    val lockoutUntilTimestamp: Long = 0L
) {
    val isLockedOut: Boolean
        get() = System.currentTimeMillis() < lockoutUntilTimestamp
}
