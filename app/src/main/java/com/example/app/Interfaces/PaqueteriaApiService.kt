package com.example.app.Interfaces

import com.example.app.Model.Paqueteria
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PaqueteriaApiService {
    @GET("/api/paqueteria")
    suspend fun obtenerPaqueteria(): List<Paqueteria>

    @GET("/api/paqueteria/{id}")
    suspend fun obtenerPaquete(@Path("id") id: Long): Paqueteria

    @POST("/api/paqueteria")
    suspend fun guardarPaquete(@Body paqueteria: Paqueteria): Paqueteria

    @PUT("/api/paqueteria/{id}")
    suspend fun actualizarPaquete(@Path("id") id: Long, @Body paqueteria: Paqueteria): Paqueteria

    @DELETE("/api/paqueteria/{id}")
    suspend fun eliminarPaquete(@Path("id") id: Long)
}
