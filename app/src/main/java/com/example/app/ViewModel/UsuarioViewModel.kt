package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.Usuario
import com.example.app.Repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val repository: UsuarioRepository
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _rolSeleccionado = MutableStateFlow<String?>(null)
    val rolSeleccionado: StateFlow<String?> = _rolSeleccionado.asStateFlow()

    fun seleccionarRol(rol: String) {
        _rolSeleccionado.value = rol
        clearError()
    }

    fun setError(message: String) { _error.value = message }
    fun clearError() { _error.value = null }

    fun login(usuario: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                println("Iniciando login para usuario: $usuario")
                val usuarioAutenticado = repository.login(usuario, password)
                _usuarioActual.value = usuarioAutenticado
                println("Login exitoso, usuario autenticado: ${usuarioAutenticado.usuario}")
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
                _usuarioActual.value = null
                println("Error en login: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _usuarioActual.value = null
        _error.value = null
        _rolSeleccionado.value = null
    }

    fun obtenerTodos() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val lista = repository.obtenerTodos()
            _usuarios.value = lista
        } catch (e: Exception) {
            _error.value = "Error al obtener usuarios: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}

