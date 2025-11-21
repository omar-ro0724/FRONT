package com.example.app.Pantallas.RolAdministrador

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.app.Model.Notificacion
import com.example.app.R
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.example.app.ui.theme.GrisOscuro
import com.example.app.ViewModel.NotificacionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaDashboardAdmin(
    navController: NavController,
    notificacionViewModel: NotificacionViewModel = hiltViewModel()
) {
    val notificaciones by notificacionViewModel.notificaciones.collectAsState()
    val isLoading by notificacionViewModel.isLoading.collectAsState()
    val error by notificacionViewModel.error.collectAsState()
    
    // Refrescar notificaciones cuando se entra a la pantalla
    LaunchedEffect(Unit) {
        try {
            notificacionViewModel.obtenerTodos()
        } catch (e: Exception) {
            // Error manejado por el ViewModel
        }
    }
    
    // Refrescar cuando se vuelve a esta pantalla desde otra
    LaunchedEffect(navController) {
        // Este efecto se ejecutará cuando la pantalla esté activa
        try {
            notificacionViewModel.obtenerTodos()
        } catch (e: Exception) {
            // Error manejado por el ViewModel
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzulOscuro)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GrisOscuro)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menú",
                tint = DoradoElegante,
                modifier = Modifier.clickable {
                    navController.navigate("PantallaMenu")
                }
            )
            Text("Rafael 24H", color = Color.White, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = DoradoElegante,
                modifier = Modifier.clickable {
                    navController.navigate("PantallaNotificaciones")
                }
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.conjunto),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Text(
                text = "Hacienda San Rafael",
                modifier = Modifier.padding(8.dp),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("NOVEDADES", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("+", color = DoradoElegante, fontSize = 28.sp, modifier = Modifier.clickable {
                navController.navigate("PantallaCreacionPublicacionAdmin")
            })
        }

        // Mensaje de error si existe
        error?.let { errorMessage ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }

        // Lista de publicaciones/notificaciones
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DoradoElegante)
            }
        } else {
            // Filtrar notificaciones válidas (usando la función helper del modelo)
            val notificacionesValidas = notificaciones.filter { 
                it.esValida()
            }
            
            if (notificacionesValidas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay publicaciones disponibles.",
                        color = GrisClaro,
                        fontSize = 16.sp
                    )
                }
            } else {
                val notificacionesReversed = notificacionesValidas.reversed()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        count = notificacionesReversed.size,
                        key = { index -> 
                            val notificacion = notificacionesReversed[index]
                            // Generar clave única: usar ID si existe, sino usar índice + hash del mensaje + fecha
                            notificacion.id ?: run {
                                val mensajeHash = notificacion.mensajeSeguro().hashCode().toLong()
                                val fechaHash = notificacion.fechaEnvio?.hashCode()?.toLong() ?: 0L
                                // Usar índice como parte de la clave para garantizar unicidad
                                index.toLong() * 1000000000L + mensajeHash * 1000L + fechaHash + index
                            }
                        }
                    ) { index ->
                        val notificacion = notificacionesReversed[index]
                        PublicacionItem(
                            notificacion = notificacion,
                            notificacionViewModel = notificacionViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PublicacionItem(
    notificacion: Notificacion,
    notificacionViewModel: NotificacionViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Header con usuario, fecha y menú de 3 puntos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del usuario
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DoradoElegante.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Usuario",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notificacion.usuario?.nombre ?: notificacion.usuario?.usuario ?: "Usuario",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    notificacion.fechaEnvio?.let { fecha ->
                        Text(
                            text = formatearFecha(fecha),
                            color = GrisClaro,
                            fontSize = 12.sp
                        )
                    }
                }
                // Menú de 3 puntos
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = AzulOscuro
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar", color = Color.White) },
                            onClick = {
                                showMenu = false
                                // TODO: Navegar a pantalla de edición
                                // navController.navigate("PantallaEditarPublicacion/${notificacion.id}")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color.Red
                                )
                            }
                        )
                    }
                }
            }

            // Mensaje/descripción (antes de la imagen, como en Facebook)
            val mensaje = notificacion.mensajeSeguro()
            if (mensaje.isNotBlank()) {
                Text(
                    text = mensaje,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Imagen si existe (ocupa todo el ancho, como en Facebook)
            val imagenPath = notificacion.imagenUrl
            if (imagenPath != null && imagenPath.isNotBlank()) {
                val file = remember(imagenPath) { File(imagenPath) }
                val fileExists = remember(file) { 
                    try {
                        file.exists() && file.canRead()
                    } catch (e: Exception) {
                        false
                    }
                }
                
                if (fileExists) {
                    Image(
                        painter = rememberAsyncImagePainter(file),
                        contentDescription = "Imagen de la publicación",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 400.dp)
                    )
                }
            }

            // Separador inferior
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Dialog de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar publicación", color = Color.White) },
            text = { Text("¿Estás seguro de que deseas eliminar esta publicación?", color = Color.White) },
            confirmButton = {
                TextButton(
                    onClick = {
                        notificacion.id?.let { id ->
                            notificacionViewModel.eliminar(id)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = AzulOscuro
        )
    }
}

fun formatearFecha(fechaISO: String?): String {
    if (fechaISO == null || fechaISO.isBlank()) {
        return "Fecha no disponible"
    }
    return try {
        // Intentar diferentes formatos de fecha
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        var date: Date? = null
        for (format in formats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                date = inputFormat.parse(fechaISO)
                if (date != null) break
            } catch (e: Exception) {
                // Continuar con el siguiente formato
            }
        }
        if (date != null) {
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            outputFormat.format(date)
        } else {
            fechaISO
        }
    } catch (e: Exception) {
        fechaISO
    }
}
