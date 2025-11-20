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

    fun guardar(paqueteria: Paqueteria) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val paqueteGuardado = withContext(Dispatchers.IO) {
                repository.guardar(paqueteria)
            }
            obtenerTodos()
            
            // Notificación automática al residente según el PDF
            if (paqueteGuardado.usuario != null) {
                val notificacion = Notificacion(
                    mensaje = "Tienes un paquete disponible de ${paqueteria.transportadora}",
                    fechaEnvio = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME),
                    usuario = paqueteGuardado.usuario
                )
                withContext(Dispatchers.IO) {
                    notificacionRepository.guardar(notificacion)
                }
            }
        } catch (e: Exception) {
            _error.value = "Error al guardar paquete: ${e.message}"
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

    fun clearError() {
        _error.value = null
    }
}