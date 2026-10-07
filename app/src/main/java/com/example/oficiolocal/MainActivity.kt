package com.example.oficiolocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.ui.navigation.AppNavGraph
import com.example.oficiolocal.ui.theme.OficioLocalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkMode by ServiceLocator.settingsRepository.darkMode.collectAsState()
            OficioLocalTheme(darkTheme = darkMode) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}
