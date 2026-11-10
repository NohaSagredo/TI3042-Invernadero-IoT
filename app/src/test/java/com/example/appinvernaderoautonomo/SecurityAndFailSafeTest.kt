package com.example.appinvernaderoautonomo

import com.example.appinvernaderoautonomo.model.PlantProfile
import com.example.appinvernaderoautonomo.model.Role
import com.example.appinvernaderoautonomo.security.FailSafeLockoutManager
import com.example.appinvernaderoautonomo.security.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecurityAndFailSafeTest {

    @Before
    fun setUp() {
        FailSafeLockoutManager.reset()
    }

    @Test
    fun testPasswordHasher_ValidCredentialsSucceeds() {
        val rawPassword = "Admin@Secure2026!"
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hashPassword(rawPassword, salt)
        val saltHex = PasswordHasher.bytesToHex(salt)

        val isValid = PasswordHasher.verifyPassword(rawPassword, saltHex, hash)
        assertTrue("El hash PBKDF2 debe validar la contraseña correcta.", isValid)
    }

    @Test
    fun testPasswordHasher_InvalidCredentialsFails() {
        val rawPassword = "Operador@Pass2026"
        val wrongPassword = "WrongPassword123"
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hashPassword(rawPassword, salt)
        val saltHex = PasswordHasher.bytesToHex(salt)

        val isValid = PasswordHasher.verifyPassword(wrongPassword, saltHex, hash)
        assertFalse("El hash PBKDF2 debe rechazar una contraseña incorrecta.", isValid)
    }

    @Test
    fun testPasswordHasher_DistinctSaltsProduceDifferentHashes() {
        val password = "SharedPassword123"
        val salt1 = PasswordHasher.generateSalt()
        val salt2 = PasswordHasher.generateSalt()

        val hash1 = PasswordHasher.hashPassword(password, salt1)
        val hash2 = PasswordHasher.hashPassword(password, salt2)

        assertNotEquals("Dos salts distintos nunca deben generar el mismo hash (Anti-Rainbow Table).", hash1, hash2)
    }

    @Test
    fun testFailSafeLockout_TriggersAtThreeAttempts() {
        assertFalse("Inicialmente el sistema no debe estar bloqueado.", FailSafeLockoutManager.isLockedOut())

        val locked1 = FailSafeLockoutManager.registerFailedAttempt()
        assertFalse("Intento 1 no debe activar bloqueo.", locked1)
        assertEquals(1, FailSafeLockoutManager.failedAttempts.value)

        val locked2 = FailSafeLockoutManager.registerFailedAttempt()
        assertFalse("Intento 2 no debe activar bloqueo.", locked2)
        assertEquals(2, FailSafeLockoutManager.failedAttempts.value)

        val locked3 = FailSafeLockoutManager.registerFailedAttempt()
        assertTrue("Intento 3 debe activar inmediatamente el bloqueo Fail-Safe.", locked3)
        assertTrue("El sistema debe estar en estado de bloqueo.", FailSafeLockoutManager.isLockedOut())
        assertTrue("Los segundos restantes deben ser mayores a 0.", FailSafeLockoutManager.getRemainingSeconds() > 170)

        FailSafeLockoutManager.reset()
        assertFalse("Tras reset el bloqueo debe cancelarse.", FailSafeLockoutManager.isLockedOut())
        assertEquals(0, FailSafeLockoutManager.failedAttempts.value)
    }

    @Test
    fun testRole_RbacPermissionMatrix() {
        // Admin
        assertTrue("Admin debe poder calibrar umbrales.", Role.ADMIN.canEditThresholds())
        assertTrue("Admin debe poder forzar actuadores.", Role.ADMIN.canOverrideActuators())
        assertTrue("Admin debe poder cambiar rutinas.", Role.ADMIN.canChangeRoutines())
        assertTrue("Admin debe poder ver auditoría.", Role.ADMIN.canViewAuditLogs())

        // Operador
        assertFalse("Operador NO debe calibrar umbrales.", Role.OPERATOR.canEditThresholds())
        assertFalse("Operador NO debe forzar actuadores en modo manual.", Role.OPERATOR.canOverrideActuators())
        assertTrue("Operador sí debe poder conmutar rutinas.", Role.OPERATOR.canChangeRoutines())
        assertFalse("Operador NO debe ver la bitácora de auditoría de ciberseguridad.", Role.OPERATOR.canViewAuditLogs())

        // Auditor
        assertFalse("Auditor NO debe alterar umbrales.", Role.AUDITOR.canEditThresholds())
        assertFalse("Auditor NO debe forzar actuadores físicos.", Role.AUDITOR.canOverrideActuators())
        assertFalse("Auditor NO debe conmutar rutinas operativas.", Role.AUDITOR.canChangeRoutines())
        assertTrue("Auditor SÍ debe tener acceso a la bitácora de auditoría ISO.", Role.AUDITOR.canViewAuditLogs())
    }

    @Test
    fun testPlantProfile_BiologicalBoundsSanity() {
        PlantProfile.DEFAULT_PROFILES.forEach { perfil ->
            assertTrue("En perfil ${perfil.name}, tempMin debe ser menor a tempMax.", perfil.tempMin < perfil.tempMax)
            assertTrue("En perfil ${perfil.name}, humidityMin debe ser menor a humidityMax.", perfil.humidityMin < perfil.humidityMax)
            assertTrue("En perfil ${perfil.name}, soilMin debe ser menor a soilMax.", perfil.soilRawMin < perfil.soilRawMax)
            assertTrue("El ángulo del servo no debe sobrepasar 120 grados físicos.", perfil.targetServoAngle in 0..120)
            assertTrue("La velocidad del ventilador debe estar en rango PWM 8-bit (0-255).", perfil.targetFanPwm in 0..255)
        }
    }
}
