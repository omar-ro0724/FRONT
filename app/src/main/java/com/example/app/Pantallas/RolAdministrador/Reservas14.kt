package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Pool
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro

@Composable
fun PantallaReservas(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = GrisClaro
                )
            }
            Text(
                text = "Reservas",
                style = MaterialTheme.typography.headlineSmall,
                color = GrisClaro,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

        ReservaCategoria(
            titulo = "Piscina",
            icono = Icons.Default.Pool,
            reservas = emptyList(), // vacía para mostrar mensaje
            onReservaClick = { navController.navigate("PantallaDetalleReservaPiscina") }
        )

        ReservaCategoria(
            titulo = "Salón Comunal",
            icono = Icons.Default.Home,
            reservas = emptyList(), // vacía para mostrar mensaje
            onReservaClick = { navController.navigate("PantallaDetalleReservaSalonComunal") }
        )

        ReservaCategoria(
            titulo = "Gimnasio",
            icono = Icons.Default.FitnessCenter,
            reservas = emptyList(),
            onReservaClick = {}
        )

        ReservaCategoria(
            titulo = "Zona BBQ",
            icono = Icons.Default.OutdoorGrill,
            reservas = emptyList(), // vacía para mostrar mensaje
            onReservaClick = { navController.navigate("PantallaDetalleReservaZonaBBQ") }
        )
    }
}
@Composable
fun ReservaCategoria(
    titulo: String,
    icono: ImageVector,
    reservas: List<String>,
    onReservaClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = DoradoElegante,
                modifier = Modifier.size(35.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        Text(
            text = "0 Reservas",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 35.dp, top = 2.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (reservas.isEmpty()) {
            Text(
                text = "No hay reservas creadas",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 35.dp, top = 6.dp)
            )
        } else {
            reservas.forEach { reserva ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .clickable { onReservaClick() }
                        .padding(12.dp)
                ) {
                    Text(text = reserva, color = Color.White)
                }
            }
        }
    }
}
