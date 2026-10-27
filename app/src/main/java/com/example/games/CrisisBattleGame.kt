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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun CrisisBattleGame(
    config: EmulatorConfig,
    input: GameInputState,
    soundEngine: PspSoundEngine,
    modifier: Modifier = Modifier
) {
    var playerHp by remember { mutableIntStateOf(4850) }
    var playerMp by remember { mutableIntStateOf(320) }
    var playerAp by remember { mutableIntStateOf(280) }
    var bossHp by remember { mutableIntStateOf(18200) }
    val maxBossHp = 25000

    var dmwReel1 by remember { mutableIntStateOf(7) }
    var dmwReel2 by remember { mutableIntStateOf(7) }
    var dmwReel3 by remember { mutableIntStateOf(7) }
    var limitBreakActive by remember { mutableStateOf(false) }
    var battleStatus by remember { mutableStateOf("BATALLA EN CURSO") }
    var animTimer by remember { mutableFloatStateOf(0f) }

    // DMW Slot Machine Simulation
    LaunchedEffect(Unit) {
        while (true) {
            delay(1200)
            dmwReel1 = (1..7).random()
            dmwReel2 = (1..7).random()
            dmwReel3 = (1..7).random()
            if (dmwReel1 == 7 && dmwReel2 == 7 && dmwReel3 == 7) {
                limitBreakActive = true
                battleStatus = "¡OCTASLASH LIMIT BREAK LISTO!"
                soundEngine.playConfirm()
            }
        }
    }

    // Input actions
    LaunchedEffect(input.cross, input.triangle, input.square, input.circle) {
        if (input.cross) {
            // Buster Sword Attack
            bossHp = (bossHp - 650).coerceAtLeast(0)
            soundEngine.playSlash()
            soundEngine.vibrate(20, 180)
            battleStatus = "CORTE BUSTER DE ZACK: 650 DMG"
        } else if (input.triangle && playerMp >= 25) {
            // Firaga Magic
            playerMp -= 25
            bossHp = (bossHp - 1450).coerceAtLeast(0)
            soundEngine.playBoost()
            soundEngine.vibrate(35, 220)
            battleStatus = "¡PIRO++ / FIRAGA!: 1450 MAG DMG"
        } else if (input.square && limitBreakActive) {
            // Limit Break
            bossHp = (bossHp - 5200).coerceAtLeast(0)
            limitBreakActive = false
            soundEngine.playBoost()
            soundEngine.vibrate(50, 255)
            battleStatus = "¡OCTASLASH! 8 GOLPES CRÍTICOS: 5200 DMG"
        } else if (input.circle && playerMp >= 15) {
            // Curaga
            playerMp -= 15
            playerHp = (playerHp + 1200).coerceAtMost(5000)
            soundEngine.playConfirm()
            battleStatus = "CURA++ RESTAURA 1200 HP"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C101C))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Mako Reactor Arena Floor
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF06181E), Color(0xFF0A2B28), Color(0xFF031012)),
                    startY = 0f,
                    endY = h
                )
            )

            // Glowing Mako Energy Streams
            for (i in 0..4) {
                val lineX = (i + 1) * (w / 6f)
                drawLine(
                    color = Color(0xFF00E676).copy(alpha = 0.35f),
                    start = Offset(lineX, 0f),
                    end = Offset(lineX, h),
                    strokeWidth = 3f
                )
            }

            // Boss: Behemoth / Genesis silhouette
            val bx = w * 0.72f
            val by = h * 0.45f
            drawCircle(
                color = Color(0xFFB71C1C),
                radius = 55f,
                center = Offset(bx, by)
            )
            drawCircle(
                color = Color.Yellow,
                radius = 5f,
                center = Offset(bx - 15f, by - 10f)
            )

            // Player Zack Fair (SOLDIER 1st Class)
            val px = w * 0.28f
            val py = h * 0.58f
            drawCircle(
                color = Color(0xFF1565C0),
                radius = 32f,
                center = Offset(px, py)
            )
            // Buster sword blade
            drawLine(
                color = Color.LightGray,
                start = Offset(px - 10f, py - 45f),
                end = Offset(px + 10f, py + 15f),
                strokeWidth = 10f
            )

            // Post-processing
            applyShaderEffect(config.shader, size)
        }

        // Top Left: DMW (Digital Mind Wave) Slots
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .border(1.5.dp, if (limitBreakActive) Color(0xFFFFD600) else Color.Cyan, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "D.M.W SLOTS",
                    color = if (limitBreakActive) Color(0xFFFFD600) else Color.Cyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DmwSlotPill(number = dmwReel1)
                    DmwSlotPill(number = dmwReel2)
                    DmwSlotPill(number = dmwReel3)
                }
            }
        }

        // Top Right: Boss Health Bar
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "GENESIS AVATAR [JEFE]",
                color = Color(0xFFFF5252),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(width = 180.dp, height = 12.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                val bossRatio = (bossHp.toFloat() / maxBossHp).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp)
                        .background(Color(0xFFFF1744))
                )
            }
            Text(
                text = "$bossHp / $maxBossHp HP",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }

        // Bottom Left: Zack's HP / MP / AP Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = "ZACK FAIR • SOLDIER 1ST CLASS",
                color = Color.Cyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "HP: $playerHp / 5000  |  MP: $playerMp / 350  |  AP: $playerAp",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = battleStatus,
                color = Color(0xFFFFD600),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DmwSlotPill(number: Int) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E2838))
            .border(1.dp, Color.Cyan, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$number",
            color = if (number == 7) Color(0xFFFFD600) else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
        )
    }
}
