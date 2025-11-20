package com.example.app.Utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async

object NetworkConfigManager {
    private const val PREFS_NAME = "network_config"
    private const val KEY_SERVER_IP = "server_ip"
    private const val KEY_SERVER_PORT = "server_port"
    private const val DEFAULT_PORT = 8080
    
    private var sharedPreferences: SharedPreferences? = null
    
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Obtener la IP local del dispositivo
    fun getLocalIpAddress(): String? {
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
            Log.e("NetworkConfigManager", "Error obteniendo IP local", e)
        }
        return null
    }
    
    // Generar lista inteligente de IPs posibles basándose en la red actual
    fun generatePossibleServerIps(): List<String> {
        val possibleIps = mutableListOf<String>()
        
        // 1. Agregar IP guardada previamente (si existe) al principio (MÁXIMA PRIORIDAD)
        val savedIp = getSavedServerIp()
        if (savedIp != null) {
            possibleIps.add(savedIp)
        }
        
        // 2. IPs basadas en la red local del dispositivo (ALTA PRIORIDAD)
        val localIp = getLocalIpAddress()
        if (localIp != null) {
            val parts = localIp.split(".")
            if (parts.size == 4) {
                val networkBase = "${parts[0]}.${parts[1]}.${parts[2]}"
                
                // Probar primero las IPs más comunes en la misma subred (orden de probabilidad)
                // Incluir la IP del dispositivo mismo y IPs comunes
                val deviceHost = parts[3].toIntOrNull() ?: 0
                val highPriorityHosts = mutableListOf<Int>()
                
                // Agregar IPs comunes primero
                highPriorityHosts.addAll(listOf(9, 1, 100, 10, 2, 254, 101, 11, 20, 50, 5, 15, 25, 30, 40, 60, 80, 90, 110, 120, 150, 200))
                
                // Agregar IPs cercanas a la IP del dispositivo (probablemente el servidor esté cerca)
                if (deviceHost > 0) {
                    for (offset in 1..10) {
                        if (deviceHost + offset <= 254) highPriorityHosts.add(deviceHost + offset)
                        if (deviceHost - offset >= 1) highPriorityHosts.add(deviceHost - offset)
                    }
                }
                
                // Agregar IPs comunes adicionales (incluyendo 14 que es la IP del servidor)
                highPriorityHosts.addAll(listOf(14, 13, 12, 16, 17, 18, 19, 21, 22, 23, 24))
                
                for (host in highPriorityHosts.distinct()) {
                    val ip = "$networkBase.$host"
                    if (!possibleIps.contains(ip)) {
                        possibleIps.add(ip)
                    }
                }
                
                // Luego agregar el resto de IPs de la subred (1-254) en orden
                for (i in 1..254) {
                    if (!highPriorityHosts.contains(i)) {
                        val ip = "$networkBase.$i"
                        if (!possibleIps.contains(ip)) {
                            possibleIps.add(ip)
                        }
                    }
                }
            }
        }
        
        // 3. IPs comunes predefinidas (como respaldo)
        val commonServerIps = listOf(
            "192.168.100.9",    // Red WiFi conocida (alta prioridad)
            "192.168.100.1",    // Gateway común
            "192.168.100.100",  // IP común
            "10.120.137.9",     // IP del servidor conocida
            "192.168.1.9",      // Red común alternativa
            "192.168.1.1",      // Gateway común
            "192.168.1.100",    // IP común
            "192.168.0.1",      // Gateway común
            "192.168.0.100",    // IP común
            "10.0.0.1",         // Gateway común
            "10.0.0.100"        // IP común
        )
        // Agregar solo si no están ya en la lista
        for (ip in commonServerIps) {
            if (!possibleIps.contains(ip)) {
                possibleIps.add(ip) // Agregar al final como respaldo
            }
        }
        
        return possibleIps.distinct()
    }
    
    // Probar conexión a una IP específica
    suspend fun testConnection(ip: String, port: Int = DEFAULT_PORT, timeoutSeconds: Int = 3): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
                    .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
                    .build()
                
                // Probar primero con el endpoint más simple y rápido
                val endpoints = listOf(
                    "http://$ip:$port/actuator/health",  // Endpoint de Spring Boot Actuator (más rápido)
                    "http://$ip:$port/api/usuarios",     // Endpoint de la API
                    "http://$ip:$port/"                  // Raíz del servidor
                )
                
                for (endpoint in endpoints) {
                    try {
                        val request = Request.Builder()
                            .url(endpoint)
                            .get() // Usar GET para mejor compatibilidad
                            .build()
                        
                        val response = client.newCall(request).execute()
                        // Cualquier respuesta HTTP (incluso 404, 401, 403) significa que el servidor está activo
                        val isSuccessful = response.code in 200..599
                        response.close()
                        
                        if (isSuccessful) {
                            Log.d("NetworkConfigManager", "✅✅✅ Servidor encontrado en $ip:$port (endpoint: $endpoint, código: ${response.code}) ✅✅✅")
                            return@withContext true
                        }
                    } catch (e: Exception) {
                        // Continuar con el siguiente endpoint
                        continue
                    }
                }
                
                false
            } catch (e: java.net.ConnectException) {
                // Connection refused - servidor no está escuchando en esa IP o puerto
                false
            } catch (e: java.net.SocketTimeoutException) {
                // Timeout - servidor no responde o está bloqueado por firewall
                false
            } catch (e: java.net.UnknownHostException) {
                // Host desconocido
                false
            } catch (e: Exception) {
                false
            }
        }
    }
    
    @Volatile
    private var isSearching = false
    
    // Encontrar la IP del servidor probando múltiples IPs
    suspend fun findWorkingServerIp(): String? {
        // Evitar búsquedas duplicadas
        if (isSearching) {
            Log.d("NetworkConfigManager", "Búsqueda ya en progreso, esperando...")
            return null
        }
        
        isSearching = true
        try {
            val possibleIps = generatePossibleServerIps()
            val localIp = getLocalIpAddress()
            
            Log.d("NetworkConfigManager", "═══════════════════════════════════════")
            Log.d("NetworkConfigManager", "🔍 INICIANDO BÚSQUEDA DE SERVIDOR")
            Log.d("NetworkConfigManager", "IP local del dispositivo: $localIp")
            Log.d("NetworkConfigManager", "IPs a probar: ${possibleIps.size}")
            Log.d("NetworkConfigManager", "═══════════════════════════════════════")
            
            // Probar IPs en lotes (limitado a 50 simultáneas para búsqueda más rápida)
            val batchSize = 50
            var testedCount = 0
            // Probar TODAS las IPs posibles (sin límite)
            val ipsToTest = possibleIps
            
            for (i in ipsToTest.indices step batchSize) {
                val batch = ipsToTest.subList(i, minOf(i + batchSize, ipsToTest.size))
                
                // Probar en paralelo usando coroutines
                val results = withContext(Dispatchers.IO) {
                    batch.map { ip ->
                        async {
                            testedCount++
                            if (testedCount <= 20 || testedCount % 30 == 0) {
                                Log.d("NetworkConfigManager", "[$testedCount/${ipsToTest.size}] Probando $ip:8080...")
                            }
                            ip to testConnection(ip, timeoutSeconds = 3) // Timeout de 3 segundos
                        }
                    }.map { it.await() }
                }
                
                // Encontrar la primera IP que funciona
                val workingIp = results.find { it.second }?.first
                if (workingIp != null) {
                    Log.d("NetworkConfigManager", "═══════════════════════════════════════")
                    Log.d("NetworkConfigManager", "✅✅✅ SERVIDOR ENCONTRADO ✅✅✅")
                    Log.d("NetworkConfigManager", "IP: $workingIp:8080")
                    Log.d("NetworkConfigManager", "═══════════════════════════════════════")
                    saveServerIp(workingIp)
                    return workingIp
                }
                
                // Log cada 30 IPs probadas
                if (testedCount % 30 == 0 && testedCount < ipsToTest.size) {
                    Log.d("NetworkConfigManager", "Progreso: $testedCount/${ipsToTest.size} IPs probadas...")
                }
            }
            
            Log.w("NetworkConfigManager", "═══════════════════════════════════════")
            Log.w("NetworkConfigManager", "⚠️ SERVIDOR NO ENCONTRADO")
            Log.w("NetworkConfigManager", "Probadas ${possibleIps.size} IPs en la red $localIp")
            Log.w("NetworkConfigManager", "═══════════════════════════════════════")
            Log.w("NetworkConfigManager", "SOLUCIÓN: Configura el servidor Spring Boot para escuchar en 0.0.0.0")
            Log.w("NetworkConfigManager", "Agrega en application.properties:")
            Log.w("NetworkConfigManager", "server.address=0.0.0.0")
            Log.w("NetworkConfigManager", "═══════════════════════════════════════")
            
            return null
        } finally {
            isSearching = false
        }
    }
    
    // Guardar IP del servidor que funcionó
    fun saveServerIp(ip: String) {
        sharedPreferences?.edit()?.putString(KEY_SERVER_IP, ip)?.apply()
        Log.d("NetworkConfigManager", "IP guardada: $ip")
    }
    
    // Obtener IP guardada
    fun getSavedServerIp(): String? {
        return sharedPreferences?.getString(KEY_SERVER_IP, null)
    }
    
    // Obtener puerto guardado o usar el por defecto
    fun getServerPort(): Int {
        return sharedPreferences?.getInt(KEY_SERVER_PORT, DEFAULT_PORT) ?: DEFAULT_PORT
    }
    
    // Guardar puerto
    fun saveServerPort(port: Int) {
        sharedPreferences?.edit()?.putInt(KEY_SERVER_PORT, port)?.apply()
    }
    
    // Obtener URL base completa
    fun getBaseUrl(): String {
        val ip = getSavedServerIp() ?: "localhost"
        val port = getServerPort()
        return "http://$ip:$port/"
    }
    
    // Resetear configuración (forzar nueva búsqueda)
    fun resetConfiguration() {
        sharedPreferences?.edit()?.remove(KEY_SERVER_IP)?.apply()
        Log.d("NetworkConfigManager", "Configuración de red reseteada")
    }
    
    // Verificar si hay una IP guardada
    fun hasSavedIp(): Boolean {
        return getSavedServerIp() != null
    }
    
    // Configurar IP manualmente (para cuando la búsqueda automática falla)
    fun setServerIpManually(ip: String) {
        saveServerIp(ip)
        Log.d("NetworkConfigManager", "IP configurada manualmente: $ip")
    }
    
    // Probar si una IP específica funciona
    suspend fun testSpecificIp(ip: String, port: Int = DEFAULT_PORT): Boolean {
        return testConnection(ip, port, timeoutSeconds = 3)
    }
}

