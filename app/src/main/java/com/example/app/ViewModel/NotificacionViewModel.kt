package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.Notificacion
import com.example.app.Repository.NotificacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NotificacionViewModel @Inject constructor(
    private val repository: NotificacionRepository
) : ViewModel() {
    
    private val _notificaciones = MutableStateFlow<List<Notificacion>>(emptyList())
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()

    private val _notificacionSeleccionada = MutableStateFlow<Notificacion?>(null)
    val notificacionSeleccionada: StateFlow<Notificacion?> = _notificacionSeleccionada.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Contador de notificaciones no leídas
    private val _notificacionesNoLeidas = MutableStateFlow(0)
    val notificacionesNoLeidas: StateFlow<Int> = _notificacionesNoLeidas.asStateFlow()

    init {
        // No llamar obtenerTodos() aquí para evitar crashes al inicializar
        // Se llamará desde la UI cuando sea necesario
    }

    fun obtenerTodos() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val lista = withContext(Dispatchers.IO) {
                repository.obtenerTodos()
            }
            _notificaciones.value = lista
            // Actualizar contador de no leídas (asumiendo que hay un campo leida en el modelo)
            _notificacionesNoLeidas.value = lista.size // Por ahora, todas se consideran no leídas
        } catch (e: Exception) {
            _error.value = "Error al obtener notificaciones: ${e.message}"
            // Asegurar que la lista esté vacía en caso de error para evitar crashes
            _notificaciones.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun guardar(notificacion: Notificacion) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val notificacionGuardada = withContext(Dispatchers.IO) {
                repository.guardar(notificacion)
            }
            obtenerTodos()
        } catch (e: Exception) {
            _error.value = "Error al guardar notificación: ${e.message}"
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
            _error.value = "Error al eliminar notificación: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerPorId(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val notificacion = withContext(Dispatchers.IO) {
                repository.obtenerPorId(id)
            }
            _notificacionSeleccionada.value = notificacion
        } catch (e: Exception) {
            _error.value = "Error al obtener notificación: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}