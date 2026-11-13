package com.example.appinvernaderoautonomo.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.appinvernaderoautonomo.MainActivity

object GreenhouseNotificationHelper {

    const val CHANNEL_ID = "greenhouse_iot_alerts_channel"
    private const val CHANNEL_NAME = "Alertas del Invernadero IoT"
    private const val CHANNEL_DESC = "Notificaciones push locales para temperaturas críticas, sequía y seguridad"

    const val NOTIF_ID_TEMP = 1001
    const val NOTIF_ID_DROUGHT = 1002
    const val NOTIF_ID_FAILSAFE = 1003
    const val NOTIF_ID_MODE = 1004
    const val NOTIF_ID_TEST = 1005

    // Control de frecuencia para no saturar al usuario (mínimo 20 segundos entre alertas del mismo tipo)
    private var lastTempNotifTime = 0L
    private var lastDroughtNotifTime = 0L
    private var lastFailSafeNotifTime = 0L
    private const val COOLDOWN_MS = 20_000L

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                enableLights(true)
                lightColor = 0xFF1E7E34.toInt()
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        isCritical: Boolean = false
    ) {
        if (!hasNotificationPermission(context)) {
            return
        }

        init(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val iconRes = if (isCritical) {
            android.R.drawable.ic_dialog_alert
        } else {
            android.R.drawable.ic_dialog_info
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (_: SecurityException) {
            // Ignorado si no se concedió el permiso en runtime
        }
    }

    fun notifyTemperatureAlert(context: Context, tempActual: Float, tempMax: Float, cultivo: String) {
        val now = System.currentTimeMillis()
        if (now - lastTempNotifTime < COOLDOWN_MS) return
        lastTempNotifTime = now

        sendNotification(
            context = context,
            notificationId = NOTIF_ID_TEMP,
            title = "⚠️ Alerta de Temperatura: ${String.format("%.1f", tempActual)}°C",
            message = "La temperatura ha superado el límite seguro de $tempMax°C para el cultivo de '$cultivo'. Se activó la ventilación de emergencia.",
            isCritical = true
        )
    }

    fun notifyDroughtAlert(context: Context, adcRaw: Int, cultivo: String) {
        val now = System.currentTimeMillis()
        if (now - lastDroughtNotifTime < COOLDOWN_MS) return
        lastDroughtNotifTime = now

        sendNotification(
            context = context,
            notificationId = NOTIF_ID_DROUGHT,
            title = "💧 Alerta de Riego Requerido",
            message = "Sensor FC-28 detecta sustrato seco (ADC $adcRaw). Se recomienda activar el riego para el '$cultivo'.",
            isCritical = true
        )
    }

    fun notifyFailSafeAlert(context: Context, motivo: String) {
        val now = System.currentTimeMillis()
        if (now - lastFailSafeNotifTime < COOLDOWN_MS) return
        lastFailSafeNotifTime = now

        sendNotification(
            context = context,
            notificationId = NOTIF_ID_FAILSAFE,
            title = "🛡️ Seguridad IoT: Modo Seguro Activado",
            message = "El invernadero activó el protocolo automático por: $motivo. Compuerta posicionada a 45°.",
            isCritical = true
        )
    }

    fun notifyModeChanged(context: Context, nuevoModo: String, detalle: String) {
        sendNotification(
            context = context,
            notificationId = NOTIF_ID_MODE,
            title = "🔄 Conectividad: Modo $nuevoModo",
            message = detalle,
            isCritical = false
        )
    }

    fun sendTestNotification(context: Context) {
        sendNotification(
            context = context,
            notificationId = NOTIF_ID_TEST,
            title = "🔔 Notificación Push del Invernadero IoT",
            message = "¡Prueba exitosa! El servicio de notificaciones en tiempo real está funcionando correctamente.",
            isCritical = false
        )
    }
}
