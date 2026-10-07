package com.example.oficiolocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.oficiolocal.ui.navigation.AppNavGraph
import com.example.oficiolocal.ui.theme.OficioLocalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OficioLocalTheme { AppNavGraph() }
        }
    }
}