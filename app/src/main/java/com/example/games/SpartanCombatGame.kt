package com.example.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PspSoundEngine
import com.example.model.EmulatorConfig
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class SpartanEnemy(
    var x: Float,
    var y: Float,
    var hp: Float = 100f,
    val maxHp: Float = 100f,
    var isDead: Boolean = false
)

data class BloodParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float = 1.0f
)

@Composable
fun SpartanCombatGame(
    config: EmulatorConfig,
    input: GameInputState,
    soundEngine: PspSoundEngine,
    modifier: Modifier = Modifier
) {
    var heroX by remember { mutableFloatStateOf(200f) }
    var heroY by remember { mutableFloatStateOf(400f) }
    var heroHp by remember { mutableFloatStateOf(100f) }
    var rageMeter by remember { mutableFloatStateOf(85f) }
    var comboCount by remember { mutableIntStateOf(12) }
    var comboTimer by remember { mutableFloatStateOf(3f) }
    var attackAnimTimer by remember { mutableFloatStateOf(0f) }
    var attackType by remember { mutableStateOf("NONE") } // LIGHT, HEAVY, RAGE
    var heroFacingRight by remember { mutableStateOf(true) }

    val enemies = remember {
        mutableStateListOf(
            SpartanEnemy(450f, 380f),
            SpartanEnemy(650f, 420f),
            SpartanEnemy(300f, 450f)
        )
    }

    val particles = remember { mutableStateListOf<BloodParticle>() }

    // Game Loop
    LaunchedEffect(input.turbo) {
        val frameDelay = if (input.turbo) 8L else 16L
        val rand = Random(123)

        while (true) {
            val dt = 0.016f * (if (input.turbo) 2f else 1f)

            // Hero Movement
            val moveX = when {
                input.left -> -1f
                input.right -> 1f
                else -> input.analogX
            }
            val moveY = when {
                input.up -> -1f
                input.down -> 1f
                else -> input.analogY
            }

            if (moveX > 0.1f) heroFacingRight = true
            if (moveX < -0.1f) heroFacingRight = false

            heroX = (heroX + moveX * 180f * dt).coerceIn(80f, 900f)
            heroY = (heroY + moveY * 140f * dt).coerceIn(240f, 520f)

            // Attack triggering
            if (input.square && attackAnimTimer <= 0f) {
                // Light Slash
                attackAnimTimer = 0.35f
                attackType = "LIGHT"
                soundEngine.playSlash()
                soundEngine.vibrate(20, 160)
            } else if (input.triangle && attackAnimTimer <= 0f) {
                // Heavy Slam
                attackAnimTimer = 0.5f
                attackType = "HEAVY"
                soundEngine.playSlash()
                soundEngine.vibrate(35, 230)
            } else if (input.circle && rageMeter >= 30f && attackAnimTimer <= 0f) {
                // Spartan Rage
                attackAnimTimer = 0.8f
                attackType = "RAGE"
                rageMeter = (rageMeter - 30f).coerceAtLeast(0f)
                soundEngine.playBoost()
                soundEngine.vibrate(50, 255)
            }

            if (attackAnimTimer > 0f) {
                attackAnimTimer -= dt
                // Check hit collision against enemies
                val hitRange = if (attackType == "RAGE") 250f else 120f
                val damage = when (attackType) {
                    "LIGHT" -> 25f
                    "HEAVY" -> 55f
                    "RAGE" -> 110f
                    else -> 0f
                }

                enemies.forEach { enemy ->
                    if (!enemy.isDead) {
                        val dx = enemy.x - heroX
                        val dy = enemy.y - heroY
                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                        if (dist < hitRange) {
                            enemy.hp -= damage * dt * 8f
                            comboCount++
                            comboTimer = 2.5f
                            rageMeter = (rageMeter + 2.5f).coerceAtMost(100f)

                            // Spawn blood sparks
                            repeat(3) {
                                particles.add(
                                    BloodParticle(
                                        x = enemy.x,
                                        y = enemy.y,
                                        vx = (rand.nextFloat() - 0.5f) * 300f,
                                        vy = (rand.nextFloat() - 0.7f) * 250f
                                    )
                                )
                            }

                            if (enemy.hp <= 0f) {
                                enemy.isDead = true
                            }
                        }
                    }
                }
            }

            // Combo timer decay
            if (comboTimer > 0f) {
                comboTimer -= dt
                if (comboTimer <= 0f) comboCount = 0
            }

            // Enemy AI & Respawn
            enemies.forEach { enemy ->
                if (!enemy.isDead) {
                    val dx = heroX - enemy.x
                    val dy = heroY - enemy.y
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    if (dist > 35f) {
                        enemy.x += (dx / dist) * 65f * dt
                        enemy.y += (dy / dist) * 45f * dt
                    }
                } else {
                    // Respawn elsewhere
                    if (rand.nextFloat() < 0.02f) {
                        enemy.hp = 100f
                        enemy.isDead = false
                        enemy.x = if (rand.nextBoolean()) 100f else 850f
                        enemy.y = 280f + rand.nextFloat() * 200f
                    }
                }
            }

            // Update particles
            val iter = particles.iterator()
            while (iter.hasNext()) {
                val p = iter.next()
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.vy += 450f * dt // gravity
                p.life -= dt * 2f
                if (p.life <= 0f) iter.remove()
            }

            delay(frameDelay)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0B10))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Colosseum / Ancient Temple Background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF240E0A), Color(0xFF4A180E), Color(0xFF1F0C08)),
                    startY = 0f,
                    endY = h * 0.45f
                ),
                size = Size(w, h * 0.45f)
            )

            // Pillars
            val pillarW = 45f
            for (p in 0..7) {
                val px = p * (w / 7f)
                drawRect(
                    color = Color(0xFF381B15),
                    topLeft = Offset(px, 30f),
                    size = Size(pillarW, h * 0.45f - 30f)
                )
            }

            // Stone Arena Floor
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2C1914), Color(0xFF140B08)),
                    startY = h * 0.45f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.45f),
                size = Size(w, h * 0.55f)
            )

            // Draw Enemies (Minotaurs / Skeletons)
            enemies.forEach { enemy ->
                if (!enemy.isDead) {
                    val ex = enemy.x
                    val ey = enemy.y

                    // Enemy shadow
                    drawOval(
                        color = Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(ex - 22f, ey + 18f),
                        size = Size(44f, 14f)
                    )

                    // Enemy body (Skeleton / Demon)
                    drawCircle(
                        color = Color(0xFF78909C),
                        radius = 18f,
                        center = Offset(ex, ey)
                    )
                    // Glowing evil red eyes
                    drawCircle(color = Color.Red, radius = 2.5f, center = Offset(ex - 4f, ey - 3f))
                    drawCircle(color = Color.Red, radius = 2.5f, center = Offset(ex + 4f, ey - 3f))

                    // Enemy HP Bar
                    val hpRatio = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(ex - 24f, ey - 32f),
                        size = Size(48f, 5f)
                    )
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(ex - 24f, ey - 32f),
                        size = Size(48f * hpRatio, 5f)
                    )
                }
            }

            // Draw Hero (Kratos / Spartan Warrior)
            val hx = heroX
            val hy = heroY

            // Hero shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.6f),
                topLeft = Offset(hx - 26f, hy + 22f),
                size = Size(52f, 16f)
            )

            // Hero Body
            drawCircle(
                color = Color(0xFFD7CCC8), // Pale Spartan skin tone
                radius = 22f,
                center = Offset(hx, hy)
            )
            // Red Spartan War Paint Stripe
            drawLine(
                color = Color(0xFFD50000),
                start = Offset(hx - 10f, hy - 18f),
                end = Offset(hx + 8f, hy + 18f),
                strokeWidth = 5f
            )

            // Glowing Chains & Blades of Chaos
            val bladeColor = if (attackAnimTimer > 0f) Color(0xFFFF6D00) else Color(0xFFFFAB00)
            val arcDir = if (heroFacingRight) 1f else -1f

            if (attackAnimTimer > 0f) {
                // Dynamic fiery attack trail arcs
                val arcRadius = if (attackType == "RAGE") 180f else 90f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF3D00).copy(alpha = 0.8f), Color.Transparent),
                        center = Offset(hx, hy),
                        radius = arcRadius
                    ),
                    radius = arcRadius,
                    center = Offset(hx, hy)
                )

                // Fiery chained blade swoosh
                val bladeTipX = hx + (cos(attackAnimTimer * 20f) * arcRadius * arcDir).toFloat()
                val bladeTipY = hy + (sin(attackAnimTimer * 20f) * arcRadius).toFloat()
                drawLine(
                    color = bladeColor,
                    start = Offset(hx, hy),
                    end = Offset(bladeTipX, bladeTipY),
                    strokeWidth = 6f
                )
            } else {
                // Resting blades on back
                drawRoundRect(
                    color = Color(0xFFFF9100),
                    topLeft = Offset(hx - 14f, hy - 24f),
                    size = Size(8f, 26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }

            // Draw Blood / Fire Particles
            particles.forEach { p ->
                drawCircle(
                    color = Color(0xFFFF1744).copy(alpha = p.life.coerceIn(0f, 1f)),
                    radius = 3.5f,
                    center = Offset(p.x, p.y)
                )
            }

            // Apply Post-Processing Shaders
            applyShaderEffect(config.shader, size)
        }

        // HUD: Health Bar & Spartan Rage Meter
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            // Health Bar (Green)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SALUD",
                    color = Color.Green,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(width = 160.dp, height = 12.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(1.dp)
                            .background(Color(0xFF00E676))
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Rage Meter (Red / Orange)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "FURIA",
                    color = Color(0xFFFF3D00),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(width = 160.dp, height = 12.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(1.dp)
                            .background(Color(0xFFFF3D00))
                    )
                }
            }
        }

        // COMBO COUNTER
        if (comboCount > 0) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$comboCount HITS",
                    color = Color(0xFFFFD600),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = when {
                        comboCount > 30 -> "¡FURIA DIVINA!"
                        comboCount > 15 -> "¡BRUTAL!"
                        else -> "COMBO ESPARTANO"
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
