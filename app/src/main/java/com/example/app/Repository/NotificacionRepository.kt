package com.example.app.Repository

import com.example.app.Interfaces.NotificacionApiService
import com.example.app.Model.Notificacion
import javax.inject.Inject

class NotificacionRepository @Inject constructor(
    private val api: NotificacionApiService
) {

    suspend fun obtenerTodos(): List<Notificacion> {
        return api.obtenerNotificaciones()
    }

    suspend fun obtenerPorId(id: Long): Notificacion {
        return api.obtenerNotificacion(id)
    }

    suspend fun guardar(notificacion: Notificacion): Notificacion {
        return api.guardarNotificacion(notificacion)
    }

    suspend fun eliminar(id: Long) {
        api.eliminarNotificacion(id)
    }
}