package com.example.oficiolocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController // 1. IMPORTACIÓN AGREGADA
import com.example.oficiolocal.ui.navigation.AppNavGraph
import com.example.oficiolocal.ui.theme.OficioLocalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OficioLocalTheme {
                // 2. CREAMOS EL NAVCONTROLLER Y SE LO PASAMOS A APPAVGGRAPH
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}