package com.example.appinvernaderoautonomo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.appinvernaderoautonomo.notifications.GreenhouseNotificationHelper
import com.example.appinvernaderoautonomo.ui.navegacion.NavegacionApp
import com.example.appinvernaderoautonomo.ui.theme.AppInvernaderoAutonomoTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Callback tras respuesta del usuario
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        GreenhouseNotificationHelper.init(this)
        solicitarPermisosRequeridos()

        setContent {
            AppInvernaderoAutonomoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavegacionApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun solicitarPermisosRequeridos() {
        val permisos = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permisos.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permisos.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (permisos.isNotEmpty()) {
            requestPermissionsLauncher.launch(permisos.toTypedArray())
        }
    }
}
