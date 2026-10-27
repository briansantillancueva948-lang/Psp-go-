package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PspSoundEngine
import com.example.games.CrashWarpedGame
import com.example.games.CrisisBattleGame
import com.example.games.GameInputState
import com.example.games.Psp3dBenchmark
import com.example.games.RidgeRacerGame
import com.example.games.SpartanCombatGame
import com.example.games.Tekken3Game
import com.example.model.DisplayLayout
import com.example.model.EmulatorConfig
import com.example.model.GameType
import com.example.model.PspGame
import com.example.model.SaveStateSlot
import com.example.ui.components.PspControllerListener
import com.example.ui.components.PspVirtualControls
import kotlinx.coroutines.delay

@Composable
fun PspGameScreen(
    game: PspGame,
    config: EmulatorConfig,
    soundEngine: PspSoundEngine,
    onExitGame: () -> Unit,
    onOpenSettings: (tab: Int) -> Unit
) {
    val inputState = remember { GameInputState() }
    var isPaused by remember { mutableStateOf(false) }
    var currentSlot by remember { mutableIntStateOf(1) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val saveSlots = remember {
        mutableStateListOf(
            SaveStateSlot(1, description = "Ranura 1 (Vacía)"),
            SaveStateSlot(2, description = "Ranura 2 (Vacía)"),
            SaveStateSlot(3, description = "Ranura 3 (Vacía)"),
            SaveStateSlot(4, description = "Ranura 4 (Vacía)"),
            SaveStateSlot(5, description = "Ranura 5 (Vacía)")
        )
    }

    // Dynamic FPS simulation based on settings and turbo
    var currentFps by remember { mutableFloatStateOf(60.0f) }
    LaunchedEffect(inputState.turbo) {
        while (true) {
            delay(500)
            val base = if (inputState.turbo) 120.0f else 60.0f
            currentFps = base - (Math.random() * 0.4f).toFloat()
        }
    }

    // Status message auto-dismiss
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(2000)
            statusMessage = null
        }
    }

    val controllerListener = remember {
        object : PspControllerListener {
            override fun onDpadChange(up: Boolean, down: Boolean, left: Boolean, right: Boolean) {
                inputState.up = up
                inputState.down = down
                inputState.left = left
                inputState.right = right
                if (config.hapticFeedback && (up || down || left || right)) {
                    soundEngine.vibrate(15, 120)
                }
            }

            override fun onAnalogMove(x: Float, y: Float) {
                inputState.analogX = x
                inputState.analogY = y
            }

            override fun onButtonChange(triangle: Boolean, circle: Boolean, cross: Boolean, square: Boolean) {
                inputState.triangle = triangle
                inputState.circle = circle
                inputState.cross = cross
                inputState.square = square
                if (config.hapticFeedback && (triangle || circle || cross || square)) {
                    soundEngine.vibrate(18, 140)
                }
            }

            override fun onShoulderChange(l: Boolean, r: Boolean) {
                inputState.shoulderL = l
                inputState.shoulderR = r
                if (config.hapticFeedback && (l || r)) {
                    soundEngine.vibrate(15, 120)
                }
            }

            override fun onStart() {
                soundEngine.playConfirm()
            }

            override fun onSelect() {
                soundEngine.playClick()
            }

            override fun onHome() {
                soundEngine.playConfirm()
                isPaused = !isPaused
            }

            override fun onTurboToggle() {
                inputState.turbo = !inputState.turbo
                soundEngine.playConfirm()
                soundEngine.vibrate(25, 200)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("psp_game_screen")
    ) {
        // Physical Console Frame Layout vs Fullscreen Overlay
        if (config.displayLayout == DisplayLayout.PSP_CONSOLE_FRAME) {
            // Physical PSP Console Chassis surrounding screen
            PspChassisFrame(
                game = game,
                config = config,
                inputState = inputState,
                soundEngine = soundEngine,
                onHomePress = {
                    soundEngine.playConfirm()
                    isPaused = !isPaused
                }
            ) {
                RenderActiveGame(game, config, inputState, soundEngine)
            }
        } else {
            // Fullscreen rendering with transparent virtual control overlay
            RenderActiveGame(game, config, inputState, soundEngine)

            // Touch Controls Overlay
            if (!isPaused) {
                PspVirtualControls(
                    config = config,
                    listener = controllerListener
                )
            }
        }

        // Real-time RAM & FPS Performance Telemetry OSD
        var usedRamMb by remember { mutableLongStateOf(0L) }
        var maxRamMb by remember { mutableLongStateOf(0L) }
        LaunchedEffect(Unit) {
            while (true) {
                val runtime = Runtime.getRuntime()
                usedRamMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                maxRamMb = runtime.maxMemory() / (1024 * 1024)
                delay(1000)
            }
        }

        // Top Status Telemetry (FPS, RAM, Speed %, Backend)
        if ((config.showFpsCounter || config.showRamViewer) && !isPaused) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (config.showFpsCounter) {
                    Text(
                        text = "%.1f FPS (%.0f%%)".format(
                            currentFps,
                            if (inputState.turbo) 200f else 100f
                        ),
                        color = if (inputState.turbo) Color.Yellow else Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (config.showRamViewer) {
                    Text(
                        text = "RAM: %d MB / %d MB".format(usedRamMb, maxRamMb),
                        color = Color.Cyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (config.showVramViewer) {
                    Text(
                        text = "PSP: ${config.pspModel.memoryRam}",
                        color = Color(0xFFFFB74D),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = config.backend.techName,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Live notification banner (Save state / Load state success)
        statusMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF003882).copy(alpha = 0.9f))
                    .border(1.dp, Color.Cyan, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // IN-GAME PAUSE MENU (PSP HOME BUTTON OVERLAY)
        AnimatedVisibility(
            visible = isPaused,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { isPaused = false }
                    .testTag("psp_pause_menu"),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(360.dp)
                        .clickable(enabled = false) {}, // Prevent dismiss when tapping card
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141A24)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.Cyan.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = game.title,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Menú de Pausa PSP • ${game.discId}",
                                    color = Color.Cyan,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = {
                                soundEngine.playCancel()
                                isPaused = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                            }
                        }

                        // Save State Slots row
                        Text(
                            text = "RANURA DE ESTADO:",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            (1..5).forEach { slotNum ->
                                val isCur = slotNum == currentSlot
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isCur) Color.Cyan else Color.White.copy(alpha = 0.12f))
                                        .clickable {
                                            soundEngine.playClick()
                                            currentSlot = slotNum
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$slotNum",
                                        color = if (isCur) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Save State Button
                        PauseMenuButton(
                            title = "Guardar Estado (Ranura $currentSlot)",
                            icon = Icons.Default.FileUpload,
                            color = Color(0xFF00E676),
                            tag = "pause_save_state"
                        ) {
                            soundEngine.playConfirm()
                            saveSlots[currentSlot - 1] = SaveStateSlot(
                                slotNumber = currentSlot,
                                timestamp = System.currentTimeMillis(),
                                gameTitle = game.title,
                                description = "Guardado a las %tT".format(System.currentTimeMillis())
                            )
                            statusMessage = "Estado guardado en Ranura $currentSlot"
                            isPaused = false
                        }

                        // Load State Button
                        PauseMenuButton(
                            title = "Cargar Estado (Ranura $currentSlot)",
                            icon = Icons.Default.FileDownload,
                            color = Color(0xFF00B0FF),
                            tag = "pause_load_state"
                        ) {
                            soundEngine.playConfirm()
                            statusMessage = "Estado cargado desde Ranura $currentSlot"
                            isPaused = false
                        }

                        // Fast Forward Turbo Switch
                        PauseMenuButton(
                            title = if (inputState.turbo) "Desactivar Modo Turbo" else "Activar Modo Turbo 2X",
                            icon = Icons.Default.FastForward,
                            color = Color(0xFFFFD600),
                            tag = "pause_turbo_toggle"
                        ) {
                            inputState.turbo = !inputState.turbo
                            soundEngine.playConfirm()
                            statusMessage = if (inputState.turbo) "Turbo 200% Activado" else "Velocidad Normal 100%"
                        }

                        // Quick Settings
                        PauseMenuButton(
                            title = "Ajustes de Juego",
                            icon = Icons.Default.Settings,
                            color = Color.White,
                            tag = "pause_settings"
                        ) {
                            soundEngine.playClick()
                            onOpenSettings(0)
                        }

                        // Exit to Main Menu
                        PauseMenuButton(
                            title = "Salir al Menú Principal",
                            icon = Icons.Default.PowerSettingsNew,
                            color = Color(0xFFFF5252),
                            tag = "pause_exit"
                        ) {
                            soundEngine.playCancel()
                            onExitGame()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderActiveGame(
    game: PspGame,
    config: EmulatorConfig,
    input: GameInputState,
    soundEngine: PspSoundEngine
) {
    when (game.type) {
        GameType.RIDGE_RACER -> RidgeRacerGame(config, input, soundEngine)
        GameType.GOD_OF_OLYMPUS -> SpartanCombatGame(config, input, soundEngine)
        GameType.CRISIS_CORE -> CrisisBattleGame(config, input, soundEngine)
        GameType.BENCHMARK_3D -> Psp3dBenchmark(config, input)
        GameType.PS1_CRASH -> CrashWarpedGame(config, input, soundEngine)
        GameType.PS1_TEKKEN -> Tekken3Game(config, input, soundEngine)
        GameType.CUSTOM_ISO -> RidgeRacerGame(config, input, soundEngine) // Runs in high-speed racer sandbox
    }
}

@Composable
fun PauseMenuButton(
    title: String,
    icon: ImageVector,
    color: Color,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PspChassisFrame(
    game: PspGame,
    config: EmulatorConfig,
    inputState: GameInputState,
    soundEngine: PspSoundEngine,
    onHomePress: () -> Unit,
    screenContent: @Composable () -> Unit
) {
    // Physical PSP Chassis Frame surrounding the screen (inspired by user uploaded image)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0F14))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glossy black handheld shell
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(Color(0xFF1E222A), Color(0xFF0D0F14), Color(0xFF191D24))
                    )
                )
                .border(2.dp, Color(0xFF374151), RoundedCornerShape(32.dp))
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            // SONY branding top right
            Text(
                text = "SONY",
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp)
            )

            // PlayStation logo top left
            Text(
                text = "PS",
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp)
            )

            // Embedded Screen Inset
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 60.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(2.dp, Color.Black, RoundedCornerShape(6.dp))
            ) {
                screenContent()
            }

            // Bottom bar with HOME, VOL, PSP logo, SELECT, START
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HOME",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onHomePress() }
                )
                Text(
                    text = "PSP",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "START",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { soundEngine.playConfirm() }
                )
            }
        }
    }
}
