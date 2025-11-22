package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.Paqueteria
import com.example.app.Repository.NotificacionRepository
import com.example.app.Repository.PaqueteriaRepository
import com.example.app.Model.Notificacion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PaqueteriaViewModel @Inject constructor(
    private val repository: PaqueteriaRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {
    
    private val _paquetes = MutableStateFlow<List<Paqueteria>>(emptyList())
    val paquetes: StateFlow<List<Paqueteria>> = _paquetes.asStateFlow()

    private val _paqueteSeleccionado = MutableStateFlow<Paqueteria?>(null)
    val paqueteSeleccionado: StateFlow<Paqueteria?> = _paqueteSeleccionado.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun obtenerTodos() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerTodos()
            }
            _paquetes.value = lista
        } catch (e: Exception) {
            _error.value = "Error al obtener paquetes: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun guardar(paqueteria: Paqueteria, usuarioCompleto: com.example.app.Model.Usuario) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            android.util.Log.d("PaqueteriaViewModel", "Guardando paquete: transportadora=${paqueteria.transportadora}, usuarioId=${paqueteria.usuario?.id}")
            
            val paqueteGuardado = withContext(Dispatchers.IO) {
                repository.guardar(paqueteria)
            }
            
            android.util.Log.d("PaqueteriaViewModel", "Paquete guardado exitosamente: id=${paqueteGuardado.id}")
            
            // Actualizar lista de paquetes
            obtenerTodos()
            
            // Notificación automática al residente usando el usuario completo
            if (usuarioCompleto.id != null) {
                val mensaje = "Tienes un paquete disponible de ${paqueteria.transportadora}. " +
                        "Por favor pasa a recogerlo en la portería."
                
                android.util.Log.d("PaqueteriaViewModel", "=== CREANDO NOTIFICACIÓN ===")
                android.util.Log.d("PaqueteriaViewModel", "Usuario destino - ID: ${usuarioCompleto.id}, Nombre: ${usuarioCompleto.nombre}, Rol: ${usuarioCompleto.rol}, Documento: ${usuarioCompleto.documento}")
                android.util.Log.d("PaqueteriaViewModel", "Mensaje: $mensaje")
                android.util.Log.d("PaqueteriaViewModel", "Transportadora: ${paqueteria.transportadora}")
                
                // Crear notificación con el usuario completo - IMPORTANTE: usar el usuario completo con todos sus campos
                val notificacion = Notificacion(
                    mensaje = mensaje,
                    fechaEnvio = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME),
                    usuario = usuarioCompleto  // Usar el usuario completo que se seleccionó
                )
                
                android.util.Log.d("PaqueteriaViewModel", "Notificación creada - Mensaje: ${notificacion.mensaje}, UsuarioId: ${notificacion.usuario?.id}, UsuarioNombre: ${notificacion.usuario?.nombre}")
                
                try {
                    // Usar launch separado para que no se cancele con el scope principal
                    viewModelScope.launch {
                        try {
                            val notificacionGuardada = withContext(Dispatchers.IO) {
                                notificacionRepository.guardar(notificacion)
                            }
                            android.util.Log.d("PaqueteriaViewModel", "=== NOTIFICACIÓN GUARDADA EXITOSAMENTE ===")
                            android.util.Log.d("PaqueteriaViewModel", "Notificación ID: ${notificacionGuardada.id}")
                            android.util.Log.d("PaqueteriaViewModel", "Usuario asociado - ID: ${notificacionGuardada.usuario?.id}, Nombre: ${notificacionGuardada.usuario?.nombre}, Rol: ${notificacionGuardada.usuario?.rol}")
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            android.util.Log.w("PaqueteriaViewModel", "Notificación cancelada (pero probablemente se guardó en el backend): ${e.message}")
                            // Intentar verificar si la notificación se guardó consultando el servidor después
                            kotlinx.coroutines.delay(1000)
                            try {
                                val todasLasNotificaciones = withContext(Dispatchers.IO) {
                                    notificacionRepository.obtenerTodos()
                                }
                                val notificacionGuardada = todasLasNotificaciones.findLast { 
                                    it.mensaje == notificacion.mensaje && 
                                    it.usuario?.id == notificacion.usuario?.id 
                                }
                                if (notificacionGuardada != null) {
                                    android.util.Log.d("PaqueteriaViewModel", "Notificación verificada en el servidor - ID: ${notificacionGuardada.id}")
                                } else {
                                    android.util.Log.w("PaqueteriaViewModel", "No se pudo verificar si la notificación se guardó")
                                }
                            } catch (verifyError: Exception) {
                                android.util.Log.e("PaqueteriaViewModel", "Error al verificar notificación: ${verifyError.message}")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("PaqueteriaViewModel", "=== ERROR AL GUARDAR NOTIFICACIÓN ===")
                            android.util.Log.e("PaqueteriaViewModel", "Error: ${e.message}", e)
                            // No lanzar error aquí para no bloquear el guardado del paquete
                            _error.value = "Paquete guardado pero error al enviar notificación: ${e.message}"
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PaqueteriaViewModel", "Error al iniciar coroutine de notificación: ${e.message}")
                }
            } else {
                android.util.Log.e("PaqueteriaViewModel", "=== ERROR: NO SE PUEDE ENVIAR NOTIFICACIÓN ===")
                android.util.Log.e("PaqueteriaViewModel", "Usuario completo sin ID: $usuarioCompleto")
                android.util.Log.e("PaqueteriaViewModel", "Usuario ID: ${usuarioCompleto.id}, Nombre: ${usuarioCompleto.nombre}")
            }
        } catch (e: Exception) {
            android.util.Log.e("PaqueteriaViewModel", "Error al guardar paquete", e)
            _error.value = "Error al guardar paquete: ${e.message}"
            throw e
        } finally {
            _isLoading.value = false
        }
    }

    fun eliminar(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            withContext(Dispatchers.IO) {
                repository.eliminar(id)
            }
            obtenerTodos()
        } catch (e: Exception) {
            _error.value = "Error al eliminar paquete: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerPorId(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val paquete = withContext(Dispatchers.IO) {
                repository.obtenerPorId(id)
            }
            _paqueteSeleccionado.value = paquete
        } catch (e: Exception) {
            _error.value = "Error al obtener paquete: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun actualizarEstado(id: Long, nuevoEstado: String) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            android.util.Log.d("PaqueteriaViewModel", "Actualizando estado del paquete $id a $nuevoEstado")
            val paqueteActual = withContext(Dispatchers.IO) {
                repository.obtenerPorId(id)
            }
            
            val paqueteActualizado = paqueteActual.copy(estado = nuevoEstado)
            
            val paqueteGuardado = withContext(Dispatchers.IO) {
                repository.actualizar(id, paqueteActualizado)
            }
            
            android.util.Log.d("PaqueteriaViewModel", "Estado del paquete actualizado exitosamente")
            // Actualizar lista de paquetes
            obtenerTodos()
        } catch (e: Exception) {
            android.util.Log.e("PaqueteriaViewModel", "Error al actualizar estado del paquete", e)
            _error.value = "Error al actualizar estado del paquete: ${e.message}"
            throw e
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}