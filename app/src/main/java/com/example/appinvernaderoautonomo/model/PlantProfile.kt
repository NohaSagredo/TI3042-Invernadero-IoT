package com.example.appinvernaderoautonomo.model

data class PlantProfile(
    val id: String,
    val name: String,
    val description: String,
    val tempMin: Float,
    val tempMax: Float,
    val humidityMin: Float,
    val humidityMax: Float,
    val soilRawMin: Int,
    val soilRawMax: Int,
    val targetServoAngle: Int = 90,
    val targetFanPwm: Int = 180
) {
    val tempRangeText: String
        get() = "${tempMin}°C - ${tempMax}°C"

    val humidityRangeText: String
        get() = "${humidityMin.toInt()}% - ${humidityMax.toInt()}%"

    val soilRangeText: String
        get() = "Raw ${soilRawMin} - ${soilRawMax}"

    companion object {
        val PERFIL_TOMATE = PlantProfile(
            id = "TOMATE",
            name = "Cultivo de Tomate (Templado)",
            description = "Microclima óptimo para solanáceas en etapa de floración y cuajado.",
            tempMin = 20.0f,
            tempMax = 26.0f,
            humidityMin = 60.0f,
            humidityMax = 70.0f,
            soilRawMin = 450,
            soilRawMax = 650,
            targetServoAngle = 90,
            targetFanPwm = 200
        )

        val PERFIL_FRUTILLA = PlantProfile(
            id = "FRUTILLA",
            name = "Cultivo de Frutilla (Fresas)",
            description = "Clima fresco y ventilado con protección contra Botrytis cinerea.",
            tempMin = 15.0f,
            tempMax = 22.0f,
            humidityMin = 65.0f,
            humidityMax = 75.0f,
            soilRawMin = 500,
            soilRawMax = 700,
            targetServoAngle = 75,
            targetFanPwm = 220
        )

        val PERFIL_LECHUGA = PlantProfile(
            id = "LECHUGA",
            name = "Hortalizas / Lechuga",
            description = "Temperatura moderada y alta aireación para sustrato húmedo.",
            tempMin = 16.0f,
            tempMax = 24.0f,
            humidityMin = 50.0f,
            humidityMax = 68.0f,
            soilRawMin = 400,
            soilRawMax = 600,
            targetServoAngle = 60,
            targetFanPwm = 150
        )

        val PERFIL_PERSONALIZADO = PlantProfile(
            id = "PERSONALIZADO",
            name = "Modo Personalizado (Admin)",
            description = "Perfil con umbrales configurables manualmente por ingeniería.",
            tempMin = 18.0f,
            tempMax = 28.0f,
            humidityMin = 55.0f,
            humidityMax = 75.0f,
            soilRawMin = 420,
            soilRawMax = 680,
            targetServoAngle = 90,
            targetFanPwm = 200
        )

        val DEFAULT_PROFILES = listOf(
            PERFIL_TOMATE,
            PERFIL_FRUTILLA,
            PERFIL_LECHUGA,
            PERFIL_PERSONALIZADO
        )
    }
}
