package com.example.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PspSoundEngine
import com.example.model.EmulatorConfig
import kotlinx.coroutines.delay
import kotlin.math.sin

data class CrashCrate(
    val zDist: Float,
    val xPos: Float,
    val isTnt: Boolean = false,
    var smashed: Boolean = false
)

@Composable
fun CrashWarpedGame(
    config: EmulatorConfig,
    input: GameInputState,
    soundEngine: PspSoundEngine,
    modifier: Modifier = Modifier
) {
    var crashX by remember { mutableFloatStateOf(0f) }
    var crashJumpY by remember { mutableFloatStateOf(0f) }
    var isSpinning by remember { mutableFloatStateOf(0f) }
    var runDistance by remember { mutableFloatStateOf(0f) }
    var wumpaCount by remember { mutableIntStateOf(34) }
    var livesCount by remember { mutableIntStateOf(99) }
    var cratesSmashed by remember { mutableIntStateOf(18) }

    val crates = remember {
        mutableStateListOf(
            CrashCrate(120f, -0.4f),
            CrashCrate(240f, 0.4f, isTnt = true),
            CrashCrate(360f, 0.0f),
            CrashCrate(480f, -0.3f),
            CrashCrate(600f, 0.3f)
        )
    }

    // 60 FPS Game Loop
    LaunchedEffect(input.turbo) {
        val frameDelay = if (input.turbo) 8L else 16L
        while (true) {
            val dt = 0.016f * (if (input.turbo) 2f else 1f)

            // Steering Left/Right
            val steer = when {
                input.left -> -1f
                input.right -> 1f
                else -> input.analogX
            }
            crashX = (crashX + steer * 1.8f * dt).coerceIn(-1.2f, 1.2f)

            // Jump with ✕
            if (input.cross && crashJumpY <= 0.01f) {
                crashJumpY = 1.0f
                soundEngine.playConfirm()
                soundEngine.vibrate(15, 130)
            }
            if (crashJumpY > 0f) {
                crashJumpY = (crashJumpY - 2.8f * dt).coerceAtLeast(0f)
            }

            // Spin Attack with □
            if (input.square && isSpinning <= 0f) {
                isSpinning = 0.45f
                soundEngine.playSlash()
                soundEngine.vibrate(22, 170)
            }
            if (isSpinning > 0f) {
                isSpinning -= dt
            }

            // Forward progression
            runDistance += 120f * dt

            // Check crates hit
            crates.forEach { crate ->
                if (!crate.smashed) {
                    val relativeZ = crate.zDist - (runDistance % 700f)
                    if (relativeZ in -20f..30f && kotlin.math.abs(crashX - crate.xPos) < 0.45f) {
                        if (isSpinning > 0f || crashJumpY > 0.1f) {
                            crate.smashed = true
                            cratesSmashed++
                            wumpaCount += 5
                            soundEngine.playBoost()
                            soundEngine.vibrate(25, 200)
                        }
                    }
                } else if ((runDistance % 700f) < 50f) {
                    crate.smashed = false // loop crates
                }
            }

            delay(frameDelay)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Time Warp Medieval / Egyptian Portal Sky
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B0A2A), Color(0xFF4A148C), Color(0xFF7B1FA2)),
                    startY = 0f,
                    endY = h * 0.48f
                ),
                size = Size(w, h * 0.48f)
            )

            // Swirling Time Portal Vortex in sky
            val vortexCx = w * 0.5f
            val vortexCy = h * 0.28f
            for (r in 5 downTo 1) {
                drawCircle(
                    color = if (r % 2 == 0) Color.Cyan.copy(alpha = 0.4f) else Color(0xFFFFD600).copy(alpha = 0.4f),
                    radius = r * 22f + (sin(runDistance * 0.05f) * 6f),
                    center = Offset(vortexCx, vortexCy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                )
            }

            // Egyptian / Medieval Stone Pathway (Perspective Corridor)
            val horizonY = h * 0.48f
            val pathTopW = w * 0.12f
            val pathBottomW = w * 0.72f
            val roadPath = Path().apply {
                moveTo(w * 0.5f - pathTopW * 0.5f, horizonY)
                lineTo(w * 0.5f + pathTopW * 0.5f, horizonY)
                lineTo(w * 0.5f + pathBottomW * 0.5f, h)
                lineTo(w * 0.5f - pathBottomW * 0.5f, h)
                close()
            }
            drawPath(roadPath, color = Color(0xFFD7CCC8))

            // Sand / Castle Wall sides
            drawRect(
                color = Color(0xFF5D4037),
                topLeft = Offset(0f, horizonY),
                size = Size(w * 0.5f - pathBottomW * 0.5f, h - horizonY)
            )
            drawRect(
                color = Color(0xFF5D4037),
                topLeft = Offset(w * 0.5f + pathBottomW * 0.5f, horizonY),
                size = Size(w, h - horizonY)
            )

            // Crates on track
            crates.forEach { crate ->
                if (!crate.smashed) {
                    val relZ = (crate.zDist - (runDistance % 700f)).coerceAtLeast(10f)
                    val depthFactor = (1.0f - (relZ / 700f)).coerceIn(0.1f, 1f)
                    val crateY = horizonY + (h - horizonY) * depthFactor
                    val crateX = w * 0.5f + (crate.xPos * pathBottomW * 0.45f * depthFactor)
                    val crateSize = 48f * depthFactor

                    // Crate box
                    drawRect(
                        color = if (crate.isTnt) Color(0xFFFF1744) else Color(0xFF8D6E63),
                        topLeft = Offset(crateX - crateSize * 0.5f, crateY - crateSize),
                        size = Size(crateSize, crateSize)
                    )
                    // Crate border
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(crateX - crateSize * 0.5f, crateY - crateSize),
                        size = Size(crateSize, crateSize),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
            }

            // Crash Bandicoot Character (PS1 Low-Poly Look)
            val charBaseY = h * 0.82f - (crashJumpY * 90f)
            val charBaseX = w * 0.5f + (crashX * 160f)
            val charW = 44f
            val charH = 64f

            // Shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(charBaseX - 22f, h * 0.84f),
                size = Size(44f, 14f)
            )

            // If spinning, draw tornado blur
            if (isSpinning > 0f) {
                drawCircle(
                    color = Color(0xFFFF9100).copy(alpha = 0.8f),
                    radius = 36f,
                    center = Offset(charBaseX, charBaseY - 26f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 28f,
                    center = Offset(charBaseX, charBaseY - 26f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                )
            } else {
                // Crash Orange Torso
                drawRoundRect(
                    color = Color(0xFFFF6D00),
                    topLeft = Offset(charBaseX - 16f, charBaseY - charH * 0.7f),
                    size = Size(32f, 32f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                // Blue Denim Jeans
                drawRect(
                    color = Color(0xFF1565C0),
                    topLeft = Offset(charBaseX - 14f, charBaseY - charH * 0.28f),
                    size = Size(28f, 18f)
                )
                // Red High-top Sneakers
                drawRect(
                    color = Color(0xFFD50000),
                    topLeft = Offset(charBaseX - 18f, charBaseY - 4f),
                    size = Size(16f, 10f)
                )
                drawRect(
                    color = Color(0xFFD50000),
                    topLeft = Offset(charBaseX + 2f, charBaseY - 4f),
                    size = Size(16f, 10f)
                )
                // Big Eyes & Smile
                drawCircle(color = Color.White, radius = 5f, center = Offset(charBaseX - 5f, charBaseY - 38f))
                drawCircle(color = Color.White, radius = 5f, center = Offset(charBaseX + 5f, charBaseY - 38f))
                drawCircle(color = Color.Black, radius = 2.5f, center = Offset(charBaseX - 5f, charBaseY - 38f))
                drawCircle(color = Color.Black, radius = 2.5f, center = Offset(charBaseX + 5f, charBaseY - 38f))
            }

            // Post-processing
            applyShaderEffect(config.shader, size)
        }

        // HUD: Wumpa Fruit, Lives, Crates (Classic PS1 HUD)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🍎 WUMPA: $wumpaCount / 100",
                    color = Color(0xFFFFB300),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "📦 CAJAS: $cratesSmashed / 45",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "VIDAS: x$livesCount",
                color = Color(0xFF00E676),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // PS1 Classics Badge top right
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "PS1 POPS EMULATOR",
                color = Color.Cyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Resolución 4:3 PSX Original",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}
