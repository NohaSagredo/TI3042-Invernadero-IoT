package com.example.appinvernaderoautonomo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.appinvernaderoautonomo.model.AuditEventType
import com.example.appinvernaderoautonomo.model.PlantProfile
import com.example.appinvernaderoautonomo.model.Role
import com.example.appinvernaderoautonomo.security.PasswordHasher

class GreenhouseDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "greenhouse_iot.db"
        const val DATABASE_VERSION = 2

        // Tabla Usuarios
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USER_USERNAME = "username"
        const val COL_USER_PASSWORD_HASH = "password_hash"
        const val COL_USER_SALT_HEX = "salt_hex"
        const val COL_USER_ROLE = "role"
        const val COL_USER_FULL_NAME = "full_name"
        const val COL_USER_FAILED_ATTEMPTS = "failed_attempts"
        const val COL_USER_LOCKOUT_UNTIL = "lockout_until"

        // Tabla Perfiles de Cultivo
        const val TABLE_PROFILES = "plant_profiles"
        const val COL_PROF_ID = "id"
        const val COL_PROF_NAME = "name"
        const val COL_PROF_DESC = "description"
        const val COL_PROF_TEMP_MIN = "temp_min"
        const val COL_PROF_TEMP_MAX = "temp_max"
        const val COL_PROF_HUM_MIN = "humidity_min"
        const val COL_PROF_HUM_MAX = "humidity_max"
        const val COL_PROF_SOIL_MIN = "soil_raw_min"
        const val COL_PROF_SOIL_MAX = "soil_raw_max"
        const val COL_PROF_SERVO = "target_servo_angle"
        const val COL_PROF_FAN = "target_fan_pwm"

        // Tabla Telemetría Periódica (cada 5 minutos)
        const val TABLE_TELEMETRY = "telemetry_logs"
        const val COL_TEL_ID = "id"
        const val COL_TEL_TIMESTAMP = "timestamp"
        const val COL_TEL_TEMP = "temperatura"
        const val COL_TEL_HUM_AIR = "humedad_ambiente"
        const val COL_TEL_HUM_SOIL = "humedad_suelo_raw"
        const val COL_TEL_SERVO = "angulo_ventana"
        const val COL_TEL_FAN = "velocidad_ventilador_pwm"
        const val COL_TEL_ROUTINE = "rutina_activa"
        const val COL_TEL_MANUAL = "modo_manual_activo"

        // Tabla Auditoría de Seguridad
        const val TABLE_AUDIT = "audit_logs"
        const val COL_AUD_ID = "id"
        const val COL_AUD_TIMESTAMP = "timestamp"
        const val COL_AUD_USERNAME = "username"
        const val COL_AUD_ROLE = "user_role"
        const val COL_AUD_TYPE = "event_type"
        const val COL_AUD_DETAILS = "details"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_USERNAME TEXT UNIQUE NOT NULL,
                $COL_USER_PASSWORD_HASH TEXT NOT NULL,
                $COL_USER_SALT_HEX TEXT NOT NULL,
                $COL_USER_ROLE TEXT NOT NULL,
                $COL_USER_FULL_NAME TEXT NOT NULL,
                $COL_USER_FAILED_ATTEMPTS INTEGER DEFAULT 0,
                $COL_USER_LOCKOUT_UNTIL INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_PROFILES (
                $COL_PROF_ID TEXT PRIMARY KEY,
                $COL_PROF_NAME TEXT NOT NULL,
                $COL_PROF_DESC TEXT NOT NULL,
                $COL_PROF_TEMP_MIN REAL NOT NULL,
                $COL_PROF_TEMP_MAX REAL NOT NULL,
                $COL_PROF_HUM_MIN REAL NOT NULL,
                $COL_PROF_HUM_MAX REAL NOT NULL,
                $COL_PROF_SOIL_MIN INTEGER NOT NULL,
                $COL_PROF_SOIL_MAX INTEGER NOT NULL,
                $COL_PROF_SERVO INTEGER NOT NULL,
                $COL_PROF_FAN INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_TELEMETRY (
                $COL_TEL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TEL_TIMESTAMP INTEGER NOT NULL,
                $COL_TEL_TEMP REAL NOT NULL,
                $COL_TEL_HUM_AIR REAL NOT NULL,
                $COL_TEL_HUM_SOIL INTEGER NOT NULL,
                $COL_TEL_SERVO INTEGER NOT NULL,
                $COL_TEL_FAN INTEGER NOT NULL,
                $COL_TEL_ROUTINE TEXT NOT NULL,
                $COL_TEL_MANUAL INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_AUDIT (
                $COL_AUD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_AUD_TIMESTAMP INTEGER NOT NULL,
                $COL_AUD_USERNAME TEXT NOT NULL,
                $COL_AUD_ROLE TEXT NOT NULL,
                $COL_AUD_TYPE TEXT NOT NULL,
                $COL_AUD_DETAILS TEXT NOT NULL
            )
            """.trimIndent()
        )

        precargarDatosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TELEMETRY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AUDIT")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        asegurarUsuarioVisor(db)
    }

    private fun asegurarUsuarioVisor(db: SQLiteDatabase) {
        try {
            val cursor = db.rawQuery(
                "SELECT 1 FROM $TABLE_USERS WHERE $COL_USER_USERNAME = ?",
                arrayOf("pantalla")
            )
            val existe = cursor.moveToFirst()
            cursor.close()
            if (!existe) {
                insertarUsuario(db, "pantalla", "pantalla123", Role.VISOR, "Pantalla Invernadero (Visor en Terreno)")
            }
        } catch (_: Exception) {
        }
    }

    private fun precargarDatosIniciales(db: SQLiteDatabase) {
        // 1. Usuarios con PBKDF2 + Salt 128-bit
        insertarUsuario(db, "admin", "admin123", Role.ADMIN, "Rodrigo Peña (Administrador)")
        insertarUsuario(db, "operador", "operador123", Role.OPERATOR, "Sebastián Sandoval (Operador)")
        insertarUsuario(db, "auditor", "auditor123", Role.AUDITOR, "Auditor de Ciberseguridad TI3042")
        insertarUsuario(db, "pantalla", "pantalla123", Role.VISOR, "Pantalla Invernadero (Visor en Terreno)")

        // 2. Perfiles de cultivo agronómicos
        PlantProfile.DEFAULT_PROFILES.forEach { perfil ->
            val cv = ContentValues().apply {
                put(COL_PROF_ID, perfil.id)
                put(COL_PROF_NAME, perfil.name)
                put(COL_PROF_DESC, perfil.description)
                put(COL_PROF_TEMP_MIN, perfil.tempMin)
                put(COL_PROF_TEMP_MAX, perfil.tempMax)
                put(COL_PROF_HUM_MIN, perfil.humidityMin)
                put(COL_PROF_HUM_MAX, perfil.humidityMax)
                put(COL_PROF_SOIL_MIN, perfil.soilRawMin)
                put(COL_PROF_SOIL_MAX, perfil.soilRawMax)
                put(COL_PROF_SERVO, perfil.targetServoAngle)
                put(COL_PROF_FAN, perfil.targetFanPwm)
            }
            db.insert(TABLE_PROFILES, null, cv)
        }

        // 3. Muestras iniciales de telemetría periódica (cada 5 minutos)
        val ahora = System.currentTimeMillis()
        val cincoMinMs = 5 * 60 * 1000L
        for (i in 6 downTo 1) {
            val ts = ahora - (i * cincoMinMs)
            val cvTel = ContentValues().apply {
                put(COL_TEL_TIMESTAMP, ts)
                put(COL_TEL_TEMP, 23.5f + (i * 0.4f))
                put(COL_TEL_HUM_AIR, 62.0f + (i * 0.8f))
                put(COL_TEL_HUM_SOIL, 510 + (i * 10))
                put(COL_TEL_SERVO, 60)
                put(COL_TEL_FAN, 120)
                put(COL_TEL_ROUTINE, "TOMATE")
                put(COL_TEL_MANUAL, 0)
            }
            db.insert(TABLE_TELEMETRY, null, cvTel)
        }

        // 4. Registro inicial de auditoría
        val cvAud = ContentValues().apply {
            put(COL_AUD_TIMESTAMP, ahora - (35 * 60 * 1000L))
            put(COL_AUD_USERNAME, "SISTEMA")
            put(COL_AUD_ROLE, "AUDITOR")
            put(COL_AUD_TYPE, AuditEventType.SYSTEM_RESET.name)
            put(COL_AUD_DETAILS, "Base de datos inicializada con estándares de seguridad ISO 27400 y hash PBKDF2.")
        }
        db.insert(TABLE_AUDIT, null, cvAud)
    }

    private fun insertarUsuario(
        db: SQLiteDatabase,
        username: String,
        clavePlana: String,
        rol: Role,
        nombreCompleto: String
    ) {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hashPassword(clavePlana, salt)
        val saltHex = PasswordHasher.bytesToHex(salt)

        val cv = ContentValues().apply {
            put(COL_USER_USERNAME, username)
            put(COL_USER_PASSWORD_HASH, hash)
            put(COL_USER_SALT_HEX, saltHex)
            put(COL_USER_ROLE, rol.name)
            put(COL_USER_FULL_NAME, nombreCompleto)
            put(COL_USER_FAILED_ATTEMPTS, 0)
            put(COL_USER_LOCKOUT_UNTIL, 0)
        }
        db.insert(TABLE_USERS, null, cv)
    }
}
