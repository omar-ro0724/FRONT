package com.example.app.Pantallas.RolAdministrador

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import com.example.app.Model.Notificacion
import com.example.app.ui.theme.AzulOscuro
import com.example.app.ui.theme.DoradoElegante
import com.example.app.ui.theme.GrisClaro
import com.example.app.ViewModel.NotificacionViewModel
import com.example.app.ViewModel.UsuarioViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaCreacionPublicacionAdmin(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel = hiltViewModel(),
    notificacionViewModel: NotificacionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val isLoading by notificacionViewModel.isLoading.collectAsState()
    
    var descripcion by remember { mutableStateOf("") }
    var imagenSeleccionada by remember { mutableStateOf<String?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var publicacionGuardada by remember { mutableStateOf(false) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    val nombreUsuario = usuarioActual?.nombre ?: usuarioActual?.usuario ?: "Usuario"

    // Permisos
    val cameraPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.CAMERA
    } else {
        Manifest.permission.CAMERA
    }

    val hasCameraPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Función para crear archivo de imagen (debe definirse antes de usarse)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir).apply {
            imageFile = this
        }
    }

    // ActivityResultLauncher para tomar foto (debe definirse primero)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageFile != null) {
            imagenSeleccionada = imageFile!!.absolutePath
            mensajeError = null
        } else {
            mensajeError = "Error al tomar la foto"
        }
    }

    // ActivityResultLauncher para permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission.value = isGranted
        if (isGranted) {
            // Crear archivo y abrir cámara
            val file = createImageFile()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            takePictureLauncher.launch(uri)
        } else {
            mensajeError = "Permiso de cámara denegado"
        }
    }

    // Función para abrir cámara
    fun openCamera() {
        if (hasCameraPermission.value) {
            val file = createImageFile()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            takePictureLauncher.launch(uri)
        } else {
            permissionLauncher.launch(cameraPermission)
        }
    }

    LaunchedEffect(publicacionGuardada) {
        if (publicacionGuardada) {
            kotlinx.coroutines.delay(1500)
            navController.popBackStack()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AzulOscuro
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Creación Publicación",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Foto de usuario con icono por defecto
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = Color.White,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(nombreUsuario, color = Color.White, fontWeight = FontWeight.Bold)
                    if (usuarioActual?.torre != null && usuarioActual?.apartamento != null) {
                        Text(
                            "${usuarioActual?.torre} - ${usuarioActual?.apartamento}",
                            color = GrisClaro,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Imagen seleccionada (si hay)
            imagenSeleccionada?.let { imagenPath ->
                Image(
                    painter = rememberAsyncImagePainter(File(imagenPath)),
                    contentDescription = "Imagen Seleccionada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Campo de descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción de la publicación") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DoradoElegante,
                    unfocusedBorderColor = GrisClaro,
                    cursorColor = DoradoElegante,
                    focusedLabelColor = GrisClaro,
                    unfocusedLabelColor = GrisClaro,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Icons.Default.Person to "Etiqueta",
                    Icons.Default.Image to "Galería",
                    Icons.Default.VideoLibrary to "Video",
                    Icons.Default.PhotoCamera to "Cámara"
                ).forEach { (icon, desc) ->
                    IconButton(
                        onClick = {
                            when (desc) {
                                "Cámara" -> {
                                    openCamera()
                                }
                                "Galería" -> {
                                    mensajeError = "Funcionalidad de galería pendiente de implementar"
                                }
                                else -> {
                                    // Otras opciones pendientes
                                }
                            }
                        }
                    ) {
                        Icon(icon, contentDescription = desc, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Mensajes de éxito/error
            mensajeExito?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = Color.Green,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            mensajeError?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Botón compartir
            Button(
                onClick = {
                    if (descripcion.isBlank()) {
                        mensajeError = "Por favor ingresa una descripción"
                        mensajeExito = null
                    } else if (usuarioActual == null) {
                        mensajeError = "No hay usuario autenticado"
                        mensajeExito = null
                    } else {
                        mensajeError = null
                        val fechaActual = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
                        val notificacion = Notificacion(
                            mensaje = descripcion,
                            fechaEnvio = fechaActual,
                            usuario = usuarioActual,
                            imagenUrl = imagenSeleccionada  // Guardar ruta de la imagen
                        )
                        
                        coroutineScope.launch {
                            try {
                                notificacionViewModel.guardar(notificacion)
                                mensajeExito = "Publicación creada exitosamente"
                                mensajeError = null
                                
                                // Limpiar campos después de guardar
                                descripcion = ""
                                imagenSeleccionada = null
                                imageFile = null
                                
                                // Marcar como guardado y refrescar
                                publicacionGuardada = true
                                notificacionViewModel.obtenerTodos()
                            } catch (e: Exception) {
                                mensajeError = "Error al guardar publicación: ${e.message}"
                                mensajeExito = null
                            }
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DoradoElegante),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AzulOscuro
                    )
                } else {
                    Text("Compartir", color = AzulOscuro, fontSize = 16.sp)
                }
            }
        }
    }
}
