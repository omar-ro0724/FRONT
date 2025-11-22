package com.example.app.Repository

import com.example.app.Interfaces.PaqueteriaApiService
import com.example.app.Model.Paqueteria
import javax.inject.Inject

class PaqueteriaRepository @Inject constructor(
    private val api: PaqueteriaApiService
) {

    suspend fun obtenerTodos(): List<Paqueteria> {
        return api.obtenerPaqueteria()
    }

    suspend fun obtenerPorId(id: Long): Paqueteria {
        return api.obtenerPaquete(id)
    }

    suspend fun guardar(paqueteria: Paqueteria): Paqueteria {
        return api.guardarPaquete(paqueteria)
    }

    suspend fun actualizar(id: Long, paqueteria: Paqueteria): Paqueteria {
        return api.actualizarPaquete(id, paqueteria)
    }

    suspend fun eliminar(id: Long) {
        api.eliminarPaquete(id)
    }
}
