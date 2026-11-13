package com.example.appinvernaderoautonomo.model

enum class ConnectionMode(
    val title: String,
    val subtitle: String,
    val badgeColorHex: Long
) {
    SIMULACION("Modo Simulación", "Ambiente de prueba sintético interno", 0xFF6366F1),
    WIFI("Modo Wi-Fi", "Conexión directa REST/HTTP con nodo ESP32", 0xFF10B981),
    BLUETOOTH("Modo Bluetooth", "Enlace inalámbrico serial local SPP", 0xFF2563EB)
}

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
    val modoConexion: ConnectionMode = ConnectionMode.SIMULACION,
    val nombreDispositivo: String = "ESP32 Invernadero (Simulado)",
    val ipNodoEsp32: String = "192.168.4.1",
    val tipoConexion: String = "Simulación Interna",
    val ultimoMensaje: String = "Telemetría nominal en Modo Simulación",
    val rutinaActiva: PlantProfile = PlantProfile.PERFIL_TOMATE,
    val modoFailSafeBorde: Boolean = false,
    val alertaSeguridad: String? = null,
    val bluetoothDeviceName: String = "ESP32_Invernadero_BT",
    val bluetoothMac: String = "94:B9:7E:8B:12:34",
    val bluetoothConectado: Boolean = false
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
