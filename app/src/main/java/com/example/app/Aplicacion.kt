package com.example.app

import android.app.Application
import com.example.app.Utils.NetworkConfigManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Aplicacion : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar NetworkConfigManager para detectar IP del dispositivo
        NetworkConfigManager.initialize(this)
    }
}