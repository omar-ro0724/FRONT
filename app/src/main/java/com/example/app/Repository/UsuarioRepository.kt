package com.example.app.Repository

import com.example.app.DTO.LoginRequest
import com.example.app.Interfaces.RetrofitClient.RetrofitClient
import com.example.app.Interfaces.UsuarioApiService
import com.example.app.Model.Usuario
import javax.inject.Inject

class UsuarioRepository @Inject constructor(
    private val api: UsuarioApiService
) {

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
        try {
            val response = api.login(LoginRequest(usuario, password))
            if (response.isSuccessful) {
                val usuarioResponse = response.body() ?: throw Exception("Respuesta vacía del servidor")
                println("Login successful for user: ${usuarioResponse.usuario} with role: ${usuarioResponse.rol}")
                return usuarioResponse
            } else {
                val errorMsg = "Error de login: ${response.code()} - ${response.message()}"
                println("Login failed: $errorMsg")
                throw Exception(errorMsg)
            }
        } catch (e: java.net.ConnectException) {
            throw Exception("No se pudo conectar al servidor. Verifica que el servidor esté corriendo y en la misma red.")
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Tiempo de espera agotado. El servidor no responde. Verifica la conexión de red.")
        } catch (e: java.net.UnknownHostException) {
            throw Exception("No se pudo encontrar el servidor. Verifica la configuración de red.")
        } catch (e: Exception) {
            // Si es un error de conexión genérico, dar mensaje más amigable
            if (e.message?.contains("Failed to connect") == true || 
                e.message?.contains("Unable to resolve host") == true ||
                e.message?.contains("Connection refused") == true) {
                throw Exception("No se pudo conectar al servidor. La aplicación está buscando el servidor automáticamente...")
            }
            throw e
        }
    }
}
