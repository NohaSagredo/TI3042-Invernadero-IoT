package com.example.appinvernaderoautonomo

import com.example.appinvernaderoautonomo.model.ConnectionMode
import com.example.appinvernaderoautonomo.model.EstadoSuelo
import com.example.appinvernaderoautonomo.model.InvernaderoState
import com.example.appinvernaderoautonomo.model.PlantProfile
import com.example.appinvernaderoautonomo.model.Role
import com.example.appinvernaderoautonomo.model.User
import com.example.appinvernaderoautonomo.notifications.GreenhouseNotificationHelper
import com.example.appinvernaderoautonomo.security.FailSafeLockoutManager
import com.example.appinvernaderoautonomo.security.PasswordHasher
import com.example.appinvernaderoautonomo.security.SessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecurityAndFailSafeTest {

    @Before
    fun setUp() {
        FailSafeLockoutManager.reset()
        SessionManager.logout()
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

        // Visor / Pantalla en Terreno (Kiosko)
        assertFalse("Visor NO debe calibrar umbrales.", Role.VISOR.canEditThresholds())
        assertFalse("Visor NO debe forzar actuadores.", Role.VISOR.canOverrideActuators())
        assertFalse("Visor NO debe conmutar rutinas.", Role.VISOR.canChangeRoutines())
        assertFalse("Visor NO debe ver bitácora de auditoría.", Role.VISOR.canViewAuditLogs())
        assertFalse("Visor NO debe modificar ajustes.", Role.VISOR.canModifySettings())
        assertTrue("Visor es un rol de solo lectura.", Role.VISOR.isReadOnly())
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

    @Test
    fun testSessionManager_LoginAndLogoutLifecycle() {
        assertFalse("Inicialmente no hay sesión autenticada.", SessionManager.isAuthenticated)
        assertNull("El usuario actual debe ser nulo.", SessionManager.currentUser.value)

        val testUser = User(
            id = 1,
            username = "admin",
            passwordHash = "fakehash",
            saltHex = "fakesalt",
            role = Role.ADMIN,
            fullName = "Rodrigo Peña"
        )

        val token = SessionManager.login(testUser)
        assertNotNull("El token de sesión generado no debe ser nulo.", token)
        assertEquals(64, token.length) // 32 bytes en hex son 64 caracteres
        assertTrue("Debe figurar autenticado.", SessionManager.isAuthenticated)
        assertEquals(Role.ADMIN, SessionManager.currentRole)

        SessionManager.logout()
        assertFalse("Tras logout debe estar desautenticado.", SessionManager.isAuthenticated)
        assertNull("El usuario actual debe ser nulo tras logout.", SessionManager.currentUser.value)
    }

    @Test
    fun testInvernaderoState_CalculationsAndBounds() {
        val state = InvernaderoState(
            temperatura = 22.5f,
            humedadAmbiente = 65.0f,
            humedadSueloRaw = 550,
            anguloVentana = 60,
            velocidadVentiladorPwm = 128,
            rutinaActiva = PlantProfile.PERFIL_TOMATE
        )

        assertTrue("22.5°C debe estar dentro del rango de tomate (20-26°C).", state.estaEnRangoTemperatura)
        assertTrue("65% debe estar dentro del rango de tomate (60-70%).", state.estaEnRangoHumedad)
        assertTrue("550 ADC debe estar dentro del rango óptimo de suelo (450-650).", state.estaEnRangoSuelo)
        assertEquals(50, state.porcentajeVentilador)
        assertEquals(50, state.porcentajeVentanaAbierta)
        assertEquals(EstadoSuelo.OPTIMA, state.estadoSuelo)

        // Estado seco
        val dryState = state.copy(humedadSueloRaw = 950)
        assertEquals(EstadoSuelo.FALTA_AGUA, dryState.estadoSuelo)
        assertFalse("950 ADC debe estar fuera del rango óptimo.", dryState.estaEnRangoSuelo)
    }

    @Test
    fun testConnectionMode_TriModalConfigurationAndState() {
        val modes = ConnectionMode.values()
        assertEquals("Deben existir exactamente 3 modos de conexión (Simulación, Wi-Fi, Bluetooth).", 3, modes.size)

        val simMode = ConnectionMode.SIMULACION
        assertEquals("Modo Simulación", simMode.title)

        val wifiMode = ConnectionMode.WIFI
        assertEquals("Modo Wi-Fi", wifiMode.title)

        val btMode = ConnectionMode.BLUETOOTH
        assertEquals("Modo Bluetooth", btMode.title)

        val state = InvernaderoState(
            modoConexion = ConnectionMode.BLUETOOTH,
            bluetoothConectado = true,
            bluetoothDeviceName = "ESP32_Invernadero_BT"
        )
        assertEquals(ConnectionMode.BLUETOOTH, state.modoConexion)
        assertTrue(state.bluetoothConectado)
        assertEquals("ESP32_Invernadero_BT", state.bluetoothDeviceName)
    }

    @Test
    fun testGreenhouseNotificationHelper_ConstantsDefined() {
        assertEquals("greenhouse_iot_alerts_channel", GreenhouseNotificationHelper.CHANNEL_ID)
        assertEquals(1001, GreenhouseNotificationHelper.NOTIF_ID_TEMP)
        assertEquals(1002, GreenhouseNotificationHelper.NOTIF_ID_DROUGHT)
        assertEquals(1003, GreenhouseNotificationHelper.NOTIF_ID_FAILSAFE)
        assertEquals(1004, GreenhouseNotificationHelper.NOTIF_ID_MODE)
        assertEquals(1005, GreenhouseNotificationHelper.NOTIF_ID_TEST)
    }
}
