package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.XmbWaveColor
import kotlin.math.sin
import kotlin.random.Random

data class WaveParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val alpha: Float,
    val speed: Float
)

@Composable
fun PspWaveBackground(
    waveColor: XmbWaveColor,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "psp_wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val particles = remember {
        val rand = Random(42)
        List(40) {
            WaveParticle(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat(),
                radius = rand.nextFloat() * 2.5f + 1f,
                alpha = rand.nextFloat() * 0.6f + 0.2f,
                speed = rand.nextFloat() * 0.3f + 0.1f
            )
        }
    }

    val primaryColor = Color(waveColor.primaryHex)
    val secondaryColor = Color(waveColor.secondaryHex)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background rich gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.95f),
                    primaryColor.copy(alpha = 0.65f),
                    Color(0xFF06090E)
                )
            )
        )

        // Draw floating sparkle particles
        particles.forEach { p ->
            val py = ((p.yRatio + phase * p.speed * 0.08f) % 1f) * height
            val px = p.xRatio * width
            drawCircle(
                color = Color.White.copy(alpha = p.alpha * (0.5f + 0.5f * sin(phase + px).toFloat())),
                radius = p.radius,
                center = Offset(px, py)
            )
        }

        // Multiple overlapping ribbon waves simulating PSP XMB silk waves
        val waveCount = 5
        for (w in 0 until waveCount) {
            val waveOffset = w * 0.45f
            val path = Path()
            val midY = height * (0.42f + w * 0.04f)
            val waveColorTint = secondaryColor.copy(alpha = (0.35f - w * 0.05f).coerceAtLeast(0.12f))

            path.moveTo(0f, midY)
            var x = 0f
            val step = width / 50f
            while (x <= width) {
                val relX = x / width
                val y1 = sin(relX * 4.5f + phase + waveOffset) * (height * 0.12f)
                val y2 = sin(relX * 8.0f - phase * 0.7f + waveOffset) * (height * 0.04f)
                val y = midY + y1 + y2
                path.lineTo(x, y.toFloat())
                x += step
            }

            // Draw flowing stroke
            drawPath(
                path = path,
                color = waveColorTint,
                style = Stroke(width = (3.5f - w * 0.5f).coerceAtLeast(1.5f))
            )

            // Ribbon fill underneath wave 0
            if (w == 0) {
                val fillPath = Path()
                fillPath.addPath(path)
                fillPath.lineTo(width, height)
                fillPath.lineTo(0f, height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        startY = midY - height * 0.1f,
                        endY = height
                    )
                )
            }
        }
    }
}
