package com.example.app.Repository

import com.example.app.DTO.LoginRequest
import com.example.app.Interfaces.RetrofitClient.RetrofitClient
import com.example.app.Interfaces.UsuarioApiService
import com.example.app.Model.Usuario
import com.example.app.Utils.NetworkConfigManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UsuarioRepository @Inject constructor(
    // No inyectar directamente, usar RetrofitClient para obtener el servicio actualizado
) {
    
    // Obtener el servicio de API desde RetrofitClient (siempre actualizado)
    private val api: UsuarioApiService
        get() = RetrofitClient.usuarioApiService

    suspend fun obtenerTodos(): List<Usuario> {
        return api.obtenerUsuarios()
    }

    suspend fun obtenerPorId(id: Long): Usuario {
        return api.obtenerUsuario(id)
    }

    suspend fun guardar(usuario: Usuario): Usuario {
        return api.guardarUsuario(usuario)
    }

    suspend fun eliminar(id: Long) {
        api.eliminarUsuario(id)
    }

    suspend fun login(usuario: String, password: String): Usuario {
        println("Login attempt: $usuario") // Para debugging
        
        // Obtener la IP actual que está intentando usar
        var currentIp = RetrofitClient.getCurrentServerIp()
        val port = NetworkConfigManager.getServerPort()
        
        println("Intentando conectar a: http://$currentIp:$port")
        
        return withContext(Dispatchers.IO) {
            // Intentar primero con la IP actual
            var lastException: Exception? = null
            
            try {
                val response = api.login(LoginRequest(usuario, password))
                if (response.isSuccessful) {
                    val usuarioResponse = response.body() ?: throw Exception("Respuesta vacía del servidor")
                    println("✅ Login successful for user: ${usuarioResponse.usuario} with role: ${usuarioResponse.rol}")
                    // Guardar la IP que funcionó
                    NetworkConfigManager.saveServerIp(currentIp)
                    RetrofitClient.updateBaseUrl(currentIp)
                    return@withContext usuarioResponse
                } else {
                    when (response.code()) {
                        401 -> throw Exception("Usuario o contraseña incorrectos")
                        404 -> throw Exception("Servicio no encontrado. Verifica la configuración del servidor.")
                        500 -> throw Exception("Error del servidor. Intenta más tarde.")
                        else -> throw Exception("Error de login: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: java.net.ConnectException) {
                lastException = e
                println("⚠️ Falló conexión a $currentIp:$port, buscando servidor en red local...")
            } catch (e: java.net.SocketTimeoutException) {
                lastException = e
                println("⚠️ Timeout con $currentIp:$port, buscando servidor en red local...")
            } catch (e: java.net.UnknownHostException) {
                lastException = e
                println("⚠️ Host desconocido $currentIp, buscando servidor en red local...")
            } catch (e: retrofit2.HttpException) {
                // Errores HTTP no son de conexión, lanzarlos directamente
                when (e.code()) {
                    401 -> throw Exception("Usuario o contraseña incorrectos")
                    404 -> throw Exception("Servicio no encontrado. Verifica que el endpoint /api/usuarios/login exista")
                    500 -> throw Exception("Error del servidor. Revisa los logs del backend")
                    else -> throw Exception("Error HTTP ${e.code()}: ${e.message()}")
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Error desconocido"
                // Si es un error de conexión, intentar buscar en red local
                if (errorMsg.contains("Failed to connect", ignoreCase = true) || 
                    errorMsg.contains("Unable to resolve host", ignoreCase = true) ||
                    errorMsg.contains("Connection refused", ignoreCase = true) ||
                    errorMsg.contains("Network is unreachable", ignoreCase = true) ||
                    errorMsg.contains("SocketTimeoutException", ignoreCase = true)) {
                    lastException = e
                    println("⚠️ Error de conexión con $currentIp:$port, buscando servidor en red local...")
                } else {
                    throw e
                }
            }
            
            // Si llegamos aquí, la IP conocida falló. Buscar en la red local del dispositivo
            if (lastException != null) {
                println("🔍 Buscando servidor automáticamente en la red local (probando TODAS las IPs)...")
                val deviceIp = NetworkConfigManager.getLocalIpAddress()
                val deviceIpForError = deviceIp ?: "desconocida"
                
                // Usar la función completa que prueba TODAS las IPs de la red (1-254)
                val foundIp = NetworkConfigManager.findWorkingServerIp()
                
                if (foundIp != null) {
                    println("✅✅✅ SERVIDOR ENCONTRADO EN: $foundIp:$port")
                    RetrofitClient.updateBaseUrl(foundIp)
                    
                    // Intentar login con la IP encontrada
                    try {
                        val testApi = RetrofitClient.usuarioApiService
                        val response = testApi.login(LoginRequest(usuario, password))
                        if (response.isSuccessful) {
                            val usuarioResponse = response.body() ?: throw Exception("Respuesta vacía del servidor")
                            println("✅ Login exitoso con IP encontrada: $foundIp")
                            return@withContext usuarioResponse
                        } else {
                            when (response.code()) {
                                401 -> throw Exception("Usuario o contraseña incorrectos")
                                404 -> throw Exception("Servicio no encontrado. Verifica la configuración del servidor.")
                                500 -> throw Exception("Error del servidor. Intenta más tarde.")
                                else -> throw Exception("Error de login: ${response.code()} - ${response.message()}")
                            }
                        }
                    } catch (e: Exception) {
                        // Si el servidor responde pero el login falla, lanzar el error específico
                        if (e.message?.contains("Usuario o contraseña") == true || 
                            e.message?.contains("Error de login") == true) {
                            throw e
                        }
                        // Si hay otro error, continuar y mostrar mensaje genérico
                    }
                }
                
                // Si no se encontró ninguna IP, lanzar el error original con información útil
                throw Exception("❌ No se pudo conectar al servidor\n\nIPs probadas:\n• $currentIp:$port (falló)\n• Red local completa $deviceIpForError (254 IPs probadas, no encontrado)\n\nSOLUCIÓN:\n1. ✅ Verifica que el servidor Spring Boot esté corriendo\n2. ✅ Confirma que estás en la misma red WiFi que el servidor\n3. ✅ En el servidor, ejecuta: ipconfig (Windows) o ifconfig (Linux)\n4. ✅ Verifica que el firewall permita conexiones en el puerto 8080\n5. ✅ El servidor debe estar en la misma red que el dispositivo ($deviceIpForError)\n6. ✅ Verifica que application.properties tenga: server.address=0.0.0.0")
            }
            
            throw lastException ?: Exception("Error desconocido")
        }
    }
}
