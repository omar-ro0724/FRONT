package com.example.app.Interfaces

import com.example.app.Model.Queja
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface QuejaApiService {
    @GET("/api/quejas")
    suspend fun obtenerQuejas(): List<Queja>

    @GET("/api/quejas/{id}")
    suspend fun obtenerQueja(@Path("id") id: Long): Queja

    @POST("/api/quejas")
    suspend fun guardarQueja(@Body queja: Queja): Queja

    @DELETE("/api/quejas/{id}")
    suspend fun eliminarQueja(@Path("id") id: Long)
}
