package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PspSoundEngine
import com.example.model.EmulatorConfig
import com.example.ui.components.PspWaveBackground
import kotlinx.coroutines.delay

enum class UpdatePhase {
    SELECT_METHOD,
    CHECKING,
    CHANGELOG,
    DOWNLOADING,
    FLASHING,
    COMPLETE
}

@Composable
fun PspSystemUpdateScreen(
    config: EmulatorConfig,
    soundEngine: PspSoundEngine,
    onBack: () -> Unit,
    onUpdateComplete: (newVersion: String) -> Unit
) {
    var phase by remember { mutableStateOf(UpdatePhase.SELECT_METHOD) }
    var selectedMethod by remember { mutableStateOf("internet") } // "internet" or "storage"
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Conectando con el servidor de actualización...") }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "update_progress")

    // Automated download & flashing flow
    LaunchedEffect(phase) {
        when (phase) {
            UpdatePhase.CHECKING -> {
                delay(1200)
                phase = UpdatePhase.CHANGELOG
            }
            UpdatePhase.DOWNLOADING -> {
                progress = 0f
                statusText = "Descargando paquete de actualización PSP™ (31.4 MB)..."
                while (progress < 0.85f) {
                    delay(80)
                    progress += 0.035f
                }
                statusText = "Verificando suma de comprobación MD5 y firma digital SCE..."
                delay(600)
                progress = 1.0f
                delay(400)
                phase = UpdatePhase.FLASHING
            }
            UpdatePhase.FLASHING -> {
                progress = 0f
                statusText = "Escribiendo en memoria flash0:/kd/boot.bin..."
                while (progress < 1.0f) {
                    delay(60)
                    progress += 0.04f
                    if (progress > 0.5f) {
                        statusText = "Instalando módulos de emulación y Popsloader..."
                    }
                }
                statusText = "Actualización del sistema instalada correctamente."
                delay(500)
                soundEngine.playConfirm()
                phase = UpdatePhase.COMPLETE
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D14))
            .testTag("psp_system_update_screen")
    ) {
        // Authentic animated wave background
        PspWaveBackground(
            waveColor = config.waveColor,
            modifier = Modifier.fillMaxSize()
        )

        // Dark translucent overlay for crisp readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundEngine.playCancel()
                        onBack()
                    },
                    modifier = Modifier.testTag("update_back_btn")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Actualización del sistema PSP™",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Software actual: ${config.systemFirmware} • Modelo: ${config.pspModel.label}",
                        color = Color.Cyan,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content depending on phase
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (phase) {
                    UpdatePhase.SELECT_METHOD -> {
                        MethodSelectionCard(
                            onSelectInternet = {
                                soundEngine.playConfirm()
                                selectedMethod = "internet"
                                phase = UpdatePhase.CHECKING
                            },
                            onSelectStorage = {
                                soundEngine.playConfirm()
                                selectedMethod = "storage"
                                phase = UpdatePhase.CHECKING
                            }
                        )
                    }

                    UpdatePhase.CHECKING -> {
                        CheckingProgressCard(selectedMethod = selectedMethod)
                    }

                    UpdatePhase.CHANGELOG -> {
                        ChangelogCard(
                            onStartUpdate = {
                                soundEngine.playConfirm()
                                phase = UpdatePhase.DOWNLOADING
                            },
                            onCancel = {
                                soundEngine.playCancel()
                                phase = UpdatePhase.SELECT_METHOD
                            }
                        )
                    }

                    UpdatePhase.DOWNLOADING, UpdatePhase.FLASHING -> {
                        ProgressCard(
                            phase = phase,
                            progress = animatedProgress,
                            statusText = statusText
                        )
                    }

                    UpdatePhase.COMPLETE -> {
                        CompleteCard(
                            onRestart = {
                                soundEngine.playConfirm()
                                onUpdateComplete("6.61 PRO-C∞")
                            }
                        )
                    }
                }
            }

            // PSP Action Footer Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✕ Seleccionar    ○ Volver al XMB",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun MethodSelectionCard(
    onSelectInternet: () -> Unit,
    onSelectStorage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121824).copy(alpha = 0.9f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Seleccione el método de actualización:",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Method 1: Internet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2838))
                    .border(1.dp, Color.Cyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onSelectInternet() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0055AA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Actualización mediante Internet",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Conectar a la red para descargar la última versión de PPSSPP / Firmware PSP",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            // Method 2: Storage Media
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2838))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable { onSelectStorage() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334466)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SdCard, contentDescription = null, tint = Color.Cyan)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Actualización mediante soporte de almacenamiento",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Buscar paquete en /PSP/GAME/UPDATE/EBOOT.PBP en Memory Stick™",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckingProgressCard(selectedMethod: String) {
    Card(
        modifier = Modifier.width(360.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121824).copy(alpha = 0.92f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = Color.Cyan, strokeWidth = 3.dp)
            Text(
                text = if (selectedMethod == "internet")
                    "Comprobando versión en los servidores de actualización..."
                else
                    "Escaneando soporte de almacenamiento /PSP/GAME/UPDATE...",
                color = Color.White,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChangelogCard(
    onStartUpdate: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Cyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121824).copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Software del sistema PSP™ 6.61 PRO-C∞",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "PPSSPP Gold v1.19.0 Enhanced • Tamaño: 31.4 MB",
                        color = Color.Cyan,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF00AA44))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DISPONIBLE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Características de la nueva versión:",
                color = Color(0xFFFFCC00),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            val notes = listOf(
                "• Visor en tiempo real de RAM física del celular, RAM PSP (64MB) y FPS.",
                "• Integración de la cámara interna/frontal del celular en la Utilidad de Cámara Go!Cam.",
                "• Creación y almacenamiento automático de carpetas originales PSP (/GAME, /SAVEDATA, /Cheats).",
                "• Auto-detección de carátulas originales para juegos ISO, CSO y BIN.",
                "• Compatibilidad total con PlayStation 1 Classics (.bin/.cue/.pbp).",
                "• Optimizaciones gráficas Vulkan de 60 FPS estables con latencia reducida."
            )

            notes.forEach { note ->
                Text(text = note, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancelar")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onStartUpdate,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077EE)),
                    modifier = Modifier.testTag("start_update_btn")
                ) {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Actualizar ahora")
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(
    phase: UpdatePhase,
    progress: Float,
    statusText: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121824).copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (phase == UpdatePhase.DOWNLOADING) "Descargando actualización..." else "Instalando en flash0:/...",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = Color.Cyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color.Cyan,
                trackColor = Color(0xFF1E2838)
            )

            Text(
                text = statusText,
                color = Color.LightGray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun CompleteCard(onRestart: () -> Unit) {
    Card(
        modifier = Modifier
            .width(420.dp)
            .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121824).copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF00E676),
                modifier = Modifier.size(54.dp)
            )

            Text(
                text = "Actualización completada",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "El software del sistema PSP™ se ha actualizado a la versión 6.61 PRO-C∞. Pulse el botón para reiniciar el sistema con la intro original.",
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restart_psp_btn")
            ) {
                Text(
                    text = "✕ Reiniciar sistema PSP™",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
