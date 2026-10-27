package com.example.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PspSoundEngine
import com.example.model.EmulatorConfig
import kotlinx.coroutines.delay
import kotlin.random.Random

data class TekkenSpark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float = 1.0f
)

@Composable
fun Tekken3Game(
    config: EmulatorConfig,
    input: GameInputState,
    soundEngine: PspSoundEngine,
    modifier: Modifier = Modifier
) {
    var p1Hp by remember { mutableFloatStateOf(100f) }
    var p2Hp by remember { mutableFloatStateOf(82f) }
    var roundTimer by remember { mutableIntStateOf(52) }
    var p1X by remember { mutableFloatStateOf(280f) }
    var p2X by remember { mutableFloatStateOf(580f) }
    var attackTimer by remember { mutableFloatStateOf(0f) }
    var hitSparksActive by remember { mutableStateOf(false) }
    var hitAnnounce by remember { mutableStateOf("ROUND 1 • ¡FIGHT!") }
    val sparks = remember { mutableStateListOf<TekkenSpark>() }

    // 60 FPS Game Loop
    LaunchedEffect(input.turbo) {
        val frameDelay = if (input.turbo) 8L else 16L
        val rand = Random(456)

        while (true) {
            val dt = 0.016f * (if (input.turbo) 2f else 1f)

            // P1 Movement
            val moveX = when {
                input.left -> -1f
                input.right -> 1f
                else -> input.analogX
            }
            p1X = (p1X + moveX * 160f * dt).coerceIn(80f, p2X - 45f)

            // Attacks:
            // □ = Left Punch (1)
            // △ = Right Punch (2)
            // ✕ = Left Kick (3)
            // ○ = Right Kick (4)
            if (input.square && attackTimer <= 0f) {
                attackTimer = 0.25f
                soundEngine.playSlash()
                soundEngine.vibrate(18, 140)
                hitAnnounce = "JIN: LEFT JAB [1]"
                if (p2X - p1X < 90f) {
                    p2Hp = (p2Hp - 6f).coerceAtLeast(0f)
                    spawnSparks(sparks, p2X, 360f, rand)
                }
            } else if (input.triangle && attackTimer <= 0f) {
                // Electric Wind God Fist!
                attackTimer = 0.4f
                soundEngine.playBoost()
                soundEngine.vibrate(35, 230)
                hitAnnounce = "¡ELECTRIC GOD FIST! [2]"
                if (p2X - p1X < 110f) {
                    p2Hp = (p2Hp - 16f).coerceAtLeast(0f)
                    spawnSparks(sparks, p2X, 340f, rand, count = 12)
                }
            } else if (input.circle && attackTimer <= 0f) {
                // Roundhouse Kick
                attackTimer = 0.35f
                soundEngine.playSlash()
                soundEngine.vibrate(25, 180)
                hitAnnounce = "ROUNDHOUSE KICK [4]"
                if (p2X - p1X < 100f) {
                    p2Hp = (p2Hp - 12f).coerceAtLeast(0f)
                    spawnSparks(sparks, p2X, 350f, rand)
                }
            }

            if (attackTimer > 0f) attackTimer -= dt

            // P2 AI behavior (Heihachi)
            if (p2Hp > 0f) {
                val dist = p2X - p1X
                if (dist > 85f) {
                    p2X -= 55f * dt
                }
                if (rand.nextFloat() < 0.03f && attackTimer <= 0f) {
                    p1Hp = (p1Hp - 4f).coerceAtLeast(0f)
                    soundEngine.vibrate(15, 100)
                    spawnSparks(sparks, p1X, 360f, rand)
                }
            }

            // Update sparks
            val iter = sparks.iterator()
            while (iter.hasNext()) {
                val s = iter.next()
                s.x += s.vx * dt
                s.y += s.vy * dt
                s.life -= dt * 2.5f
                if (s.life <= 0f) iter.remove()
            }

            delay(frameDelay)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B12))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Dojo / Mishima Temple Background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A0A08), Color(0xFF3E120A), Color(0xFF140806)),
                    startY = 0f,
                    endY = h * 0.55f
                ),
                size = Size(w, h * 0.55f)
            )

            // Wooden Tatami Dojo Floor
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4E342E), Color(0xFF271A16)),
                    startY = h * 0.55f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.55f),
                size = Size(w, h * 0.45f)
            )

            // P1: Jin Kazama (Black karate pants with flames)
            val jx = p1X
            val jy = h * 0.72f
            drawOval(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(jx - 24f, jy + 26f),
                size = Size(48f, 14f)
            )
            // Torso
            drawRect(
                color = Color(0xFFFFCC80), // Muscular skin
                topLeft = Offset(jx - 16f, jy - 46f),
                size = Size(32f, 36f)
            )
            // Karate Gi Pants with Flame Motif
            drawRect(
                color = Color.Black,
                topLeft = Offset(jx - 14f, jy - 10f),
                size = Size(28f, 34f)
            )
            drawRect(
                color = Color(0xFFFF5722), // Flame embroidery
                topLeft = Offset(jx + 2f, jy),
                size = Size(10f, 20f)
            )
            // Head & Spiky Hair
            drawCircle(color = Color(0xFFFFCC80), radius = 12f, center = Offset(jx, jy - 56f))
            drawCircle(color = Color.Black, radius = 14f, center = Offset(jx - 2f, jy - 64f))

            // Lightning spark on fist if attack
            if (attackTimer > 0f) {
                drawCircle(
                    color = Color.Cyan,
                    radius = 16f,
                    center = Offset(jx + 28f, jy - 30f)
                )
            }

            // P2: Heihachi Mishima (Dark Gi, grey spiked hair)
            val hx = p2X
            val hy = h * 0.72f
            drawOval(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(hx - 24f, hy + 26f),
                size = Size(48f, 14f)
            )
            drawRect(
                color = Color(0xFF37474F),
                topLeft = Offset(hx - 16f, hy - 46f),
                size = Size(32f, 36f)
            )
            drawRect(
                color = Color(0xFF263238),
                topLeft = Offset(hx - 14f, hy - 10f),
                size = Size(28f, 34f)
            )
            drawCircle(color = Color(0xFFFFCC80), radius = 12f, center = Offset(hx, hy - 56f))
            drawCircle(color = Color.LightGray, radius = 14f, center = Offset(hx + 2f, hy - 64f))

            // Hit sparks
            sparks.forEach { s ->
                drawCircle(
                    color = if (s.life > 0.5f) Color(0xFFFFEB3B) else Color(0xFFFF5722),
                    radius = 3.5f,
                    center = Offset(s.x, s.y)
                )
            }

            // Post-processing
            applyShaderEffect(config.shader, size)
        }

        // TOP HUD: Health Bars, Names & Round Timer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // P1 JIN HP BAR
                Column {
                    Text(
                        text = "JIN KAZAMA",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 150.dp, height = 14.dp)
                            .background(Color.Red.copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(1.dp)
                                .background(Color(0xFFFFD600))
                        )
                    }
                }

                // TIMER
                Text(
                    text = "$roundTimer",
                    color = Color.Yellow,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                // P2 HEIHACHI HP BAR
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "HEIHACHI",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 150.dp, height = 14.dp)
                            .background(Color.Red.copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        val p2Ratio = (p2Hp / 100f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(1.dp)
                                .background(Color(0xFFFFD600))
                        )
                    }
                }
            }

            // Move announcer
            Text(
                text = hitAnnounce,
                color = Color.Cyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
            )
        }
    }
}

private fun spawnSparks(sparks: MutableList<TekkenSpark>, x: Float, y: Float, rand: Random, count: Int = 8) {
    repeat(count) {
        sparks.add(
            TekkenSpark(
                x = x,
                y = y,
                vx = (rand.nextFloat() - 0.5f) * 400f,
                vy = (rand.nextFloat() - 0.7f) * 350f
            )
        )
    }
}
