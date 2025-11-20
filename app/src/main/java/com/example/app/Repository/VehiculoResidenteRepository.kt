package com.example.app.Repository

import com.example.app.Interfaces.VehiculoResidenteApiService
import com.example.app.Model.VehiculoResidente
import javax.inject.Inject

class VehiculoResidenteRepository @Inject constructor(
    private val api: VehiculoResidenteApiService
) {

    suspend fun obtenerTodos(): List<VehiculoResidente> {
        return api.obtenerVehiculos()
    }

    suspend fun obtenerPorId(id: Long): VehiculoResidente {
        return api.obtenerVehiculo(id)
    }

    suspend fun guardar(vehiculoResidente: VehiculoResidente): VehiculoResidente {
        return api.guardarVehiculo(vehiculoResidente)
    }

    suspend fun eliminar(id: Long) {
        api.eliminarVehiculo(id)
    }
}
