package com.example.app.Interfaces

import com.example.app.Model.Notificacion
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificacionApiService {
    @GET("/api/notificaciones")
    suspend fun obtenerNotificaciones(): List<Notificacion>

    @GET("/api/notificaciones/{id}")
    suspend fun obtenerNotificacion(@Path("id") id: Long): Notificacion

    @POST("/api/notificaciones")
    suspend fun guardarNotificacion(@Body notificacion: Notificacion): Notificacion

    @PUT("/api/notificaciones/{id}")
    suspend fun actualizarNotificacion(@Path("id") id: Long, @Body notificacion: Notificacion): Notificacion

    @DELETE("/api/notificaciones/{id}")
    suspend fun eliminarNotificacion(@Path("id") id: Long)
}
