package com.example.oficiolocal

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.ui.navigation.AppNavGraph
import com.example.oficiolocal.ui.theme.OficioLocalTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= 33) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val darkMode by ServiceLocator.settingsRepository.darkMode.collectAsState()
            val windowSize = calculateWindowSizeClass(this)
            OficioLocalTheme(darkTheme = darkMode) {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    widthSizeClass = windowSize.widthSizeClass,
                )
            }
        }
    }
}
