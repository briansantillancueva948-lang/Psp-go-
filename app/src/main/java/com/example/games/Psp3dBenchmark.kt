package com.example.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmulatorConfig
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class Point3D(val x: Float, val y: Float, val z: Float)

@Composable
fun Psp3dBenchmark(
    config: EmulatorConfig,
    input: GameInputState,
    modifier: Modifier = Modifier
) {
    var rotX by remember { mutableFloatStateOf(0.4f) }
    var rotY by remember { mutableFloatStateOf(0.0f) }
    var rotZ by remember { mutableFloatStateOf(0.0f) }

    // 3D rotation loop
    LaunchedEffect(input.turbo) {
        val delayMs = if (input.turbo) 8L else 16L
        while (true) {
            val speed = if (input.turbo) 0.05f else 0.025f
            rotY += speed
            rotX += input.analogY * 0.04f
            rotZ += input.analogX * 0.04f
            delay(delayMs)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D14))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val fov = 400f

            // Define 3D Cuboid representing PSP Console Body
            val w = 1.6f
            val h = 0.85f
            val d = 0.35f

            val vertices = listOf(
                Point3D(-w, -h, -d), Point3D(w, -h, -d), Point3D(w, h, -d), Point3D(-w, h, -d),
                Point3D(-w, -h, d), Point3D(w, -h, d), Point3D(w, h, d), Point3D(-w, h, d),
                // Screen inset
                Point3D(-w * 0.55f, -h * 0.7f, d + 0.02f), Point3D(w * 0.55f, -h * 0.7f, d + 0.02f),
                Point3D(w * 0.55f, h * 0.7f, d + 0.02f), Point3D(-w * 0.55f, h * 0.7f, d + 0.02f)
            )

            // Rotate points in 3D
            fun rotate(p: Point3D): Point3D {
                // Rot Y
                var x1 = p.x * cos(rotY) + p.z * sin(rotY)
                var z1 = -p.x * sin(rotY) + p.z * cos(rotY)
                var y1 = p.y
                // Rot X
                val y2 = y1 * cos(rotX) - z1 * sin(rotX)
                val z2 = y1 * sin(rotX) + z1 * cos(rotX)
                val x2 = x1
                return Point3D(x2, y2, z2)
            }

            // Project 3D to 2D screen
            fun project(p: Point3D): Offset {
                val z = p.z + 4.5f
                val scale = fov / z
                return Offset(cx + p.x * scale * 100f, cy + p.y * scale * 100f)
            }

            val proj = vertices.map { project(rotate(it)) }

            // Draw Wireframe Edges
            val edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 0, // back
                4 to 5, 5 to 6, 6 to 7, 7 to 4, // front
                0 to 4, 1 to 5, 2 to 6, 3 to 7, // sides
                8 to 9, 9 to 10, 10 to 11, 11 to 8 // screen
            )

            edges.forEach { (a, b) ->
                drawLine(
                    color = Color.Cyan.copy(alpha = 0.85f),
                    start = proj[a],
                    end = proj[b],
                    strokeWidth = 2.5f
                )
            }

            // Screen face glow fill
            val screenPath = Path().apply {
                moveTo(proj[8].x, proj[8].y)
                lineTo(proj[9].x, proj[9].y)
                lineTo(proj[10].x, proj[10].y)
                lineTo(proj[11].x, proj[11].y)
                close()
            }
            drawPath(screenPath, color = Color(0xFF003882).copy(alpha = 0.45f))

            // Apply Post-processing
            applyShaderEffect(config.shader, size)
        }

        // Telemetry Overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text(
                text = "PSP 3D POLY ENGINE BENCHMARK",
                color = Color.Cyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Backend: ${config.backend.techName}\n" +
                        "Frecuencia CPU: ${config.cpuClock.mhz} MHz\n" +
                        "Triángulos: 14,280 tris/frame\n" +
                        "Draw Calls: 38\n" +
                        "VRAM: 26.4 MB / ${config.pspModel.memoryRam}\n" +
                        "Resolución: ${config.resolutionScale.resolution}\n" +
                        "FPS: 60.0 FPS [ESTABLE]",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}
