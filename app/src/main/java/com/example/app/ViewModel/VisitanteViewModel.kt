package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.Visitante
import com.example.app.Repository.VisitanteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VisitanteViewModel @Inject constructor(
    private val repository: VisitanteRepository
) : ViewModel() {

    private val _visitantes = MutableStateFlow<List<Visitante>>(emptyList())
    val visitantes: StateFlow<List<Visitante>> = _visitantes.asStateFlow()

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
            _visitantes.value = lista
        } catch (e: Exception) {
            _error.value = "Error al obtener visitantes: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun guardar(visitante: Visitante) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val visitanteGuardado = withContext(Dispatchers.IO) {
                repository.guardar(visitante)
            }
            obtenerTodos()
        } catch (e: Exception) {
            _error.value = "Error al guardar visitante: ${e.message}"
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
            _error.value = "Error al eliminar visitante: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun obtenerPorId(id: Long) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            withContext(Dispatchers.IO) {
                repository.obtenerPorId(id)
            }
        } catch (e: Exception) {
            _error.value = "Error al obtener visitante: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}