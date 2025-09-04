package com.example.app.Interfaces.RetrofitClient

import android.os.Build
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
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val DEFAULT_PORT = 8080
    
    // Lista de IPs comunes para probar (en orden de prioridad)
    private val COMMON_SERVER_IPS = listOf(
        "192.168.100.9",    // Red WiFi original
        "10.51.202.74",     // Red alternativa
        "192.168.1.100",    // Red común 192.168.1.x
        "192.168.0.100",    // Red común 192.168.0.x
        "10.0.0.100"        // Red 10.0.0.x
    )
    
    // Detectar si está en emulador
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
    
    // Obtener la IP local del dispositivo
    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        val ip = address.hostAddress ?: continue
                        // Filtrar direcciones APIPA (169.254.x.x)
                        if (!ip.startsWith("169.254.")) {
                            return ip
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("RetrofitClient", "Error obteniendo IP local", e)
        }
        return null
    }
    
    // Calcular posibles IPs del servidor basándose en la red del dispositivo
    private fun calculateServerIpsFromDeviceNetwork(): List<String> {
        val localIp = getLocalIpAddress()
        if (localIp == null) {
            return COMMON_SERVER_IPS
        }
        
        val parts = localIp.split(".")
        if (parts.size != 4) {
            return COMMON_SERVER_IPS
        }
        
        val networkBase = "${parts[0]}.${parts[1]}.${parts[2]}"
        val calculatedIps = mutableListOf<String>()
        
        // Probar IPs comunes en la misma red
        calculatedIps.add("$networkBase.9")   // IP común del servidor
        calculatedIps.add("$networkBase.100") // IP común alternativa
        calculatedIps.add("$networkBase.1")    // Gateway común
        calculatedIps.add("$networkBase.10")    // Otra IP común
        
        // Agregar las IPs comunes originales al final
        calculatedIps.addAll(COMMON_SERVER_IPS)
        
        return calculatedIps.distinct()
    }
    
    // Obtener la URL base del servidor
    private fun getBaseUrl(): String {
        val isEmu = isEmulator()
        
        // Para emulador siempre usar 10.0.2.2
        if (isEmu) {
            val url = "http://10.0.2.2:$DEFAULT_PORT/"
            android.util.Log.d("RetrofitClient", "Emulador detectado, URL: $url")
            return url
        }
        
        // Para dispositivo físico: usar IPs calculadas o comunes
        val possibleIps = calculateServerIpsFromDeviceNetwork()
        val selectedIp = possibleIps.firstOrNull() ?: COMMON_SERVER_IPS.first()
        
        val url = "http://$selectedIp:$DEFAULT_PORT/"
        android.util.Log.d("RetrofitClient", "Dispositivo físico, IP local: ${getLocalIpAddress()}, URL: $url")
        android.util.Log.d("RetrofitClient", "IPs a probar: ${possibleIps.joinToString(", ")}")
        
        return url
    }
    
    // URL base - se inicializa cuando se necesita
    @Volatile
    private var retrofitInstance: Retrofit? = null
    
    private fun getRetrofit(): Retrofit {
        // Verificar si necesitamos recrear Retrofit (primera vez o después de cambiar IP)
        val currentInstance = retrofitInstance
        if (currentInstance == null) {
            synchronized(this) {
                // Doble verificación dentro del bloque sincronizado
                if (retrofitInstance == null) {
                    val baseUrl = getBaseUrl()
                    retrofitInstance = Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    android.util.Log.d("RetrofitClient", "Retrofit creado con URL: $baseUrl")
                }
            }
        }
        return retrofitInstance!!
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    // Servicios API - compatibles con código existente
    val usuarioApiService: UsuarioApiService by lazy {
        getRetrofit().create(UsuarioApiService::class.java)
    }

    // Servicios de acceso
    val accesoPeatonalApiService: AccesoPeatonalApiService by lazy {
        getRetrofit().create(AccesoPeatonalApiService::class.java)
    }
    
    val accesoVehicularApiService: AccesoVehicularApiService by lazy {
        getRetrofit().create(AccesoVehicularApiService::class.java)
    }

    val visitanteApiService: VisitanteApiService by lazy {
        getRetrofit().create(VisitanteApiService::class.java)
    }
    
    val vehiculoResidenteApiService: VehiculoResidenteApiService by lazy {
        getRetrofit().create(VehiculoResidenteApiService::class.java)
    }
    
    val mascotaApiService: MascotaApiService by lazy {
        getRetrofit().create(MascotaApiService::class.java)
    }

    // Servicios de gestión y notificaciones
    val notificacionApiService: NotificacionApiService by lazy {
        getRetrofit().create(NotificacionApiService::class.java)
    }
    
    val pagoAdministracionApiService: PagoAdministracionApiService by lazy {
        getRetrofit().create(PagoAdministracionApiService::class.java)
    }
    
    val paqueteriaApiService: PaqueteriaApiService by lazy {
        getRetrofit().create(PaqueteriaApiService::class.java)
    }
    
    val quejaApiService: QuejaApiService by lazy {
        getRetrofit().create(QuejaApiService::class.java)
    }
    
    val reservaZonaComunApiService: ReservaZonaComunApiService by lazy {
        getRetrofit().create(ReservaZonaComunApiService::class.java)
    }
}
