package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class Notificacion(
    val id: Long? = null,

    @SerializedName("mensaje")
    val mensaje: String,

    @SerializedName("fechaEnvio")
    val fechaEnvio: String?,  // Formato ISO 8601

    @SerializedName("usuario")
    val usuario: Usuario? = null,

    @SerializedName("imagenUrl")
    val imagenUrl: String? = null  // Ruta de la imagen adjunta
)