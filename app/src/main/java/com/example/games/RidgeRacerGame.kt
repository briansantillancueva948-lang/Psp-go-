package com.example.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PspSoundEngine
import com.example.model.EmulatorConfig
import com.example.model.PostProcessingShader
import kotlinx.coroutines.delay
import kotlin.math.sin

class GameInputState {
    var up = false
    var down = false
    var left = false
    var right = false
    var analogX = 0f
    var analogY = 0f
    var triangle = false
    var circle = false
    var cross = false
    var square = false
    var shoulderL = false
    var shoulderR = false
    var turbo = false
}

@Composable
fun RidgeRacerGame(
    config: EmulatorConfig,
    input: GameInputState,
    soundEngine: PspSoundEngine,
    modifier: Modifier = Modifier
) {
    var carSpeed by remember { mutableFloatStateOf(0f) }
    var carX by remember { mutableFloatStateOf(0f) }
    var distanceTraveled by remember { mutableFloatStateOf(0f) }
    var nitroFuel by remember { mutableFloatStateOf(100f) }
    var lapTimeMs by remember { mutableLongStateOf(0L) }
    var currentGear by remember { mutableIntStateOf(1) }
    var driftScore by remember { mutableIntStateOf(0) }

    // 60 FPS Game Loop
    LaunchedEffect(input.turbo) {
        val frameDelay = if (input.turbo) 8L else 16L
        while (true) {
            val dt = 0.016f * (if (input.turbo) 2f else 1f)

            // Steering with D-pad or Analog stick
            val steerInput = when {
                input.left -> -1f
                input.right -> 1f
                else -> input.analogX
            }

            // Acceleration & Braking
            val isAccelerating = input.cross || input.up
            val isBraking = input.square || input.down
            val isNitro = (input.triangle || input.shoulderR) && nitroFuel > 5f

            if (isNitro) {
                nitroFuel = (nitroFuel - 22f * dt).coerceAtLeast(0f)
                carSpeed = (carSpeed + 160f * dt).coerceAtMost(285f)
                if (Math.random() < 0.2) soundEngine.playBoost()
            } else if (isAccelerating) {
                val maxSpeed = when (currentGear) {
                    1 -> 75f
                    2 -> 135f
                    3 -> 195f
                    else -> 245f
                }
                if (carSpeed < maxSpeed) {
                    carSpeed += 75f * dt
                }
                if (carSpeed > 65f && currentGear == 1) currentGear = 2
                if (carSpeed > 125f && currentGear == 2) currentGear = 3
                if (carSpeed > 185f && currentGear == 3) currentGear = 4
            } else if (isBraking) {
                carSpeed = (carSpeed - 120f * dt).coerceAtLeast(0f)
                if (carSpeed < 50f && currentGear > 1) currentGear = 1
            } else {
                // Natural drag
                carSpeed = (carSpeed - 25f * dt).coerceAtLeast(0f)
            }

            // Drifting
            if (isBraking && kotlin.math.abs(steerInput) > 0.4f && carSpeed > 60f) {
                driftScore += (carSpeed * 0.15f).toInt()
                soundEngine.vibrate(12, 100)
            }

            // Move position
            carX = (carX + steerInput * (carSpeed / 120f) * dt * 2.2f).coerceIn(-1.5f, 1.5f)
            distanceTraveled += carSpeed * dt * 0.4f
            lapTimeMs += (dt * 1000).toLong()

            // Nitro regen slowly
            if (!isNitro && nitroFuel < 100f) {
                nitroFuel = (nitroFuel + 6f * dt).coerceAtMost(100f)
            }

            delay(frameDelay)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0A0F1A))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Sky & Neon City Horizon
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030712),
                        Color(0xFF1E103A),
                        Color(0xFF511A5E),
                        Color(0xFF1A1429)
                    ),
                    startY = 0f,
                    endY = h * 0.48f
                ),
                size = Size(w, h * 0.48f)
            )

            // Skyline neon buildings
            val bldgCount = 18
            val bWidth = w / bldgCount
            for (i in 0 until bldgCount) {
                val bh = 30f + ((i * 37) % 70)
                val bx = i * bWidth - (carX * 25f)
                drawRect(
                    color = if (i % 2 == 0) Color(0xFF131728) else Color(0xFF1C2237),
                    topLeft = Offset(bx, h * 0.48f - bh),
                    size = Size(bWidth + 2f, bh)
                )
                // Window grid
                drawCircle(
                    color = if (i % 3 == 0) Color.Cyan.copy(alpha = 0.7f) else Color(0xFFFF4081).copy(alpha = 0.7f),
                    radius = 2f,
                    center = Offset(bx + bWidth * 0.5f, h * 0.48f - bh * 0.6f)
                )
            }

            // 2. 3D Track Road Projection
            val horizonY = h * 0.48f
            val roadBottomW = w * 0.85f
            val roadTopW = w * 0.08f
            val centerOffset = carX * (w * 0.35f)

            // Grass sides
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF14241B), Color(0xFF09140C)),
                    startY = horizonY,
                    endY = h
                ),
                topLeft = Offset(0f, horizonY),
                size = Size(w, h - horizonY)
            )

            // Road surface polygon
            val roadPath = Path().apply {
                moveTo(w * 0.5f - roadTopW * 0.5f - centerOffset * 0.15f, horizonY)
                lineTo(w * 0.5f + roadTopW * 0.5f - centerOffset * 0.15f, horizonY)
                lineTo(w * 0.5f + roadBottomW * 0.5f - centerOffset, h)
                lineTo(w * 0.5f - roadBottomW * 0.5f - centerOffset, h)
                close()
            }
            drawPath(roadPath, color = Color(0xFF181C24))

            // Road stripes & curbs
            val numSegments = 16
            for (s in 0 until numSegments) {
                val z1 = (s.toFloat() + (distanceTraveled % 1f)) / numSegments
                val z2 = ((s + 0.5f) + (distanceTraveled % 1f)) / numSegments

                val y1 = horizonY + (h - horizonY) * (z1 * z1)
                val y2 = horizonY + (h - horizonY) * (z2 * z2)

                val rw1 = roadTopW + (roadBottomW - roadTopW) * (z1 * z1)
                val rw2 = roadTopW + (roadBottomW - roadTopW) * (z2 * z2)

                val cx1 = w * 0.5f - centerOffset * (z1 * z1)
                val cx2 = w * 0.5f - centerOffset * (z2 * z2)

                // Alternate curb colors (Red / White)
                val curbColor = if ((s + (distanceTraveled * 2).toInt()) % 2 == 0) Color(0xFFFF1744) else Color.White
                // Left curb
                drawPath(
                    Path().apply {
                        moveTo(cx1 - rw1 * 0.5f, y1)
                        lineTo(cx1 - rw1 * 0.52f, y1)
                        lineTo(cx2 - rw2 * 0.52f, y2)
                        lineTo(cx2 - rw2 * 0.5f, y2)
                        close()
                    },
                    color = curbColor
                )
                // Right curb
                drawPath(
                    Path().apply {
                        moveTo(cx1 + rw1 * 0.5f, y1)
                        lineTo(cx1 + rw1 * 0.52f, y1)
                        lineTo(cx2 + rw2 * 0.52f, y2)
                        lineTo(cx2 + rw2 * 0.5f, y2)
                        close()
                    },
                    color = curbColor
                )

                // Center dashed line
                if ((s + (distanceTraveled * 2).toInt()) % 2 == 0) {
                    val cw1 = 4f * (z1 * z1 + 0.2f)
                    val cw2 = 4f * (z2 * z2 + 0.2f)
                    drawPath(
                        Path().apply {
                            moveTo(cx1 - cw1, y1)
                            lineTo(cx1 + cw1, y1)
                            lineTo(cx2 + cw2, y2)
                            lineTo(cx2 - cw2, y2)
                            close()
                        },
                        color = Color(0xFFFFD600)
                    )
                }
            }

            // 3. Player Sports Car (Rear View 3D Rendering)
            val carY = h * 0.82f
            val carW = 140f
            val carH = 55f
            val carCenterX = w * 0.5f

            // Shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.6f),
                topLeft = Offset(carCenterX - carW * 0.55f, carY + carH * 0.7f),
                size = Size(carW * 1.1f, 22f)
            )

            // Car Body (Aerodynamic wedge sports car)
            val bodyPath = Path().apply {
                moveTo(carCenterX - carW * 0.5f, carY + carH)
                lineTo(carCenterX - carW * 0.45f, carY + carH * 0.35f)
                lineTo(carCenterX - carW * 0.3f, carY)
                lineTo(carCenterX + carW * 0.3f, carY)
                lineTo(carCenterX + carW * 0.45f, carY + carH * 0.35f)
                lineTo(carCenterX + carW * 0.5f, carY + carH)
                close()
            }
            drawPath(
                bodyPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFF0091EA), Color(0xFF01579B)),
                    startY = carY,
                    endY = carY + carH
                )
            )

            // Rear Windshield
            drawPath(
                Path().apply {
                    moveTo(carCenterX - carW * 0.26f, carY + 4f)
                    lineTo(carCenterX + carW * 0.26f, carY + 4f)
                    lineTo(carCenterX + carW * 0.35f, carY + carH * 0.4f)
                    lineTo(carCenterX - carW * 0.35f, carY + carH * 0.4f)
                    close()
                },
                color = Color(0xFF102027)
            )

            // Neon Tail Lights
            drawRoundRect(
                color = Color(0xFFFF1744),
                topLeft = Offset(carCenterX - carW * 0.45f, carY + carH * 0.45f),
                size = Size(28f, 10f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color(0xFFFF1744),
                topLeft = Offset(carCenterX + carW * 0.45f - 28f, carY + carH * 0.45f),
                size = Size(28f, 10f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )

            // Dual Exhaust with flame if nitro
            if ((input.triangle || input.shoulderR) && nitroFuel > 5f) {
                drawCircle(
                    color = Color.Cyan,
                    radius = 12f + (sin(distanceTraveled * 15f) * 4f),
                    center = Offset(carCenterX - carW * 0.22f, carY + carH + 4f)
                )
                drawCircle(
                    color = Color.Cyan,
                    radius = 12f + (sin(distanceTraveled * 15f) * 4f),
                    center = Offset(carCenterX + carW * 0.22f, carY + carH + 4f)
                )
            }

            // 4. Post-processing Shader Effects (CRT, Vibrant, etc.)
            applyShaderEffect(config.shader, size)
        }

        // HUD OVERLAY: Speedometer, Gear, Lap Time, Nitro
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text(
                text = "${carSpeed.toInt()} KM/H",
                color = Color.Cyan,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "MARCHA: $currentGear  |  PUNTOS DRIFT: $driftScore",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            val sec = (lapTimeMs / 1000) % 60
            val min = (lapTimeMs / 60000)
            val ms = (lapTimeMs % 1000) / 10
            Text(
                text = "TIEMPO VUELTA: %02d:%02d.%02d".format(min, sec, ms),
                color = Color.Yellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Nitro Gauge top right
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "NITRO (△ / R)",
                color = if (nitroFuel > 15f) Color.Cyan else Color.Red,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 10.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Blue, Color.Cyan)
                            )
                        )
                )
            }
        }
    }
}

fun DrawScope.applyShaderEffect(shader: PostProcessingShader, size: Size) {
    when (shader) {
        PostProcessingShader.CRT_SCANLINES -> {
            // Horizontal CRT scanlines
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.28f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2.5f
                )
                y += 5f
            }
        }
        PostProcessingShader.VIBRANT -> {
            // Subtle high-contrast vignette
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.32f)),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.width * 0.75f
                )
            )
        }
        PostProcessingShader.RETRO_AMBER -> {
            drawRect(color = Color(0xFFFFB300).copy(alpha = 0.15f))
        }
        PostProcessingShader.MONOCHROME -> {
            drawRect(color = Color.Black.copy(alpha = 0.22f))
        }
        else -> {}
    }
}
