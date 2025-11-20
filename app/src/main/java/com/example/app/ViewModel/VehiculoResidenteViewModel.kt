package com.example.app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.Model.VehiculoResidente
import com.example.app.Repository.VehiculoResidenteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VehiculoResidenteViewModel @Inject constructor(
    private val repository: VehiculoResidenteRepository
) : ViewModel() {

    private val _vehiculos = MutableStateFlow<List<VehiculoResidente>>(emptyList())
    val vehiculos: StateFlow<List<VehiculoResidente>> = _vehiculos.asStateFlow()

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
            _vehiculos.value = lista
        } catch (e: Exception) {
            _error.value = "Error al obtener vehículos: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun guardar(vehiculo: VehiculoResidente) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val vehiculoGuardado = withContext(Dispatchers.IO) {
                repository.guardar(vehiculo)
            }
            obtenerTodos()
        } catch (e: Exception) {
            _error.value = "Error al guardar vehículo: ${e.message}"
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
            _error.value = "Error al eliminar vehículo: ${e.message}"
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
            _error.value = "Error al obtener vehículo: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}