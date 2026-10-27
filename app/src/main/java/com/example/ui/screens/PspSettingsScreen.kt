package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VolumeUp
import com.example.model.AppLanguage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PspSoundEngine
import com.example.model.CpuClock
import com.example.model.DisplayLayout
import com.example.model.EmulatorConfig
import com.example.model.FrameSkip
import com.example.model.PostProcessingShader
import com.example.model.PspGame
import com.example.model.PspModel
import com.example.model.RenderingBackend
import com.example.model.ResolutionScale
import com.example.model.TextureFilter
import com.example.model.XmbWaveColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PspSettingsScreen(
    config: EmulatorConfig,
    games: List<PspGame>,
    soundEngine: PspSoundEngine,
    initialTab: Int = 0,
    onConfigChange: (EmulatorConfig) -> Unit,
    onBack: () -> Unit,
    onOpenUpdate: () -> Unit = {},
    onOpenFileManager: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabTitles = listOf("Gráficos", "Audio", "Controles", "Sistema", "Trucos CWCheat")
    val tabIcons = listOf(
        Icons.Default.DisplaySettings,
        Icons.Default.VolumeUp,
        Icons.Default.Gamepad,
        Icons.Default.Settings,
        Icons.Default.Code
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F131A))
            .testTag("psp_settings_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Ajustes Avanzados PPSSPP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        soundEngine.playCancel()
                        onBack()
                    },
                    modifier = Modifier.testTag("settings_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        soundEngine.playConfirm()
                        onConfigChange(EmulatorConfig())
                    },
                    modifier = Modifier.testTag("settings_reset_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reiniciar",
                        tint = Color.Cyan
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF161C26))
        )

        // Settings Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF161C26),
            contentColor = Color.White,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color.Cyan,
                    height = 3.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        soundEngine.playClick()
                        selectedTab = index
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = title,
                                tint = if (selectedTab == index) Color.Cyan else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                color = if (selectedTab == index) Color.Cyan else Color.White.copy(alpha = 0.7f),
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
            }
        }

        // Tab Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // GRÁFICOS
                    item {
                        SettingsCategoryHeader("MOTOR DE RENDERIZADO")
                        SettingsSelector(
                            title = "Backend Gráfico",
                            subtitle = config.backend.displayName,
                            options = RenderingBackend.values().map { it.displayName },
                            selectedIndex = RenderingBackend.values().indexOf(config.backend),
                            onSelect = { idx ->
                                onConfigChange(config.copy(backend = RenderingBackend.values()[idx]))
                            }
                        )
                        SettingsSelector(
                            title = "Resolución de Renderizado",
                            subtitle = "${config.resolutionScale.label} (${config.resolutionScale.resolution})",
                            options = ResolutionScale.values().map { "${it.label} • ${it.resolution}" },
                            selectedIndex = ResolutionScale.values().indexOf(config.resolutionScale),
                            onSelect = { idx ->
                                onConfigChange(config.copy(resolutionScale = ResolutionScale.values()[idx]))
                            }
                        )
                        SettingsSelector(
                            title = "Salto de Cuadros (Frame Skipping)",
                            subtitle = config.frameSkip.label,
                            options = FrameSkip.values().map { it.label },
                            selectedIndex = FrameSkip.values().indexOf(config.frameSkip),
                            onSelect = { idx ->
                                onConfigChange(config.copy(frameSkip = FrameSkip.values()[idx]))
                            }
                        )
                    }

                    item {
                        SettingsCategoryHeader("POST-PROCESAMIENTO & SHADERS")
                        SettingsSelector(
                            title = "Filtro Shader de Pantalla",
                            subtitle = "${config.shader.label}: ${config.shader.desc}",
                            options = PostProcessingShader.values().map { "${it.label} (${it.desc})" },
                            selectedIndex = PostProcessingShader.values().indexOf(config.shader),
                            onSelect = { idx ->
                                onConfigChange(config.copy(shader = PostProcessingShader.values()[idx]))
                            }
                        )
                        SettingsSelector(
                            title = "Filtrado de Texturas",
                            subtitle = config.textureFilter.label,
                            options = TextureFilter.values().map { it.label },
                            selectedIndex = TextureFilter.values().indexOf(config.textureFilter),
                            onSelect = { idx ->
                                onConfigChange(config.copy(textureFilter = TextureFilter.values()[idx]))
                            }
                        )
                    }

                    item {
                        SettingsCategoryHeader("OPTIMIZACIONES DE RENDIMIENTO")
                        SettingsSwitch(
                            title = "Transformación por Hardware",
                            subtitle = "Acelera geometría 3D en la GPU móvil",
                            checked = config.hardwareTransform,
                            onCheckedChange = { onConfigChange(config.copy(hardwareTransform = it)) }
                        )
                        SettingsSwitch(
                            title = "Mapeado Rápido de Memoria (Fastmem)",
                            subtitle = "Mayor velocidad con optimización de punteros",
                            checked = config.fastMemory,
                            onCheckedChange = { onConfigChange(config.copy(fastMemory = it)) }
                        )
                        SettingsSwitch(
                            title = "Mostrar Contador de FPS y Velocidad",
                            subtitle = "Muestra 60 FPS y porcentaje en la esquina superior",
                            checked = config.showFpsCounter,
                            onCheckedChange = { onConfigChange(config.copy(showFpsCounter = it)) }
                        )
                        SettingsSwitch(
                            title = "Mostrar Visor de Memoria RAM",
                            subtitle = "Muestra uso de RAM física del celular (MB) y RAM PSP (64MB)",
                            checked = config.showRamViewer,
                            onCheckedChange = { onConfigChange(config.copy(showRamViewer = it)) }
                        )
                    }
                }

                1 -> {
                    // AUDIO
                    item {
                        SettingsCategoryHeader("AUDIO Y SONIDO")
                        SettingsSlider(
                            title = "Volumen Maestro",
                            value = config.masterVolume,
                            range = 0f..2.0f,
                            label = "${(config.masterVolume * 100).roundToInt()}%",
                            onValueChange = { onConfigChange(config.copy(masterVolume = it)) }
                        )
                        SettingsSlider(
                            title = "Volumen de Efectos",
                            value = config.soundEffectsVolume,
                            range = 0f..1.0f,
                            label = "${(config.soundEffectsVolume * 100).roundToInt()}%",
                            onValueChange = { onConfigChange(config.copy(soundEffectsVolume = it)) }
                        )
                        SettingsSwitch(
                            title = "Efecto Reverb PSP",
                            subtitle = "Simula la resonancia acústica del hardware PSP",
                            checked = config.pspReverb,
                            onCheckedChange = { onConfigChange(config.copy(pspReverb = it)) }
                        )
                        SettingsSwitch(
                            title = "Sincronización de Audio",
                            subtitle = "Evita desincronización y chasquidos",
                            checked = config.audioSync,
                            onCheckedChange = { onConfigChange(config.copy(audioSync = it)) }
                        )
                    }
                }

                2 -> {
                    // CONTROLES
                    item {
                        SettingsCategoryHeader("CONTROLES TÁCTILES EN PANTALLA")
                        SettingsSwitch(
                            title = "Mostrar Botones en Pantalla",
                            subtitle = "D-Pad, Stick analógico, △ ○ ✕ □ y Gatillos L/R",
                            checked = config.showTouchControls,
                            onCheckedChange = { onConfigChange(config.copy(showTouchControls = it)) }
                        )
                        SettingsSlider(
                            title = "Opacidad de Botones",
                            value = config.buttonOpacity,
                            range = 0.2f..1.0f,
                            label = "${(config.buttonOpacity * 100).roundToInt()}%",
                            onValueChange = { onConfigChange(config.copy(buttonOpacity = it)) }
                        )
                        SettingsSlider(
                            title = "Tamaño de Botones",
                            value = config.buttonScale,
                            range = 0.7f..1.4f,
                            label = "${(config.buttonScale * 100).roundToInt()}%",
                            onValueChange = { onConfigChange(config.copy(buttonScale = it)) }
                        )
                        SettingsSwitch(
                            title = "Vibración Háptica al Pulsar",
                            subtitle = "Retroalimentación táctil de botones",
                            checked = config.hapticFeedback,
                            onCheckedChange = { onConfigChange(config.copy(hapticFeedback = it)) }
                        )
                        SettingsSwitch(
                            title = "Mostrar Botón Turbo 2X",
                            subtitle = "Acceso rápido a avance de fotogramas",
                            checked = config.showTurboButton,
                            onCheckedChange = { onConfigChange(config.copy(showTurboButton = it)) }
                        )
                        SettingsSwitch(
                            title = "Invertir Botones ✕ / ○",
                            subtitle = "Estilo Occidental (✕ Aceptar) vs Japonés (○ Aceptar)",
                            checked = config.swapConfirmButton,
                            onCheckedChange = { onConfigChange(config.copy(swapConfirmButton = it)) }
                        )
                    }
                }

                3 -> {
                    // SISTEMA
                    item {
                        SettingsCategoryHeader("SISTEMA PSP EMULADO")
                        SettingsSelector(
                            title = "Modelo de PSP",
                            subtitle = "${config.pspModel.label} • ${config.pspModel.memoryRam}",
                            options = PspModel.values().map { "${it.label} (${it.memoryRam})" },
                            selectedIndex = PspModel.values().indexOf(config.pspModel),
                            onSelect = { idx ->
                                onConfigChange(config.copy(pspModel = PspModel.values()[idx]))
                            }
                        )
                        SettingsSelector(
                            title = "Frecuencia de CPU Emulada",
                            subtitle = config.cpuClock.label,
                            options = CpuClock.values().map { it.label },
                            selectedIndex = CpuClock.values().indexOf(config.cpuClock),
                            onSelect = { idx ->
                                onConfigChange(config.copy(cpuClock = CpuClock.values()[idx]))
                            }
                        )
                        SettingsSelector(
                            title = "Disposición de Pantalla",
                            subtitle = config.displayLayout.label,
                            options = DisplayLayout.values().map { it.label },
                            selectedIndex = DisplayLayout.values().indexOf(config.displayLayout),
                            onSelect = { idx ->
                                onConfigChange(config.copy(displayLayout = DisplayLayout.values()[idx]))
                            }
                        )
                        SettingsSelector(
                            title = "Color de Onda XMB",
                            subtitle = config.waveColor.label,
                            options = XmbWaveColor.values().map { it.label },
                            selectedIndex = XmbWaveColor.values().indexOf(config.waveColor),
                            onSelect = { idx ->
                                onConfigChange(config.copy(waveColor = XmbWaveColor.values()[idx]))
                            }
                        )
                        SettingsSwitch(
                            title = "Reproducir Intro Sony PSP al Arrancar",
                            subtitle = "Animación con sonido original de PSP",
                            checked = config.showOriginalIntroOnLaunch,
                            onCheckedChange = { onConfigChange(config.copy(showOriginalIntroOnLaunch = it)) }
                        )

                        SettingsSelector(
                            title = "Idioma del Sistema (Multi-Language)",
                            subtitle = config.appLanguage.displayName,
                            options = AppLanguage.values().map { it.displayName },
                            selectedIndex = AppLanguage.values().indexOf(config.appLanguage),
                            onSelect = { idx ->
                                val selectedLang = AppLanguage.values()[idx]
                                onConfigChange(config.copy(appLanguage = selectedLang, language = selectedLang.displayName))
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsCategoryHeader("ACTUALIZACIÓN & ALMACENAMIENTO")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF003882))
                                .clickable {
                                    soundEngine.playConfirm()
                                    onOpenUpdate()
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Buscar Actualizaciones de PPSSPP", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Versión de firmware: ${config.systemFirmware}", color = Color.Cyan, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Color.White)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E2838))
                                .clickable {
                                    soundEngine.playConfirm()
                                    onOpenFileManager()
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Almacenamiento Memory Stick™", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Directorio: .../Pictures/PSP (/GAME, /SAVEDATA, /Cheats)", color = Color.LightGray, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFFB74D))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = config.username,
                            onValueChange = { onConfigChange(config.copy(username = it)) },
                            label = { Text("Nombre de Usuario PSP") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Cyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedLabelColor = Color.Cyan
                            )
                        )
                    }
                }

                4 -> {
                    // TRUCOS CWCHEAT
                    item {
                        SettingsCategoryHeader("ADMINISTRADOR DE TRUCOS CWCHEAT")
                        Text(
                            text = "Códigos de trucos optimizados para aumentar velocidad, desbloquear contenido y vidas infinitas.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    games.forEach { game ->
                        if (game.cheats.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF161E28))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "${game.title} [${game.discId}]",
                                            color = Color.Cyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        game.cheats.forEach { cheat ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = cheat.title,
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = cheat.cwcheatCode,
                                                        color = Color.White.copy(alpha = 0.5f),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Switch(
                                                    checked = cheat.enabled,
                                                    onCheckedChange = { checked ->
                                                        soundEngine.playClick()
                                                        cheat.enabled = checked
                                                    },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color.Cyan,
                                                        checkedTrackColor = Color.Cyan.copy(alpha = 0.4f)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        color = Color.Cyan,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingsSelector(
    title: String,
    subtitle: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { androidx.compose.runtime.mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF161E28))
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = subtitle, color = Color.Cyan, fontSize = 12.sp)
                }
                Text(text = if (expanded) "▲" else "▼", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                options.forEachIndexed { idx, opt ->
                    val isCur = idx == selectedIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCur) Color.Cyan.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable {
                                onSelect(idx)
                                expanded = false
                            }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isCur) "● $opt" else "○ $opt",
                            color = if (isCur) Color.Cyan else Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = if (isCur) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF161E28))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Cyan,
                checkedTrackColor = Color.Cyan.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
fun SettingsSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    label: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF161E28))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = label, color = Color.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            valueRange = range,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color.Cyan,
                activeTrackColor = Color.Cyan
            )
        )
    }
}
