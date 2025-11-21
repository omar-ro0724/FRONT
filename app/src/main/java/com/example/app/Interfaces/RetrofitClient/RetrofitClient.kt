package com.example.app.Interfaces.RetrofitClient

import com.example.app.Interfaces.AccesoPeatonalApiService
import com.example.app.Interfaces.AccesoVehicularApiService
import com.example.app.Interfaces.MascotaApiService
import com.example.app.Interfaces.NotificacionApiService
import com.example.app.Interfaces.PagoAdministracionApiService
import com.example.app.Interfaces.PaqueteriaApiService
import com.example.app.Interfaces.QuejaApiService
import com.example.app.Interfaces.ReservaZonaComunApiService
import com.example.app.Interfaces.UsuarioApiService
import com.example.app.Interfaces.VehiculoResidenteApiService
import com.example.app.Interfaces.VisitanteApiService
import com.example.app.Utils.NetworkConfigManager
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.util.Log

object RetrofitClient {
    
    // IPs conocidas del servidor (en orden de prioridad)
    private val KNOWN_SERVER_IPS = listOf(
        "10.51.201.247",  // IP principal conocida
        "192.168.20.1",   // Router común en red 192.168.20.x
        "192.168.1.1",    // Router común alternativo
        "10.0.2.2"        // Emulador Android
    )

    // Función para obtener la URL base dinámicamente
    private fun getBaseUrl(): String {
        // 1. Intentar usar IP guardada previamente (si existe) - MÁXIMA PRIORIDAD
        val savedIp = NetworkConfigManager.getSavedServerIp()
        if (savedIp != null && savedIp.isNotBlank()) {
            val port = NetworkConfigManager.getServerPort()
            Log.d("RetrofitClient", "✅ Usando IP guardada: $savedIp:$port")
            return "http://$savedIp:$port/"
        }
        
        // 2. Intentar con IPs conocidas del servidor
        val port = NetworkConfigManager.getServerPort()
        val deviceIp = NetworkConfigManager.getLocalIpAddress()
        
        Log.d("RetrofitClient", "📱 IP del dispositivo: $deviceIp")
        Log.d("RetrofitClient", "🔍 Probando IPs conocidas del servidor...")
        
        // Primero intentar con la IP principal conocida
        val primaryIp = KNOWN_SERVER_IPS[0]
        Log.d("RetrofitClient", "🌐 Intentando con IP principal: $primaryIp:$port")
        return "http://$primaryIp:$port/"
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS) // 5 segundos para búsqueda rápida
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false) // No reintentar automáticamente, manejaremos los reintentos manualmente
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    // Retrofit mutable para poder actualizarlo dinámicamente
    @Volatile
    private var retrofitInstance: Retrofit? = null
    
    private fun getRetrofit(): Retrofit {
        if (retrofitInstance == null) {
            synchronized(this) {
                if (retrofitInstance == null) {
                    val baseUrl = getBaseUrl()
                    Log.d("RetrofitClient", "🔧 Inicializando Retrofit con URL: $baseUrl")
                    retrofitInstance = Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
        }
        return retrofitInstance!!
    }
    
    // Función para actualizar la URL base y recrear Retrofit
    fun updateBaseUrl(newIp: String) {
        synchronized(this) {
            NetworkConfigManager.saveServerIp(newIp)
            Log.d("RetrofitClient", "🔄 Actualizando IP del servidor a: $newIp")
            // Recrear Retrofit con la nueva IP
            val port = NetworkConfigManager.getServerPort()
            val newBaseUrl = "http://$newIp:$port/"
            retrofitInstance = Retrofit.Builder()
                .baseUrl(newBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            Log.d("RetrofitClient", "✅ Retrofit actualizado con nueva URL: $newBaseUrl")
        }
    }
    
    // Función para obtener la IP actual que está usando Retrofit
    fun getCurrentServerIp(): String {
        val savedIp = NetworkConfigManager.getSavedServerIp()
        return savedIp ?: KNOWN_SERVER_IPS[0]
    }

    // Servicios de API - se recrean cada vez para usar la instancia actual de Retrofit
    val usuarioApiService: UsuarioApiService
        get() = getRetrofit().create(UsuarioApiService::class.java)

    // Servicios de acceso
    val accesoPeatonalApiService: AccesoPeatonalApiService
        get() = getRetrofit().create(AccesoPeatonalApiService::class.java)

    val accesoVehicularApiService: AccesoVehicularApiService
        get() = getRetrofit().create(AccesoVehicularApiService::class.java)

    val visitanteApiService: VisitanteApiService
        get() = getRetrofit().create(VisitanteApiService::class.java)

    val vehiculoResidenteApiService: VehiculoResidenteApiService
        get() = getRetrofit().create(VehiculoResidenteApiService::class.java)

    val mascotaApiService: MascotaApiService
        get() = getRetrofit().create(MascotaApiService::class.java)

    // Servicios de gestión y notificaciones
    val notificacionApiService: NotificacionApiService
        get() = getRetrofit().create(NotificacionApiService::class.java)

    val pagoAdministracionApiService: PagoAdministracionApiService
        get() = getRetrofit().create(PagoAdministracionApiService::class.java)

    val paqueteriaApiService: PaqueteriaApiService
        get() = getRetrofit().create(PaqueteriaApiService::class.java)

    val quejaApiService: QuejaApiService
        get() = getRetrofit().create(QuejaApiService::class.java)

    val reservaZonaComunApiService: ReservaZonaComunApiService
        get() = getRetrofit().create(ReservaZonaComunApiService::class.java)

}
