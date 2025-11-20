package com.example.app.Repository

import com.example.app.Interfaces.ReservaZonaComunApiService
import com.example.app.Model.ReservaZonaComun
import javax.inject.Inject

class ReservaZonaComunRepository @Inject constructor(
    private val api: ReservaZonaComunApiService
) {

    suspend fun obtenerTodos(): List<ReservaZonaComun> {
        return api.obtenerReservas()
    }

    suspend fun obtenerPorId(id: Long): ReservaZonaComun {
        return api.obtenerReserva(id)
    }

    suspend fun guardar(reserva: ReservaZonaComun): ReservaZonaComun {
        return api.guardarReserva(reserva)
    }

    suspend fun eliminar(id: Long) {
        api.eliminarReserva(id)
    }
}
