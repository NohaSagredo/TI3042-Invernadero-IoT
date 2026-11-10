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
        iniciarLazoControl()
        iniciarMuestreoCincoMinutos()
    }

    private fun iniciarLazoControl() {
        loopJob?.cancel()
        loopJob = scope.launch {
            while (isActive) {
                delay(3000L) // Actualización periódica cada 3s
                actualizarCicloControl()
            }
        }
    }

    private fun iniciarMuestreoCincoMinutos() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            while (isActive) {
                delay(5 * 60 * 1000L) // Muestreo exacto cada 5 minutos
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

    private fun actualizarCicloControl() {
        val current = _state.value

        // Si se encuentra en modo Fail-Safe por corte Wi-Fi, el nodo físico ESP32
        // mantiene los actuadores en posición segura para la planta seleccionada
        if (!current.estaConectado || current.modoFailSafeBorde) {
            _state.value = current.copy(
                anguloVentana = 45,
                velocidadVentiladorPwm = 120,
                ultimoMensaje = "Alerta Fail-Safe: ESP32 en regulación biológica autónoma (${current.rutinaActiva.name})."
            )
            return
        }

        // Variación biológica natural leve de los sensores
        val deltaTemp = (Random.nextFloat() - 0.48f) * 0.2f
        val deltaHum = (Random.nextFloat() - 0.50f) * 0.3f
        val nuevaTemp = (current.temperatura + deltaTemp).coerceIn(14.0f, 35.0f)
        val nuevaHum = (current.humedadAmbiente + deltaHum).coerceIn(40.0f, 95.0f)

        var nuevoAngulo = current.anguloVentana
        var nuevoPwm = current.velocidadVentiladorPwm
        val rutina = current.rutinaActiva

        // Control autónomo de actuadores si no están en modo manual
        if (!current.modoManualVentana) {
            nuevoAngulo = when {
                nuevaTemp > rutina.tempMax -> rutina.targetServoAngle
                nuevaTemp < rutina.tempMin -> 0 // Cerrar herméticamente por frío
                else -> (rutina.targetServoAngle / 2) // Posición de confort
            }
        }

        if (!current.modoManualVentilador) {
            nuevoPwm = when {
                nuevaTemp > (rutina.tempMax + 1.0f) -> rutina.targetFanPwm
                nuevaHum > rutina.humidityMax -> 150 // Ventilación por humedad excesiva
                nuevaTemp < rutina.tempMin -> 0 // Apagar por bajas temperaturas
                else -> 0
            }
        }

        _state.value = current.copy(
            temperatura = String.format("%.1f", nuevaTemp).replace(',', '.').toFloat(),
            humedadAmbiente = String.format("%.1f", nuevaHum).replace(',', '.').toFloat(),
            anguloVentana = nuevoAngulo,
            velocidadVentiladorPwm = nuevoPwm,
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
            repository.recordAuditLog(
                eventType = AuditEventType.ROUTINE_CHANGED,
                details = "Rutina cambiada de '${prev.name}' a '${perfil.name}'.",
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
            repository.recordAuditLog(
                eventType = AuditEventType.MANUAL_OVERRIDE,
                details = "Forzado manual de compuerta a $anguloClamped°.",
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
}
