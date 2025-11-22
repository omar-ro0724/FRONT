package com.example.app

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
// ⛔️ NO usar androidx.navigation.Navigation en Compose
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.example.app.Pantallas.PantallaLogin
import com.example.app.Pantallas.PantallaSeleccionRol
import com.example.app.Pantallas.RolAdministrador.PantallaAccesoPeatonalDetalle
import com.example.app.Pantallas.RolAdministrador.PantallaAccesoVehicularDetalle
import com.example.app.Pantallas.RolAdministrador.PantallaAccesos
import com.example.app.Pantallas.RolAdministrador.PantallaCreacionPublicacionAdmin
import com.example.app.Pantallas.RolAdministrador.PantallaCrearUsuario
import com.example.app.Pantallas.RolAdministrador.PantallaDashboardAdmin
import com.example.app.Pantallas.RolAdministrador.PantallaDetalleQuejas
import com.example.app.Pantallas.RolAdministrador.PantallaDetalleReservaPiscina
import com.example.app.Pantallas.RolAdministrador.PantallaDetalleReservaSalonComunal
import com.example.app.Pantallas.RolAdministrador.PantallaDetalleReservaZonaBBQ
import com.example.app.Pantallas.RolAdministrador.PantallaInicioAdmin
import com.example.app.Pantallas.RolAdministrador.PantallaMascotas
import com.example.app.Pantallas.RolAdministrador.PantallaMensajes
import com.example.app.Pantallas.RolAdministrador.PantallaMenu
import com.example.app.Pantallas.RolAdministrador.PantallaNotificaciones
import com.example.app.Pantallas.RolAdministrador.PantallaPerfil
import com.example.app.Pantallas.RolAdministrador.PantallaQuejas
import com.example.app.Pantallas.RolAdministrador.PantallaReservas

import com.example.app.Pantallas.RolCelador.PantallaAccesoPeatonalCelador
import com.example.app.Pantallas.RolCelador.PantallaAccesoVehicularCelador
import com.example.app.Pantallas.RolCelador.PantallaAccesosCelador
import com.example.app.Pantallas.RolCelador.PantallaDashboardCelador
import com.example.app.Pantallas.RolCelador.PantallaDetalleQuejasCelador
import com.example.app.Pantallas.RolCelador.PantallaDetallesPaqueteriaCelador
import com.example.app.Pantallas.RolCelador.PantallaMascotasCelador
import com.example.app.Pantallas.RolCelador.PantallaMenuCelador
import com.example.app.Pantallas.RolCelador.PantallaMensajesCelador
import com.example.app.Pantallas.RolCelador.PantallaNotificacionesCelador
import com.example.app.Pantallas.RolCelador.PantallaPaqueteriaCelador
import com.example.app.Pantallas.RolCelador.PantallaPerfilCelador
import com.example.app.Pantallas.RolCelador.PantallaQuejasCelador
import com.example.app.Pantallas.RolCelador.PantallaRecibosCelador
import com.example.app.Pantallas.RolCelador.PantallaReservaPiscinaCelador
import com.example.app.Pantallas.RolCelador.PantallaReservaSalonComunalCelador
import com.example.app.Pantallas.RolCelador.PantallaReservaZonaBBQCelador
import com.example.app.Pantallas.RolCelador.PantallaReservasCelador

import com.example.app.Pantallas.RolResidente.PantallaAccesoPeatonalResidente
import com.example.app.Pantallas.RolResidente.PantallaAccesoVehicularResidente
import com.example.app.Pantallas.RolResidente.PantallaAccesosResidente
import com.example.app.Pantallas.RolResidente.PantallaInicioResidentes
import com.example.app.Pantallas.RolResidente.PantallaMascotasResidente
import com.example.app.Pantallas.RolResidente.PantallaMenuResidente
import com.example.app.Pantallas.RolResidente.PantallaNotificacionesResidente
import com.example.app.Pantallas.RolResidente.PantallaNuevaPublicacion
import com.example.app.Pantallas.RolResidente.PantallaPagos
import com.example.app.Pantallas.RolResidente.PantallaPerfilResidente
import com.example.app.Pantallas.RolResidente.PantallaQuejasResidente
import com.example.app.Pantallas.RolResidente.PantallaRecibos
import com.example.app.Pantallas.RolResidente.PantallaReservaGimnasio
import com.example.app.Pantallas.RolResidente.PantallaReservaPiscina
import com.example.app.Pantallas.RolResidente.PantallaReservaSalonComunal
import com.example.app.Pantallas.RolResidente.PantallaReservaZonaBBQ
import com.example.app.Pantallas.RolResidente.PantallaReservasResidente

import com.example.app.ViewModel.UsuarioViewModel

@Composable
fun Navegacion(navController: NavHostController) {
    // Obtenemos el VM via Hilt dentro de un contexto @Composable
    val usuarioViewModel: UsuarioViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "PantallaSeleccionRol"
    ) {
        // --- Comunes ---
        composable("PantallaSeleccionRol") {
            // Si tu firma de PantallaSeleccionRol SOLO recibe navController, quita el VM
            // PantallaSeleccionRol(navController)
            PantallaSeleccionRol(navController, usuarioViewModel)
        }
        composable("PantallaLogin") {
            PantallaLogin(navController, usuarioViewModel)
        }

        // --- ADMIN ---
        composable("PantallaInicioAdmin") { PantallaInicioAdmin(navController) }
        composable("PantallaCrearUsuario") { 
            PantallaCrearUsuario(navController, usuarioViewModel) 
        }
        composable("PantallaDashboardAdmin") { 
            PantallaDashboardAdmin(navController) 
        }
        composable("PantallaNotificaciones") { 
            PantallaNotificaciones(navController) 
        }
        composable("PantallaCreacionPublicacionAdmin") { 
            PantallaCreacionPublicacionAdmin(navController, usuarioViewModel) 
        }
        composable("PantallaMensajes/{nombre}") { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            PantallaMensajes(nombre = nombre, navController = navController)
        }
        composable("PantallaMenu") { PantallaMenu(navController) }
        composable("PantallaAccesos") { PantallaAccesos(navController) }
        composable("PantallaAccesoVehicularDetalle") { PantallaAccesoVehicularDetalle(navController) }
        composable("PantallaAccesoPeatonalDetalle") { PantallaAccesoPeatonalDetalle(navController) }
        composable("PantallaReservas") { PantallaReservas(navController) }
        composable("PantallaDetalleReservaPiscina") { PantallaDetalleReservaPiscina(navController) }
        composable("PantallaDetalleReservaSalonComunal") { PantallaDetalleReservaSalonComunal(navController) }
        composable("PantallaDetalleReservaZonaBBQ") { PantallaDetalleReservaZonaBBQ(navController) }
        composable("PantallaQuejas") { PantallaQuejas(navController) }
        composable("PantallaDetalleQuejas") { PantallaDetalleQuejas(navController) }
        composable("PantallaMascotas") { PantallaMascotas(navController) }
        composable("PantallaPerfil") { 
            PantallaPerfil(navController, usuarioViewModel) 
        }

        // --- RESIDENTE ---
        composable("PantallaInicioResidentes") { PantallaInicioResidentes(navController) }
        composable("PantallaNuevaPublicacion") { 
            PantallaNuevaPublicacion(navController, usuarioViewModel) 
        }
        composable("PantallaNotificacionesResidente") { PantallaNotificacionesResidente(navController) }
        composable("PantallaRecibos") { PantallaRecibos(navController) }
        composable("PantallaPagos") { PantallaPagos(navController) }
        composable("PantallaMenuResidente") { PantallaMenuResidente(navController) }
        composable("PantallaAccesosResidente") { PantallaAccesosResidente(navController) }
        composable("PantallaAccesoVehicularResidente") { PantallaAccesoVehicularResidente(navController) }
        composable("PantallaAccesoPeatonalResidente") { PantallaAccesoPeatonalResidente(navController) }
        composable("PantallaReservasResidente") { PantallaReservasResidente(navController) }
        composable("PantallaReservaPiscina") { PantallaReservaPiscina(navController) }
        composable("PantallaReservaSalonComunal") { PantallaReservaSalonComunal(navController) }
        composable("PantallaReservaGimnasio") { PantallaReservaGimnasio(navController) }
        composable("PantallaReservaZonaBBQ") { PantallaReservaZonaBBQ(navController) }
        composable("PantallaQuejasResidente") { PantallaQuejasResidente(navController) }
        composable("PantallaMascotasResidente") { PantallaMascotasResidente(navController) }
        composable("PantallaPerfilResidente") { PantallaPerfilResidente(navController) }

        // --- CELADOR ---
        composable("PantallaDashboardCelador") { PantallaDashboardCelador(navController) }
        composable("PantallaRecibosCelador") { PantallaRecibosCelador(navController) }
        composable("PantallaMenuCelador") { PantallaMenuCelador(navController) }
        composable("PantallaNotificacionesCelador") { PantallaNotificacionesCelador(navController) }
        composable("PantallaMensajesCelador") { PantallaMensajesCelador(navController) }
        composable("PantallaPaqueteriaCelador") { PantallaPaqueteriaCelador(navController) }
        composable("PantallaDetallesPaqueteriaCelador") { PantallaDetallesPaqueteriaCelador(navController) }
        composable("PantallaAccesosCelador") { PantallaAccesosCelador(navController) }
        composable("PantallaAccesoVehicularCelador") { PantallaAccesoVehicularCelador(navController) }
        composable("PantallaAccesoPeatonalCelador") { PantallaAccesoPeatonalCelador(navController) }
        composable("PantallaReservasCelador") { PantallaReservasCelador(navController) }
        composable("PantallaReservaPiscinaCelador") { PantallaReservaPiscinaCelador(navController) }
        composable("PantallaReservaSalonComunalCelador") { PantallaReservaSalonComunalCelador(navController) }
        composable("PantallaReservaZonaBBQCelador") { PantallaReservaZonaBBQCelador(navController) }
        composable("PantallaQuejasCelador") { PantallaQuejasCelador(navController) }
        composable("PantallaDetalleQuejasCelador") { PantallaDetalleQuejasCelador(navController) }
        composable("PantallaMascotasCelador") { PantallaMascotasCelador(navController) }
        composable("PantallaPerfilCelador") { PantallaPerfilCelador(navController) }
    }
}
