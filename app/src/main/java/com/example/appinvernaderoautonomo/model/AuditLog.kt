package com.example.appinvernaderoautonomo.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AuditEventType(val titulo: String, val esAlertaCritica: Boolean) {
    LOGIN_SUCCESS("Inicio de Sesión Exitoso", false),
    LOGIN_FAILED("Intento Fallido de Autenticación", true),
    LOCKOUT_ACTIVATED("Bloqueo de Seguridad Fail-Safe Activado", true),
    ROUTINE_CHANGED("Cambio de Rutina de Cultivo", false),
    THRESHOLD_MODIFIED("Calibración de Umbrales Críticos", false),
    MANUAL_OVERRIDE("Activación de Control Manual / Override", true),
    FAILSAFE_TRIGGERED("Modo Fail-Safe de Borde Disparado", true),
    SYSTEM_RESET("Restablecimiento del Sistema", false)
}

data class AuditLog(
    val id: Long = 0,
    val timestamp: Long,
    val username: String,
    val userRole: String,
    val eventType: AuditEventType,
    val details: String
) {
    val fechaFormateada: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val horaFormateada: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
