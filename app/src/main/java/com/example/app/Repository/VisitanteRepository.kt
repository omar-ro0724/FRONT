package com.example.app.Repository

import com.example.app.Interfaces.VisitanteApiService
import com.example.app.Model.Visitante
import javax.inject.Inject

class VisitanteRepository @Inject constructor(
    private val api: VisitanteApiService
) {

    suspend fun obtenerTodos(): List<Visitante> {
        return api.obtenerVisitantes()
    }

    suspend fun obtenerPorId(id: Long): Visitante {
        return api.obtenerVisitante(id)
    }

    suspend fun guardar(visitante: Visitante): Visitante {
        return api.guardarVisitante(visitante)
    }

    suspend fun eliminar(id: Long) {
        api.eliminarVisitante(id)
    }
}