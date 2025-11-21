package com.example.app.Model

import com.google.gson.annotations.SerializedName

data class Notificacion(
    val id: Long? = null,

    @SerializedName("mensaje")
    val mensaje: String? = null,

    @SerializedName("fechaEnvio")
    val fechaEnvio: String? = null,  // Formato ISO 8601

    @SerializedName("usuario")
    val usuario: Usuario? = null,

    @SerializedName("imagenUrl")
    val imagenUrl: String? = null  // Ruta de la imagen adjunta
) {
    // Función helper para obtener mensaje seguro (renombrada para evitar conflicto con getter automático)
    fun mensajeSeguro(): String = mensaje ?: ""
    
    // Función helper para verificar si la notificación es válida
    fun esValida(): Boolean = (id != null) || (mensaje?.isNotBlank() == true)
}