package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PspSoundEngine
import kotlinx.coroutines.delay

@Composable
fun PspBootScreen(
    soundEngine: PspSoundEngine,
    onBootFinished: () -> Unit
) {
    var stage by remember { mutableStateOf(0) } // 0: White intro, 1: Sony text, 2: PSP logo + chime, 3: Fade to XMB
    val logoScale = remember { Animatable(0.85f) }
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        soundEngine.playBootChime()
        stage = 1
        delay(600)
        stage = 2
        logoAlpha.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        delay(1400)
        stage = 3
        logoAlpha.animateTo(0f, tween(500, easing = LinearEasing))
        delay(500)
        onBootFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Allow tapping to skip intro immediately
                onBootFinished()
            }
            .testTag("psp_boot_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Sony Computer Entertainment Presentation
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(logoAlpha.value)
                .scale(logoScale.value)
        ) {
            // PlayStation Four Symbols / PSP Monogram
            Canvas(modifier = Modifier.size(100.dp)) {
                val cx = size.width / 2
                val cy = size.height / 2
                val r = size.width * 0.38f

                // Classic PSP dual ring
                drawCircle(
                    color = Color(0xFF003882),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 5f)
                )

                // Diamond cross inner geometry
                drawCircle(
                    color = Color(0xFF00A2FF),
                    radius = r * 0.65f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sony Computer Entertainment",
                color = Color(0xFF1B232E),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "PlayStation®Portable",
                color = Color(0xFF5B697A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp
            )
        }

        // Skip prompt bottom corner
        Text(
            text = "Toca la pantalla para omitir",
            color = Color(0xFF9EAAB7),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
