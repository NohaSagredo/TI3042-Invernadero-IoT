package com.example.appinvernaderoautonomo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.appinvernaderoautonomo.model.AuditEventType
import com.example.appinvernaderoautonomo.model.AuditLog
import com.example.appinvernaderoautonomo.model.PlantProfile
import com.example.appinvernaderoautonomo.model.Role
import com.example.appinvernaderoautonomo.model.TelemetryLog
import com.example.appinvernaderoautonomo.model.User
import com.example.appinvernaderoautonomo.security.FailSafeLockoutManager
import com.example.appinvernaderoautonomo.security.PasswordHasher
import com.example.appinvernaderoautonomo.security.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Failure(
        val message: String,
        val isLockedOut: Boolean = false,
        val remainingSeconds: Int = 0
    ) : AuthResult()
}

class GreenhouseRepository private constructor(context: Context) {

    private val dbHelper = GreenhouseDbHelper(context.applicationContext)

    companion object {
        @Volatile
        private var INSTANCE: GreenhouseRepository? = null

        fun getInstance(context: Context): GreenhouseRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GreenhouseRepository(context).also { INSTANCE = it }
            }
        }
    }

    suspend fun authenticate(username: String, passwordCandidate: String): AuthResult =
        withContext(Dispatchers.IO) {
            // 1. Verificar si el sistema ya está bajo bloqueo Fail-Safe global o por IP/dispositivo
            if (FailSafeLockoutManager.isLockedOut()) {
                val remaining = FailSafeLockoutManager.getRemainingSeconds()
                return@withContext AuthResult.Failure(
                    message = "Sistema bloqueado por seguridad Fail-Safe. Espere que finalice el temporizador.",
                    isLockedOut = true,
                    remainingSeconds = remaining
                )
            }

            val db = dbHelper.readableDatabase
            val cursor = db.query(
                GreenhouseDbHelper.TABLE_USERS,
                null,
                "${GreenhouseDbHelper.COL_USER_USERNAME} = ?",
                arrayOf(username.trim().lowercase()),
                null,
                null,
                null
            )

            var user: User? = null
            if (cursor.moveToFirst()) {
                user = parseUser(cursor)
            }
            cursor.close()

            if (user == null) {
                // Registrar intento fallido
                val lockedNow = FailSafeLockoutManager.registerFailedAttempt()
                val remaining = FailSafeLockoutManager.getRemainingSeconds()
                recordAuditLogInternal(
                    eventType = if (lockedNow) AuditEventType.LOCKOUT_ACTIVATED else AuditEventType.LOGIN_FAILED,
                    details = "Intento de inicio de sesión con usuario inexistente: '$username'",
                    username = username.ifBlank { "DESCONOCIDO" },
                    role = "INVITADO"
                )
                return@withContext AuthResult.Failure(
                    message = if (lockedNow) "Bloqueo Fail-Safe activado tras múltiples intentos fallidos."
                    else "Credenciales no válidas. Verifique usuario y contraseña.",
                    isLockedOut = lockedNow,
                    remainingSeconds = remaining
                )
            }

            // Verificar si este usuario específico tiene bloqueo activo en BD
            if (user.isLockedOut) {
                val remaining = ((user.lockoutUntilTimestamp - System.currentTimeMillis()) / 1000).toInt()
                return@withContext AuthResult.Failure(
                    message = "Cuenta temporalmente inhabilitada por seguridad.",
                    isLockedOut = true,
                    remainingSeconds = remaining.coerceAtLeast(1)
                )
            }

            // 2. Verificar Hash Criptográfico PBKDF2
            val isValid = PasswordHasher.verifyPassword(
                password = passwordCandidate,
                saltHex = user.saltHex,
                storedHashHex = user.passwordHash
            )

            if (!isValid) {
                val lockedNow = FailSafeLockoutManager.registerFailedAttempt()
                val remaining = FailSafeLockoutManager.getRemainingSeconds()

                // Actualizar intentos en la base de datos
                val writeDb = dbHelper.writableDatabase
                val newFailed = user.failedAttempts + 1
                val lockoutUntil = if (lockedNow) System.currentTimeMillis() + FailSafeLockoutManager.LOCKOUT_DURATION_MS else 0L

                val cv = ContentValues().apply {
                    put(GreenhouseDbHelper.COL_USER_FAILED_ATTEMPTS, newFailed)
                    put(GreenhouseDbHelper.COL_USER_LOCKOUT_UNTIL, lockoutUntil)
                }
                writeDb.update(
                    GreenhouseDbHelper.TABLE_USERS,
                    cv,
                    "${GreenhouseDbHelper.COL_USER_ID} = ?",
                    arrayOf(user.id.toString())
                )

                recordAuditLogInternal(
                    eventType = if (lockedNow) AuditEventType.LOCKOUT_ACTIVATED else AuditEventType.LOGIN_FAILED,
                    details = "Contraseña incorrecta para el usuario: '${user.username}'. Intento #$newFailed.",
                    username = user.username,
                    role = user.role.name
                )

                return@withContext AuthResult.Failure(
                    message = if (lockedNow) "Acceso bloqueado durante 3 minutos por activación de regla Fail-Safe."
                    else "Contraseña incorrecta. Intentos restantes antes del bloqueo: ${FailSafeLockoutManager.MAX_FAILED_ATTEMPTS - (newFailed % FailSafeLockoutManager.MAX_FAILED_ATTEMPTS)}",
                    isLockedOut = lockedNow,
                    remainingSeconds = remaining
                )
            }

            // 3. Éxito: Limpiar contador de fallos
            val writeDb = dbHelper.writableDatabase
            val cvReset = ContentValues().apply {
                put(GreenhouseDbHelper.COL_USER_FAILED_ATTEMPTS, 0)
                put(GreenhouseDbHelper.COL_USER_LOCKOUT_UNTIL, 0)
            }
            writeDb.update(
                GreenhouseDbHelper.TABLE_USERS,
                cvReset,
                "${GreenhouseDbHelper.COL_USER_ID} = ?",
                arrayOf(user.id.toString())
            )

            FailSafeLockoutManager.reset()
            SessionManager.login(user)

            recordAuditLogInternal(
                eventType = AuditEventType.LOGIN_SUCCESS,
                details = "Inicio de sesión correcto con rol ${user.role.displayTitle}.",
                username = user.username,
                role = user.role.name
            )

            return@withContext AuthResult.Success(user)
        }

    suspend fun getPlantProfiles(): List<PlantProfile> = withContext(Dispatchers.IO) {
        val list = mutableListOf<PlantProfile>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(GreenhouseDbHelper.TABLE_PROFILES, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            list.add(parsePlantProfile(cursor))
        }
        cursor.close()
        list
    }

    suspend fun updatePlantProfile(profile: PlantProfile, editorUsername: String, editorRole: String): Boolean =
        withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            val cv = ContentValues().apply {
                put(GreenhouseDbHelper.COL_PROF_NAME, profile.name)
                put(GreenhouseDbHelper.COL_PROF_DESC, profile.description)
                put(GreenhouseDbHelper.COL_PROF_TEMP_MIN, profile.tempMin)
                put(GreenhouseDbHelper.COL_PROF_TEMP_MAX, profile.tempMax)
                put(GreenhouseDbHelper.COL_PROF_HUM_MIN, profile.humidityMin)
                put(GreenhouseDbHelper.COL_PROF_HUM_MAX, profile.humidityMax)
                put(GreenhouseDbHelper.COL_PROF_SOIL_MIN, profile.soilRawMin)
                put(GreenhouseDbHelper.COL_PROF_SOIL_MAX, profile.soilRawMax)
                put(GreenhouseDbHelper.COL_PROF_SERVO, profile.targetServoAngle)
                put(GreenhouseDbHelper.COL_PROF_FAN, profile.targetFanPwm)
            }
            val rows = db.update(
                GreenhouseDbHelper.TABLE_PROFILES,
                cv,
                "${GreenhouseDbHelper.COL_PROF_ID} = ?",
                arrayOf(profile.id)
            )

            if (rows > 0) {
                recordAuditLogInternal(
                    eventType = AuditEventType.THRESHOLD_MODIFIED,
                    details = "Calibración de umbrales para ${profile.name}: Temp [${profile.tempMin}-${profile.tempMax}°C], Hum [${profile.humidityMin}-${profile.humidityMax}%].",
                    username = editorUsername,
                    role = editorRole
                )
            }
            rows > 0
        }

    suspend fun recordTelemetry(log: TelemetryLog): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(GreenhouseDbHelper.COL_TEL_TIMESTAMP, log.timestamp)
            put(GreenhouseDbHelper.COL_TEL_TEMP, log.temperatura)
            put(GreenhouseDbHelper.COL_TEL_HUM_AIR, log.humedadAmbiente)
            put(GreenhouseDbHelper.COL_TEL_HUM_SOIL, log.humedadSueloRaw)
            put(GreenhouseDbHelper.COL_TEL_SERVO, log.anguloVentana)
            put(GreenhouseDbHelper.COL_TEL_FAN, log.velocidadVentiladorPwm)
            put(GreenhouseDbHelper.COL_TEL_ROUTINE, log.rutinaActiva)
            put(GreenhouseDbHelper.COL_TEL_MANUAL, if (log.modoManualActivo) 1 else 0)
        }
        db.insert(GreenhouseDbHelper.TABLE_TELEMETRY, null, cv)
    }

    suspend fun getRecentTelemetry(limit: Int = 30): List<TelemetryLog> = withContext(Dispatchers.IO) {
        val list = mutableListOf<TelemetryLog>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            GreenhouseDbHelper.TABLE_TELEMETRY,
            null,
            null,
            null,
            null,
            null,
            "${GreenhouseDbHelper.COL_TEL_TIMESTAMP} DESC",
            limit.toString()
        )
        while (cursor.moveToNext()) {
            list.add(parseTelemetry(cursor))
        }
        cursor.close()
        list
    }

    suspend fun recordAuditLog(eventType: AuditEventType, details: String, username: String, role: String): Long =
        withContext(Dispatchers.IO) {
            recordAuditLogInternal(eventType, details, username, role)
        }

    private fun recordAuditLogInternal(
        eventType: AuditEventType,
        details: String,
        username: String,
        role: String
    ): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(GreenhouseDbHelper.COL_AUD_TIMESTAMP, System.currentTimeMillis())
            put(GreenhouseDbHelper.COL_AUD_USERNAME, username)
            put(GreenhouseDbHelper.COL_AUD_ROLE, role)
            put(GreenhouseDbHelper.COL_AUD_TYPE, eventType.name)
            put(GreenhouseDbHelper.COL_AUD_DETAILS, details)
        }
        return db.insert(GreenhouseDbHelper.TABLE_AUDIT, null, cv)
    }

    suspend fun getAuditLogs(limit: Int = 50): List<AuditLog> = withContext(Dispatchers.IO) {
        val list = mutableListOf<AuditLog>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            GreenhouseDbHelper.TABLE_AUDIT,
            null,
            null,
            null,
            null,
            null,
            "${GreenhouseDbHelper.COL_AUD_TIMESTAMP} DESC",
            limit.toString()
        )
        while (cursor.moveToNext()) {
            list.add(parseAudit(cursor))
        }
        cursor.close()
        list
    }

    private fun parseUser(cursor: Cursor): User {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_USER_ID))
        val username = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_USER_USERNAME))
        val hash = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_USER_PASSWORD_HASH))
        val salt = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_USER_SALT_HEX))
        val roleStr = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_USER_ROLE))
        val fullName = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_USER_FULL_NAME))
        val failed = cursor.getInt(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_USER_FAILED_ATTEMPTS))
        val lockoutUntil = cursor.getLong(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_USER_LOCKOUT_UNTIL))

        val role = try {
            Role.valueOf(roleStr)
        } catch (e: Exception) {
            Role.OPERATOR
        }

        return User(
            id = id,
            username = username,
            passwordHash = hash,
            saltHex = salt,
            role = role,
            fullName = fullName,
            failedAttempts = failed,
            lockoutUntilTimestamp = lockoutUntil
        )
    }

    private fun parsePlantProfile(cursor: Cursor): PlantProfile {
        return PlantProfile(
            id = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_PROF_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_PROF_NAME)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_PROF_DESC)),
            tempMin = cursor.getFloat(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_PROF_TEMP_MIN)),
            tempMax = cursor.getFloat(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_PROF_TEMP_MAX)),
            humidityMin = cursor.getFloat(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_PROF_HUM_MIN)),
            humidityMax = cursor.getFloat(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_PROF_HUM_MAX)),
            soilRawMin = cursor.getInt(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_PROF_SOIL_MIN)),
            soilRawMax = cursor.getInt(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_PROF_SOIL_MAX)),
            targetServoAngle = cursor.getInt(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_PROF_SERVO)),
            targetFanPwm = cursor.getInt(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_PROF_FAN))
        )
    }

    private fun parseTelemetry(cursor: Cursor): TelemetryLog {
        return TelemetryLog(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_TEL_ID)),
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_TEL_TIMESTAMP)),
            temperatura = cursor.getFloat(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_TEL_TEMP)),
            humedadAmbiente = cursor.getFloat(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_TEL_HUM_AIR)),
            humedadSueloRaw = cursor.getInt(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_TEL_HUM_SOIL)),
            anguloVentana = cursor.getInt(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_TEL_SERVO)),
            velocidadVentiladorPwm = cursor.getInt(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_TEL_FAN)),
            rutinaActiva = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_TEL_ROUTINE)),
            modoManualActivo = cursor.getInt(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_TEL_MANUAL)) == 1
        )
    }

    private fun parseAudit(cursor: Cursor): AuditLog {
        val typeStr = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_AUD_TYPE))
        val eventType = try {
            AuditEventType.valueOf(typeStr)
        } catch (e: Exception) {
            AuditEventType.SYSTEM_RESET
        }

        return AuditLog(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_AUD_ID)),
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_AUD_TIMESTAMP)),
            username = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_AUD_USERNAME)),
            userRole = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_AUD_ROLE)),
            eventType = eventType,
            details = cursor.getString(cursor.getColumnIndexOrThrow(GreenhouseDbHelper.COL_AUD_DETAILS))
        )
    }
}
