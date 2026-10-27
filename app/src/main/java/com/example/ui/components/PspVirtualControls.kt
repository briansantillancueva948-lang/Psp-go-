package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmulatorConfig
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

interface PspControllerListener {
    fun onDpadChange(up: Boolean, down: Boolean, left: Boolean, right: Boolean)
    fun onAnalogMove(x: Float, y: Float)
    fun onButtonChange(triangle: Boolean, circle: Boolean, cross: Boolean, square: Boolean)
    fun onShoulderChange(l: Boolean, r: Boolean)
    fun onStart()
    fun onSelect()
    fun onHome()
    fun onTurboToggle()
}

@Composable
fun PspVirtualControls(
    config: EmulatorConfig,
    listener: PspControllerListener,
    modifier: Modifier = Modifier
) {
    if (!config.showTouchControls) return

    val baseAlpha = config.buttonOpacity.coerceIn(0.2f, 1.0f)
    val scale = config.buttonScale

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(baseAlpha)
            .testTag("ppsspp_virtual_controls")
    ) {
        // 1. TOP CENTER: PAUSE MENU BUTTON (☰) - EXACTLY AS IN PPSSPP SCREENSHOT
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .size(38.dp)
                .clip(CircleShape)
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { listener.onHome() })
                }
                .testTag("ppsspp_pause_btn"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Pausa / Menú",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // 2. SHOULDER TRIGGERS (L and R) - AUTHENTIC PPSSPP OUTLINE
        // L Shoulder (Upper Left Edge)
        PpssppShoulderTrigger(
            isLeft = true,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = (-55).dp * scale)
                .testTag("shoulder_l"),
            onPressChange = { pressed -> listener.onShoulderChange(l = pressed, r = false) }
        )

        // R Shoulder (Upper Right Edge)
        PpssppShoulderTrigger(
            isLeft = false,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = (-55).dp * scale)
                .testTag("shoulder_r"),
            onPressChange = { pressed -> listener.onShoulderChange(l = false, r = pressed) }
        )

        // 3. LEFT CLUSTER: D-PAD & ANALOG NUB
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // PPSSPP Official D-Pad (Arrows with borders)
                PpssppOfficialDPad(
                    scale = scale,
                    onDirectionChange = { up, down, left, right ->
                        listener.onDpadChange(up, down, left, right)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // PPSSPP Official Concentric Analog Stick
                PpssppOfficialAnalogStick(
                    scale = scale,
                    deadzone = config.analogDeadzone,
                    sensitivity = config.analogSensitivity,
                    onMove = { x, y -> listener.onAnalogMove(x, y) }
                )
            }
        }

        // 4. RIGHT CLUSTER: ACTION BUTTONS (△ ○ ✕ □) - AUTHENTIC PPSSPP HOLLOW CIRCLES
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 26.dp)
        ) {
            PpssppActionSymbolsCluster(
                scale = scale,
                onButtonChange = { tri, circ, crs, sq ->
                    listener.onButtonChange(triangle = tri, circle = circ, cross = crs, square = sq)
                }
            )
        }

        // 5. BOTTOM CENTER ROW: [ ▷▷ ] [ SELECT ] [ START ]
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Turbo Fast-Forward [ ▷▷ ]
            PpssppPillButton(label = "▷▷", tag = "ppsspp_turbo_btn") {
                listener.onTurboToggle()
            }
            // SELECT
            PpssppPillButton(label = "SELECT", tag = "ppsspp_select_btn") {
                listener.onSelect()
            }
            // START
            PpssppPillButton(label = "START", tag = "ppsspp_start_btn") {
                listener.onStart()
            }
        }
    }
}

/**
 * Authentic PPSSPP curved shoulder trigger outline (L / R)
 */
@Composable
fun PpssppShoulderTrigger(
    isLeft: Boolean,
    modifier: Modifier = Modifier,
    onPressChange: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(width = 84.dp, height = 38.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onPressChange(true)
                        tryAwaitRelease()
                        pressed = false
                        onPressChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = Path()

            if (isLeft) {
                // Curved trapezoid pointing left
                path.moveTo(w, 2f)
                path.lineTo(w * 0.22f, 2f)
                path.lineTo(2f, h * 0.55f)
                path.lineTo(2f, h - 2f)
                path.lineTo(w, h - 2f)
                path.close()
            } else {
                // Curved trapezoid pointing right
                path.moveTo(0f, 2f)
                path.lineTo(w * 0.78f, 2f)
                path.lineTo(w - 2f, h * 0.55f)
                path.lineTo(w - 2f, h - 2f)
                path.lineTo(0f, h - 2f)
                path.close()
            }

            // Fill when pressed
            if (pressed) {
                drawPath(path, color = Color.White.copy(alpha = 0.35f))
            }
            // Border stroke
            drawPath(path, color = Color.White.copy(alpha = 0.9f), style = Stroke(width = 2f))
        }

        Text(
            text = if (isLeft) "L" else "R",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

/**
 * PPSSPP Official D-Pad matching screenshot (Hollow directional arrows inside bordered boxes)
 */
@Composable
fun PpssppOfficialDPad(
    scale: Float,
    onDirectionChange: (Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    var up by remember { mutableStateOf(false) }
    var down by remember { mutableStateOf(false) }
    var left by remember { mutableStateOf(false) }
    var right by remember { mutableStateOf(false) }

    val padSize = 136.dp * scale
    val btnSize = 44.dp * scale

    Box(
        modifier = Modifier
            .size(padSize)
            .testTag("ppsspp_dpad")
    ) {
        // UP
        PpssppDpadArrowButton(
            direction = "UP",
            isPressed = up,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(btnSize)
                .testTag("dpad_up")
        ) { p ->
            up = p; onDirectionChange(up, down, left, right)
        }

        // DOWN
        PpssppDpadArrowButton(
            direction = "DOWN",
            isPressed = down,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(btnSize)
                .testTag("dpad_down")
        ) { p ->
            down = p; onDirectionChange(up, down, left, right)
        }

        // LEFT
        PpssppDpadArrowButton(
            direction = "LEFT",
            isPressed = left,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(btnSize)
                .testTag("dpad_left")
        ) { p ->
            left = p; onDirectionChange(up, down, left, right)
        }

        // RIGHT
        PpssppDpadArrowButton(
            direction = "RIGHT",
            isPressed = right,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(btnSize)
                .testTag("dpad_right")
        ) { p ->
            right = p; onDirectionChange(up, down, left, right)
        }
    }
}

@Composable
fun PpssppDpadArrowButton(
    direction: String,
    isPressed: Boolean,
    modifier: Modifier = Modifier,
    onPressChange: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Outer box border
            drawRoundRect(
                color = Color.White.copy(alpha = if (isPressed) 0.5f else 0.85f),
                topLeft = Offset(2f, 2f),
                size = Size(w - 4f, h - 4f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 1.8f)
            )

            if (isPressed) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(2f, 2f),
                    size = Size(w - 4f, h - 4f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // Inner hollow arrow
            val arrowPath = Path()
            val cx = w / 2
            val cy = h / 2
            val arrowSize = 9f

            when (direction) {
                "UP" -> {
                    arrowPath.moveTo(cx, cy - arrowSize)
                    arrowPath.lineTo(cx + arrowSize, cy + arrowSize * 0.6f)
                    arrowPath.lineTo(cx - arrowSize, cy + arrowSize * 0.6f)
                    arrowPath.close()
                }
                "DOWN" -> {
                    arrowPath.moveTo(cx, cy + arrowSize)
                    arrowPath.lineTo(cx + arrowSize, cy - arrowSize * 0.6f)
                    arrowPath.lineTo(cx - arrowSize, cy - arrowSize * 0.6f)
                    arrowPath.close()
                }
                "LEFT" -> {
                    arrowPath.moveTo(cx - arrowSize, cy)
                    arrowPath.lineTo(cx + arrowSize * 0.6f, cy - arrowSize)
                    arrowPath.lineTo(cx + arrowSize * 0.6f, cy + arrowSize)
                    arrowPath.close()
                }
                "RIGHT" -> {
                    arrowPath.moveTo(cx + arrowSize, cy)
                    arrowPath.lineTo(cx - arrowSize * 0.6f, cy - arrowSize)
                    arrowPath.lineTo(cx - arrowSize * 0.6f, cy + arrowSize)
                    arrowPath.close()
                }
            }

            drawPath(
                path = arrowPath,
                color = Color.White,
                style = Stroke(width = 1.8f)
            )
        }
    }
}

/**
 * Concentric circles PPSSPP analog stick matching screenshot
 */
@Composable
fun PpssppOfficialAnalogStick(
    scale: Float,
    deadzone: Float,
    sensitivity: Float,
    onMove: (Float, Float) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val maxRadius = 38f * scale

    Box(
        modifier = Modifier
            .size(76.dp * scale)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {},
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = offsetX + dragAmount.x
                        val newY = offsetY + dragAmount.y
                        val dist = sqrt(newX * newX + newY * newY)
                        if (dist > maxRadius) {
                            val angle = atan2(newY, newX)
                            offsetX = (cos(angle) * maxRadius).toFloat()
                            offsetY = (sin(angle) * maxRadius).toFloat()
                        } else {
                            offsetX = newX
                            offsetY = newY
                        }

                        val normDist = dist / maxRadius
                        if (normDist < deadzone) {
                            onMove(0f, 0f)
                        } else {
                            val normFactor = ((normDist - deadzone) / (1f - deadzone)) * sensitivity
                            val angle = atan2(offsetY, offsetX)
                            val finalX = (cos(angle) * normFactor).toFloat().coerceIn(-1f, 1f)
                            val finalY = (sin(angle) * normFactor).toFloat().coerceIn(-1f, 1f)
                            onMove(finalX, finalY)
                        }
                    }
                )
            }
            .testTag("ppsspp_analog_stick"),
        contentAlignment = Alignment.Center
    ) {
        // Outer boundary ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = size.width * 0.48f,
                style = Stroke(width = 2f)
            )
        }

        // Inner floating concentric puck
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(44.dp * scale),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = size.width * 0.48f,
                    style = Stroke(width = 2f)
                )
                // Center concentric dot
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = size.width * 0.18f,
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }
}

/**
 * Right Action buttons: Hollow circles with clean geometric symbols (△ ○ ✕ □)
 */
@Composable
fun PpssppActionSymbolsCluster(
    scale: Float,
    onButtonChange: (Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    var triangle by remember { mutableStateOf(false) }
    var circle by remember { mutableStateOf(false) }
    var cross by remember { mutableStateOf(false) }
    var square by remember { mutableStateOf(false) }

    val clusterSize = 142.dp * scale
    val btnSize = 46.dp * scale

    Box(
        modifier = Modifier
            .size(clusterSize)
            .testTag("ppsspp_action_cluster")
    ) {
        // △ TRIANGLE (Top)
        PpssppHollowSymbolButton(
            symbol = "△",
            isPressed = triangle,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(btnSize)
                .testTag("button_triangle")
        ) { p ->
            triangle = p; onButtonChange(triangle, circle, cross, square)
        }

        // ○ CIRCLE (Right)
        PpssppHollowSymbolButton(
            symbol = "○",
            isPressed = circle,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(btnSize)
                .testTag("button_circle")
        ) { p ->
            circle = p; onButtonChange(triangle, circle, cross, square)
        }

        // ✕ CROSS (Bottom)
        PpssppHollowSymbolButton(
            symbol = "✕",
            isPressed = cross,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(btnSize)
                .testTag("button_cross")
        ) { p ->
            cross = p; onButtonChange(triangle, circle, cross, square)
        }

        // □ SQUARE (Left)
        PpssppHollowSymbolButton(
            symbol = "□",
            isPressed = square,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(btnSize)
                .testTag("button_square")
        ) { p ->
            square = p; onButtonChange(triangle, circle, cross, square)
        }
    }
}

@Composable
fun PpssppHollowSymbolButton(
    symbol: String,
    isPressed: Boolean,
    modifier: Modifier = Modifier,
    onPressChange: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.width * 0.48f
            // Ring stroke
            drawCircle(
                color = Color.White.copy(alpha = if (isPressed) 0.5f else 0.85f),
                radius = r,
                style = Stroke(width = 2f)
            )
            // Fill if pressed
            if (isPressed) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = r
                )
            }
        }

        Text(
            text = symbol,
            color = Color.White,
            fontWeight = FontWeight.Normal,
            fontSize = 21.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

/**
 * Rectangular bordered pill button [ ▷▷ ] [ SELECT ] [ START ]
 */
@Composable
fun PpssppPillButton(
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (pressed) Color.White.copy(alpha = 0.35f) else Color.Transparent)
            .border(1.5.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(3.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onClick()
                        tryAwaitRelease()
                        pressed = false
                    }
                )
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp
        )
    }
}
