package com.example.oficiolocal

import android.app.Application
import com.example.oficiolocal.di.ServiceLocator

/** Punto de arranque: aquí se crea la base de datos y los repositorios (application -> ... -> repository). */
class OficioLocalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}