package com.example.appinvernaderoautonomo.model

enum class EstadoSuelo(val titulo: String) {
    SOBRE_REGADA("Sobre regada"),
    OPTIMA("Regada (Óptima)"),
    CONSUMIENDO_AGUA("Consumiendo agua"),
    FALTA_AGUA("Falta de agua"),
    TOTALMENTE_SECA("Totalmente seca");

    companion object {
        fun desdeValorRaw(valorRaw: Int): EstadoSuelo {
            return when {
                valorRaw < 400 -> SOBRE_REGADA
                valorRaw <= 600 -> OPTIMA
                valorRaw <= 900 -> CONSUMIENDO_AGUA
                valorRaw <= 1023 -> FALTA_AGUA
                else -> TOTALMENTE_SECA
            }
        }
    }
}

data class InvernaderoState(
    val temperatura: Float = 24.2f,
    val humedadAmbiente: Float = 63.5f,
    val humedadSueloRaw: Int = 540,
    val anguloVentana: Int = 60,
    val modoManualVentana: Boolean = false,
    val velocidadVentiladorPwm: Int = 0,
    val modoManualVentilador: Boolean = false,
    val estaConectado: Boolean = true,
    val nombreDispositivo: String = "ESP32 Invernadero (Wi-Fi)",
    val ipNodoEsp32: String = "192.168.4.1",
    val tipoConexion: String = "Wi-Fi Seguro (TLS/WPA2)",
    val ultimoMensaje: String = "Telemetría recibida vía Wi-Fi",
    val rutinaActiva: PlantProfile = PlantProfile.PERFIL_TOMATE,
    val modoFailSafeBorde: Boolean = false,
    val alertaSeguridad: String? = null
) {
    val estadoSuelo: EstadoSuelo
        get() = EstadoSuelo.desdeValorRaw(humedadSueloRaw)

    val porcentajeVentilador: Int
        get() = ((velocidadVentiladorPwm.coerceIn(0, 255) / 255f) * 100).toInt()

    val porcentajeVentanaAbierta: Int
        get() = (((120 - anguloVentana.coerceIn(0, 120)) / 120f) * 100).toInt()

    val hayModoManualActivo: Boolean
        get() = modoManualVentana || modoManualVentilador

    val estaEnRangoTemperatura: Boolean
        get() = temperatura in rutinaActiva.tempMin..rutinaActiva.tempMax

    val estaEnRangoHumedad: Boolean
        get() = humedadAmbiente in rutinaActiva.humidityMin..rutinaActiva.humidityMax

    val estaEnRangoSuelo: Boolean
        get() = humedadSueloRaw in rutinaActiva.soilRawMin..rutinaActiva.soilRawMax
}
