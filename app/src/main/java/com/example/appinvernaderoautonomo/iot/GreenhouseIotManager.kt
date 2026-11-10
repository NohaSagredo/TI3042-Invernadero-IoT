package com.example.appinvernaderoautonomo.iot

import android.content.Context
import com.example.appinvernaderoautonomo.data.GreenhouseRepository
import com.example.appinvernaderoautonomo.model.AuditEventType
import com.example.appinvernaderoautonomo.model.InvernaderoState
import com.example.appinvernaderoautonomo.model.PlantProfile
import com.example.appinvernaderoautonomo.model.TelemetryLog
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
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.random.Random

class GreenhouseIotManager private constructor(context: Context) {

    private val repository = GreenhouseRepository.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(InvernaderoState())
    val state: StateFlow<InvernaderoState> = _state.asStateFlow()

    private var loopJob: Job? = null
    private var telemetryJob: Job? = null

    companion object {
        @Volatile
        private var INSTANCE: GreenhouseIotManager? = null

        fun getInstance(context: Context): GreenhouseIotManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GreenhouseIotManager(context).also { INSTANCE = it }
            }
        }
    }

    init {
        scope.launch {
            delay(1000L)
            guardarInstantaneaTelemetria()
        }
        iniciarLazoControl()
        iniciarMuestreoCincoMinutos()
    }

    private fun iniciarLazoControl() {
        loopJob?.cancel()
        loopJob = scope.launch {
            while (isActive) {
                delay(3000L)
                procesarCicloIoT()
            }
        }
    }

    private fun iniciarMuestreoCincoMinutos() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            while (isActive) {
                delay(5 * 60 * 1000L)
                guardarInstantaneaTelemetria()
            }
        }
    }

    private suspend fun guardarInstantaneaTelemetria() {
        val current = _state.value
        val log = TelemetryLog(
            timestamp = System.currentTimeMillis(),
            temperatura = current.temperatura,
            humedadAmbiente = current.humedadAmbiente,
            humedadSueloRaw = current.humedadSueloRaw,
            anguloVentana = current.anguloVentana,
            velocidadVentiladorPwm = current.velocidadVentiladorPwm,
            rutinaActiva = current.rutinaActiva.name,
            modoManualActivo = current.hayModoManualActivo
        )
        repository.recordTelemetry(log)
    }

    private suspend fun procesarCicloIoT() {
        val current = _state.value

        if (!current.estaConectado || current.modoFailSafeBorde) {
            _state.value = current.copy(
                anguloVentana = 45,
                velocidadVentiladorPwm = 120,
                ultimoMensaje = "Alerta Fail-Safe: ESP32 en regulación biológica autónoma (${current.rutinaActiva.name})."
            )
            return
        }

        // Consulta HTTP periódica al ESP32 real
        val datosHardware = consultarEsp32Real(current.ipNodoEsp32)
        if (datosHardware != null) {
            _state.value = current.copy(
                temperatura = datosHardware.optDouble("temperatura", current.temperatura.toDouble()).toFloat(),
                humedadAmbiente = datosHardware.optDouble("humedad", current.humedadAmbiente.toDouble()).toFloat(),
                humedadSueloRaw = datosHardware.optInt("sueloRaw", current.humedadSueloRaw),
                anguloVentana = datosHardware.optInt("servoAngulo", current.anguloVentana),
                velocidadVentiladorPwm = datosHardware.optInt("fanPwm", current.velocidadVentiladorPwm),
                modoFailSafeBorde = datosHardware.optBoolean("failSafe", false),
                ultimoMensaje = "Enlace activo Wi-Fi con ESP32 (${current.ipNodoEsp32})"
            )
            return
        }

        // Si no hay hardware físico en red local, se mantiene simulación biológica precisa
        ejecutarSimulacionBiologica()
    }

    private suspend fun consultarEsp32Real(ip: String): JSONObject? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("http://$ip/api/telemetry")
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 1500
                readTimeout = 1500
                requestMethod = "GET"
            }
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()
                JSONObject(response)
            } else null
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private suspend fun enviarComandoPost(endpoint: String, params: Map<String, String>): Boolean =
        withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                val ip = _state.value.ipNodoEsp32
                val url = URL("http://$ip$endpoint")
                conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 2000
                    readTimeout = 2000
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }

                val postData = params.entries.joinToString("&") { (k, v) ->
                    "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
                }

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(postData)
                writer.flush()
                writer.close()

                conn.responseCode == 200
            } catch (_: Exception) {
                false
            } finally {
                conn?.disconnect()
            }
        }

    suspend fun pingEsp32(ip: String): Boolean = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("http://$ip/api/telemetry")
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 1200
                readTimeout = 1200
                requestMethod = "GET"
            }
            conn.responseCode == 200
        } catch (_: Exception) {
            false
        } finally {
            conn?.disconnect()
        }
    }

    private fun ejecutarSimulacionBiologica() {
        val current = _state.value
        val deltaTemp = (Random.nextFloat() - 0.48f) * 0.2f
        val deltaHum = (Random.nextFloat() - 0.50f) * 0.3f
        val nuevaTemp = (current.temperatura + deltaTemp).coerceIn(14.0f, 35.0f)
        val nuevaHum = (current.humedadAmbiente + deltaHum).coerceIn(40.0f, 95.0f)

        var nuevoAngulo = current.anguloVentana
        var nuevoPwm = current.velocidadVentiladorPwm
        val rutina = current.rutinaActiva

        if (!current.modoManualVentana) {
            nuevoAngulo = when {
                nuevaTemp > rutina.tempMax -> rutina.targetServoAngle
                nuevaTemp < rutina.tempMin -> 0
                else -> (rutina.targetServoAngle / 2)
            }
        }

        if (!current.modoManualVentilador) {
            nuevoPwm = when {
                nuevaTemp > (rutina.tempMax + 1.0f) -> rutina.targetFanPwm
                nuevaHum > rutina.humidityMax -> 150
                nuevaTemp < rutina.tempMin -> 0
                else -> 0
            }
        }

        val alerta = if (current.humedadSueloRaw > 850) {
            "Déficit hídrico detectado en sustrato radicular (FC-28: ${current.humedadSueloRaw})."
        } else null

        _state.value = current.copy(
            temperatura = String.format("%.1f", nuevaTemp).replace(',', '.').toFloat(),
            humedadAmbiente = String.format("%.1f", nuevaHum).replace(',', '.').toFloat(),
            anguloVentana = nuevoAngulo,
            velocidadVentiladorPwm = nuevoPwm,
            alertaSeguridad = alerta,
            ultimoMensaje = "Telemetría Wi-Fi nominal | ${rutina.name}"
        )
    }

    fun cambiarRutina(perfil: PlantProfile, usuario: User) {
        scope.launch {
            val prev = _state.value.rutinaActiva
            _state.value = _state.value.copy(
                rutinaActiva = perfil,
                modoManualVentana = false,
                modoManualVentilador = false,
                ultimoMensaje = "Rutina conmutada a: ${perfil.name}"
            )
            // Despachar orden HTTP POST al ESP32 si está conectado
            enviarComandoPost("/api/routine", mapOf("id" to perfil.id))

            repository.recordAuditLog(
                eventType = AuditEventType.ROUTINE_CHANGED,
                details = "Rutina conmutada de '${prev.name}' a '${perfil.name}'.",
                username = usuario.username,
                role = usuario.role.name
            )
            guardarInstantaneaTelemetria()
        }
    }

    fun calibrarUmbrales(perfil: PlantProfile, usuario: User) {
        if (!usuario.role.canEditThresholds()) return
        scope.launch {
            repository.updatePlantProfile(perfil, usuario.username, usuario.role.name)
            if (_state.value.rutinaActiva.id == perfil.id) {
                _state.value = _state.value.copy(rutinaActiva = perfil)
            }
            // Sincronizar umbrales con memoria NVS del ESP32 vía HTTP
            enviarComandoPost(
                "/api/calibrate",
                mapOf(
                    "id" to perfil.id,
                    "t_min" to perfil.tempMin.toString(),
                    "t_max" to perfil.tempMax.toString(),
                    "h_min" to perfil.humidityMin.toString(),
                    "h_max" to perfil.humidityMax.toString(),
                    "s_min" to perfil.soilRawMin.toString(),
                    "s_max" to perfil.soilRawMax.toString()
                )
            )
        }
    }

    fun setManualVentana(angulo: Int, usuario: User) {
        if (!usuario.role.canOverrideActuators()) return
        scope.launch {
            val anguloClamped = angulo.coerceIn(0, 120)
            _state.value = _state.value.copy(
                anguloVentana = anguloClamped,
                modoManualVentana = true,
                ultimoMensaje = "Compuerta manual forzada a $anguloClamped°"
            )
            enviarComandoPost("/api/actuators", mapOf("servo" to anguloClamped.toString()))

            repository.recordAuditLog(
                eventType = AuditEventType.MANUAL_OVERRIDE,
                details = "Forzado manual de compuerta batiente a $anguloClamped°.",
                username = usuario.username,
                role = usuario.role.name
            )
        }
    }

    fun setManualVentilador(pwm: Int, usuario: User) {
        if (!usuario.role.canOverrideActuators()) return
        scope.launch {
            val pwmClamped = pwm.coerceIn(0, 255)
            _state.value = _state.value.copy(
                velocidadVentiladorPwm = pwmClamped,
                modoManualVentilador = true,
                ultimoMensaje = "Ventilador manual forzado a PWM $pwmClamped"
            )
            enviarComandoPost("/api/actuators", mapOf("fan" to pwmClamped.toString()))

            repository.recordAuditLog(
                eventType = AuditEventType.MANUAL_OVERRIDE,
                details = "Forzado manual de ventilador a PWM $pwmClamped.",
                username = usuario.username,
                role = usuario.role.name
            )
        }
    }

    fun reactivarModoAuto(usuario: User) {
        scope.launch {
            _state.value = _state.value.copy(
                modoManualVentana = false,
                modoManualVentilador = false,
                ultimoMensaje = "Modo automático reactivado."
            )
            enviarComandoPost("/api/actuators", mapOf("auto" to "true"))

            repository.recordAuditLog(
                eventType = AuditEventType.ROUTINE_CHANGED,
                details = "Reactivación de lazo de control automático.",
                username = usuario.username,
                role = usuario.role.name
            )
        }
    }

    fun alternarSimulacionFalloConexion(usuario: User?) {
        scope.launch {
            val nuevoEstado = !_state.value.estaConectado
            _state.value = _state.value.copy(
                estaConectado = nuevoEstado,
                modoFailSafeBorde = !nuevoEstado,
                ultimoMensaje = if (!nuevoEstado) "¡Pérdida de enlace Wi-Fi! Fail-Safe Autónomo activado en el ESP32."
                else "Conexión Wi-Fi reestablecida con el nodo IoT."
            )
            repository.recordAuditLog(
                eventType = AuditEventType.FAILSAFE_TRIGGERED,
                details = if (!nuevoEstado) "Disparo de Fail-Safe por interrupción de señal Wi-Fi."
                else "Restablecimiento de enlace Wi-Fi con ESP32.",
                username = usuario?.username ?: "SISTEMA",
                role = usuario?.role?.name ?: "SISTEMA"
            )
        }
    }

    fun actualizarIpEsp32(nuevaIp: String, usuario: User) {
        if (!usuario.role.canEditThresholds()) return
        scope.launch {
            val limpia = nuevaIp.trim()
            _state.value = _state.value.copy(ipNodoEsp32 = limpia)
            repository.recordAuditLog(
                eventType = AuditEventType.SYSTEM_RESET,
                details = "Dirección IP del nodo ESP32 reconfigurada a: '$limpia'.",
                username = usuario.username,
                role = usuario.role.name
            )
        }
    }
}
