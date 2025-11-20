package com.example.app.Interfaces.RetrofitClient

import com.example.app.Interfaces.AccesoPeatonalApiService
import com.example.app.Interfaces.AccesoVehicularApiService
import com.example.app.Interfaces.MascotaApiService
import com.example.app.Interfaces.NotificacionApiService
import com.example.app.Interfaces.PagoAdministracionApiService
import com.example.app.Interfaces.PaqueteriaApiService
import com.example.app.Interfaces.QuejaApiService
import com.example.app.Interfaces.ReservaZonaComunApiService
import com.example.app.Interfaces.UsuarioApiService
import com.example.app.Interfaces.VehiculoResidenteApiService
import com.example.app.Interfaces.VisitanteApiService
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.51.201.247:8080/" // IP del servidor

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val usuarioApiService: UsuarioApiService by lazy {
        retrofit.create(UsuarioApiService::class.java)
    }

    // Servicios de acceso
    val accesoPeatonalApiService: AccesoPeatonalApiService by lazy {
        retrofit.create(AccesoPeatonalApiService::class.java)
    }

    val accesoVehicularApiService: AccesoVehicularApiService by lazy {
        retrofit.create(AccesoVehicularApiService::class.java)
    }

    val visitanteApiService: VisitanteApiService by lazy {
        retrofit.create(VisitanteApiService::class.java)
    }

    val vehiculoResidenteApiService: VehiculoResidenteApiService by lazy {
        retrofit.create(VehiculoResidenteApiService::class.java)
    }

    val mascotaApiService: MascotaApiService by lazy {
        retrofit.create(MascotaApiService::class.java)
    }

    // Servicios de gestión y notificaciones
    val notificacionApiService: NotificacionApiService by lazy {
        retrofit.create(NotificacionApiService::class.java)
    }

    val pagoAdministracionApiService: PagoAdministracionApiService by lazy {
        retrofit.create(PagoAdministracionApiService::class.java)
    }

    val paqueteriaApiService: PaqueteriaApiService by lazy {
        retrofit.create(PaqueteriaApiService::class.java)
    }

    val quejaApiService: QuejaApiService by lazy {
        retrofit.create(QuejaApiService::class.java)
    }

    val reservaZonaComunApiService: ReservaZonaComunApiService by lazy {
        retrofit.create(ReservaZonaComunApiService::class.java)
    }

}
