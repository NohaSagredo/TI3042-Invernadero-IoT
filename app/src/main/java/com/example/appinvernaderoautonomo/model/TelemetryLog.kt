package com.example.appinvernaderoautonomo.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TelemetryLog(
    val id: Long = 0,
    val timestamp: Long,
    val temperatura: Float,
    val humedadAmbiente: Float,
    val humedadSueloRaw: Int,
    val anguloVentana: Int,
    val velocidadVentiladorPwm: Int,
    val rutinaActiva: String,
    val modoManualActivo: Boolean
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

    val porcentajeVentilador: Int
        get() = ((velocidadVentiladorPwm.coerceIn(0, 255) / 255f) * 100).toInt()
}
