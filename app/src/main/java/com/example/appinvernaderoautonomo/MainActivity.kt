package com.example.appinvernaderoautonomo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.appinvernaderoautonomo.ui.navegacion.NavegacionApp
import com.example.appinvernaderoautonomo.ui.theme.AppInvernaderoAutonomoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppInvernaderoAutonomoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavegacionApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
