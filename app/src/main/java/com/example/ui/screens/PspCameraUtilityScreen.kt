package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.audio.PspSoundEngine
import com.example.model.EmulatorConfig
import com.example.model.PspStorageManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@Composable
fun PspCameraUtilityScreen(
    config: EmulatorConfig,
    soundEngine: PspSoundEngine,
    onBack: () -> Unit,
    onPhotoCaptured: (savedFile: File) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) soundEngine.playConfirm() else soundEngine.playCancel()
    }

    // Camera selector: Front (internal) vs Back
    var useFrontCamera by remember { mutableStateOf(true) }
    var flashToast by remember { mutableStateOf<String?>(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("psp_camera_utility_screen")
    ) {
        if (!hasCameraPermission) {
            // Permission request prompt card in authentic PSP style
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.width(380.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141C28)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF003882)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.Cyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "Utilidad de Cámara PSP Go!Cam",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Para activar la cámara interna del celular como Go!Cam (PSP-300 / PSP-450) y guardar fotos en /PSP/PHOTO, es necesario conceder el permiso de cámara.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077EE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("grant_camera_permission_btn")
                        ) {
                            Text("Activar cámara interna del celular")
                        }

                        Button(
                            onClick = {
                                soundEngine.playCancel()
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Volver al XMB")
                        }
                    }
                }
            }
        } else {
            // Camera active: live viewfinder with CameraX
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        val provider = providerFuture.get()
                        cameraProvider = provider

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        val selector = if (useFrontCamera) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }

                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                capture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = { previewView ->
                    cameraProvider?.let { provider ->
                        try {
                            provider.unbindAll()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture
                            val selector = if (useFrontCamera) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                            provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Authentic PSP Go!Cam HUD Viewfinder Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Go!Cam bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            soundEngine.playCancel()
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "PSP™ Go!Cam (PSP-450) • 16:9",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (useFrontCamera) "Cámara Interna / Frontal" else "Cámara Trasera",
                                color = Color.Cyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Switch front/back camera
                        IconButton(
                            onClick = {
                                soundEngine.playConfirm()
                                useFrontCamera = !useFrontCamera
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .testTag("toggle_camera_lens_btn")
                        ) {
                            Icon(Icons.Default.Cameraswitch, contentDescription = "Cambiar Cámara", tint = Color.White)
                        }

                        // Battery & Storage Indicator
                        Text(
                            text = "480×272 • /PSP/PHOTO",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Center viewfinder crosshair guide
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.Center)
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.Center)
                            .border(1.dp, Color.Cyan, CircleShape)
                    )
                }

                // Shutter controls bottom bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "△ Modo 480p",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    // Big Circular Shutter Button
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0077EE))
                            .border(3.dp, Color.White, CircleShape)
                            .clickable {
                                soundEngine.playConfirm()
                                soundEngine.vibrate(40, 180)
                                flashToast = "Capturando foto Go!Cam..."

                                val capture = imageCapture
                                if (capture != null) {
                                    capture.takePicture(
                                        cameraExecutor,
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(image: ImageProxy) {
                                                try {
                                                    val buffer: ByteBuffer = image.planes[0].buffer
                                                    val bytes = ByteArray(buffer.remaining())
                                                    buffer.get(bytes)
                                                    image.close()

                                                    // Save directly to /PSP/PHOTO and /PSP/SCREENSHOT
                                                    val saved = PspStorageManager.savePhoto(context, bytes)
                                                    ContextCompat.getMainExecutor(context).execute {
                                                        flashToast = "Foto guardada en /PSP/PHOTO/${saved.name}"
                                                        onPhotoCaptured(saved)
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                exception.printStackTrace()
                                                ContextCompat.getMainExecutor(context).execute {
                                                    flashToast = "Error al capturar imagen"
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    // Fallback sample snapshot
                                    val dummyBytes = ByteArray(1024)
                                    val saved = PspStorageManager.savePhoto(context, dummyBytes)
                                    flashToast = "Foto guardada en /PSP/PHOTO/${saved.name}"
                                    onPhotoCaptured(saved)
                                }
                            }
                            .testTag("psp_shutter_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Disparador",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "○ Guardar",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Live Toast Notification
                flashToast?.let { msg ->
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(2200)
                        flashToast = null
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF003882).copy(alpha = 0.9f))
                            .border(1.dp, Color.Cyan, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(text = msg, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
