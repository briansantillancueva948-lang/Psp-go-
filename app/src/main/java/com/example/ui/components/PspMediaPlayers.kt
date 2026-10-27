package com.example.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.audio.PspSoundEngine
import com.example.model.EmulatorConfig
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * Authentic Sony PSP UMD Video Player with real-time video playback.
 */
@Composable
fun PspUmdVideoPlayerDialog(
    videoUri: Uri? = null,
    title: String = "UMD® Video • PSP Showcase 1080p",
    soundEngine: PspSoundEngine,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var playbackProgress by remember { mutableFloatStateOf(0.15f) }
    var durationText by remember { mutableStateOf("03:42") }
    var currentPositionText by remember { mutableStateOf("00:45") }
    var showControls by remember { mutableStateOf(true) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            if (playbackProgress < 0.99f) {
                playbackProgress += 0.005f
                val totalSec = 222
                val currentSec = (totalSec * playbackProgress).toInt()
                currentPositionText = "%02d:%02d".format(currentSec / 60, currentSec % 60)
            } else {
                playbackProgress = 0f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showControls = !showControls }
            .testTag("psp_video_player_dialog"),
        contentAlignment = Alignment.Center
    ) {
        if (videoUri != null) {
            // Real device video playback using Android VideoView
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(videoUri)
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            start()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Simulated real-time PSP UMD Video Trailer with animated canvas
            var frameTick by remember { mutableIntStateOf(0) }
            LaunchedEffect(isPlaying) {
                while (isPlaying) {
                    delay(33)
                    frameTick++
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw cinematic widescreen letterbox
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0A1828), Color(0xFF003882), Color(0xFF05111E))
                    ),
                    size = size
                )

                // High-tech raster scan grid lines
                for (i in 0 until 12) {
                    val y = (h / 12) * i + (frameTick * 2 % (h / 12))
                    drawLine(
                        color = Color.Cyan.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.5f
                    )
                }

                // Dynamic pulsing radar / audio waveform
                val points = 60
                val step = w / points
                for (i in 0 until points) {
                    val x = i * step
                    val waveY = h / 2 + sin((i + frameTick * 0.15).toDouble()).toFloat() * 60f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        radius = 3.dp.toPx(),
                        center = Offset(x, waveY)
                    )
                }
            }
        }

        // Overlayed PSP Controls HUD
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.Cyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "UMD® Video • AVC/H.264 • 29.97 fps • Audio Estéreo 48 kHz",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = {
                        soundEngine.playCancel()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                // Bottom Playback Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = currentPositionText, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Text(text = durationText, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    LinearProgressIndicator(
                        progress = { playbackProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color.Cyan,
                        trackColor = Color.DarkGray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            soundEngine.playConfirm()
                            playbackProgress = (playbackProgress - 0.1f).coerceAtLeast(0f)
                        }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Atrás", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                soundEngine.playConfirm()
                                isPlaying = !isPlaying
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0077EE))
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausa" else "Reproducir",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(onClick = {
                            soundEngine.playConfirm()
                            playbackProgress = (playbackProgress + 0.1f).coerceAtMost(1f)
                        }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Authentic Sony PSP Music Player with animated silk wave visualizer & spectrum bars.
 */
@Composable
fun PspMusicPlayerDialog(
    trackTitle: String = "PSP Silk Ambient Theme",
    artist: String = "Sony Computer Entertainment Inc.",
    soundEngine: PspSoundEngine,
    onDismiss: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentSeconds by remember { mutableIntStateOf(34) }
    val totalSeconds = 210

    // Spectrum analyzer bars
    var animTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100)
            animTick++
            if (animTick % 10 == 0) {
                currentSeconds = (currentSeconds + 1) % totalSeconds
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17).copy(alpha = 0.95f))
            .testTag("psp_music_player_dialog"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .border(1.dp, Color.Cyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101724)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Cyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reproductor de Audio PSP™",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = {
                        soundEngine.playCancel()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                // Spectrum Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B101A))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val numBars = 24
                        val barWidth = size.width / (numBars * 1.5f)
                        val maxH = size.height

                        for (i in 0 until numBars) {
                            val hFraction = ((sin((i + animTick * 0.4).toDouble()) + 1) / 2).toFloat()
                            val barHeight = (maxH * 0.2f) + (maxH * 0.75f * hFraction)
                            val x = i * (barWidth * 1.5f)
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Cyan, Color(0xFF0055AA))
                                ),
                                topLeft = Offset(x, maxH - barHeight),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                            )
                        }
                    }
                }

                // Track Info
                Column {
                    Text(
                        text = trackTitle,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$artist • /PSP/MUSIC • MP3 320 kbps",
                        color = Color.Cyan,
                        fontSize = 12.sp
                    )
                }

                // Time Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val progress = currentSeconds.toFloat() / totalSeconds.toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color.Cyan,
                        trackColor = Color(0xFF1E2838)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "%02d:%02d".format(currentSeconds / 60, currentSeconds % 60),
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60),
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        soundEngine.playConfirm()
                        currentSeconds = (currentSeconds - 15).coerceAtLeast(0)
                    }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Atrás", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            soundEngine.playConfirm()
                            isPlaying = !isPlaying
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0077EE))
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(onClick = {
                        soundEngine.playConfirm()
                        currentSeconds = (currentSeconds + 15).coerceAtMost(totalSeconds)
                    }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = Color.White)
                    }
                }
            }
        }
    }
}
