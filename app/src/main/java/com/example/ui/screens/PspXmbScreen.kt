package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Wifi
import com.example.ui.components.PspUmdVideoPlayerDialog
import com.example.ui.components.PspMusicPlayerDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.audio.PspSoundEngine
import com.example.model.ConsolePlatform
import com.example.model.EmulatorConfig
import com.example.model.PspGame
import com.example.model.UiMode
import com.example.ui.components.PspWaveBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class XmbCategoryDef(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val items: List<XmbItemDef>
)

data class XmbItemDef(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tag: String,
    val gamePayload: PspGame? = null,
    val isSavedData: Boolean = false,
    val actionType: XmbAction
)

enum class XmbAction {
    LAUNCH_GAME,
    OPEN_SAVED_DATA,
    OPEN_SETTINGS,
    OPEN_LOAD_ISO,
    OPEN_LOAD_PS1,
    REPLAY_BOOT,
    SWITCH_UI_MODE,
    CYCLE_WAVE_COLOR,
    OPEN_UPDATE,
    OPEN_FILE_MANAGER,
    OPEN_CAMERA,
    OPEN_VIDEO_PLAYER,
    OPEN_MUSIC_PLAYER,
    CUSTOM
}

data class InternalCoverOption(
    val id: String,
    val title: String,
    val system: String,
    val resName: String
)

val INTERNAL_COVERS = listOf(
    InternalCoverOption("ps1_crash", "Crash Bandicoot 3 (PS1)", "PS1 Classic", "ps1_cover_crash_1793138708025"),
    InternalCoverOption("ps1_tekken", "Tekken 3 (PS1)", "PS1 Classic", "ps1_cover_tekken_1793138721941"),
    InternalCoverOption("psp_ridge", "Ridge Racer PSP", "PSP UMD", "psp_ridge_cover_1793136775170"),
    InternalCoverOption("psp_god", "God of Olympus (PSP)", "PSP UMD", "psp_wave_bg_1793136763810"),
    InternalCoverOption("psp_neon", "PSP Cyan Controller", "Hardware", "ic_psp_logo_1793136675294")
)

@Composable
fun PspXmbScreen(
    config: EmulatorConfig,
    games: List<PspGame>,
    soundEngine: PspSoundEngine,
    onLaunchGame: (PspGame) -> Unit,
    onOpenSettings: (initialTab: Int) -> Unit,
    onOpenIsoPicker: () -> Unit,
    onOpenPs1Picker: () -> Unit = {},
    onPickCoverImage: (gameId: String) -> Unit = {},
    onReplayBoot: () -> Unit,
    onSwitchUiMode: (UiMode) -> Unit,
    onNextWaveColor: () -> Unit,
    onOpenUpdate: () -> Unit = {},
    onOpenCamera: () -> Unit = {},
    onOpenFileManager: () -> Unit = {},
    onRenameGame: (gameId: String, newName: String) -> Unit = { _, _ -> },
    onRemoveGame: (gameId: String) -> Unit = { _ -> },
    onUpdateGameCover: (gameId: String, coverResOrUri: String) -> Unit = { _, _ -> }
) {
    // Categories matching PSP XMB from screenshots
    val categories = remember(games, config.waveColor) {
        listOf(
            XmbCategoryDef(
                id = "settings",
                title = "Settings",
                icon = Icons.Default.Settings,
                items = listOf(
                    XmbItemDef("th_set", "Theme Settings", "Color actual: ${config.waveColor.label}", Icons.Default.Settings, "xmb_theme_set", actionType = XmbAction.CYCLE_WAVE_COLOR),
                    XmbItemDef("gfx_set", "Video Settings", "Vulkan • 2x HD • Shaders", Icons.Default.Movie, "xmb_video_set", actionType = XmbAction.OPEN_SETTINGS),
                    XmbItemDef("snd_set", "Audio Settings", "OpenSL ES • Reverb • 64ms", Icons.Default.MusicNote, "xmb_audio_set", actionType = XmbAction.OPEN_SETTINGS),
                    XmbItemDef("sys_set", "System Settings", "${config.pspModel.label} • ${config.cpuClock.label}", Icons.Default.Settings, "xmb_system_set", actionType = XmbAction.OPEN_SETTINGS),
                    XmbItemDef("boot_set", "System Update", "Actualización del sistema • v${config.systemFirmware}", Icons.Default.SystemUpdate, "xmb_sys_update", actionType = XmbAction.OPEN_UPDATE)
                )
            ),
            XmbCategoryDef(
                id = "photo",
                title = "Photo",
                icon = Icons.Default.CameraAlt,
                items = listOf(
                    XmbItemDef("photo_cam", "Camera Utility", "Go!Cam (Cámara interna del celular)", Icons.Default.CameraAlt, "xmb_cam_util", actionType = XmbAction.OPEN_CAMERA),
                    XmbItemDef("photo_gal", "Memory Stick™", "Carpetas /PSP/PHOTO y Almacenamiento", Icons.Default.SdCard, "xmb_photo_gal", actionType = XmbAction.OPEN_FILE_MANAGER)
                )
            ),
            XmbCategoryDef(
                id = "music",
                title = "Music",
                icon = Icons.Default.MusicNote,
                items = listOf(
                    XmbItemDef("mus_umd", "UMD® Audio Player", "Música interna /PSP/MUSIC", Icons.Default.MusicNote, "xmb_umd_music", actionType = XmbAction.OPEN_MUSIC_PLAYER)
                )
            ),
            XmbCategoryDef(
                id = "video",
                title = "Video",
                icon = Icons.Default.Movie,
                items = listOf(
                    XmbItemDef("vid_umd", "UMD® Video", "Videos en tiempo real /PSP/VIDEO", Icons.Default.Movie, "xmb_umd_video", actionType = XmbAction.OPEN_VIDEO_PLAYER)
                )
            ),
            XmbCategoryDef(
                id = "game",
                title = "Game",
                icon = Icons.Default.Gamepad,
                items = buildList {
                    // Saved Data Utility
                    add(
                        XmbItemDef(
                            id = "saved_data_util",
                            title = "Saved Data Utility",
                            subtitle = "Memory Stick Duo™ • 14.8 GB Libres",
                            icon = Icons.Default.SdCard,
                            tag = "xmb_saved_data",
                            isSavedData = true,
                            actionType = XmbAction.OPEN_SAVED_DATA
                        )
                    )
                    // Games list as cards (seen in Image 3)
                    games.forEach { g ->
                        val platformBadge = if (g.platform == ConsolePlatform.PS1) "PS1 Classic • " else "PSP • "
                        add(
                            XmbItemDef(
                                id = "game_${g.id}",
                                title = g.title,
                                subtitle = "$platformBadge${g.format} • ${g.sizeText}",
                                icon = Icons.Default.PlayArrow,
                                tag = "xmb_game_${g.id}",
                                gamePayload = g,
                                actionType = XmbAction.LAUNCH_GAME
                            )
                        )
                    }
                    // Add PS1 Game Action
                    add(
                        XmbItemDef(
                            id = "load_ps1_btn",
                            title = "Agregar Juego PS1 (BIN / ISO / CUE / PBP)",
                            subtitle = "Lanzador PlayStation 1 con carátula personalizada",
                            icon = Icons.Default.Gamepad,
                            tag = "xmb_load_ps1",
                            actionType = XmbAction.OPEN_LOAD_PS1
                        )
                    )
                    // Add PSP Game Action
                    add(
                        XmbItemDef(
                            id = "load_iso_btn",
                            title = "Agregar Juego PSP (ISO / CSO)",
                            subtitle = "Explorar archivos del almacenamiento",
                            icon = Icons.Default.SdCard,
                            tag = "xmb_load_iso",
                            actionType = XmbAction.OPEN_LOAD_ISO
                        )
                    )
                    add(
                        XmbItemDef(
                            id = "game_folders_btn",
                            title = "Explorador de Carpetas PSP (/Pictures/PSP)",
                            subtitle = "Estructura original: /GAME, /SAVEDATA, /Cheats",
                            icon = Icons.Default.Folder,
                            tag = "xmb_game_folders",
                            actionType = XmbAction.OPEN_FILE_MANAGER
                        )
                    )
                }
            ),
            XmbCategoryDef(
                id = "network",
                title = "Network",
                icon = Icons.Default.Language,
                items = listOf(
                    XmbItemDef("net_update", "Actualización de red", "Buscar nueva versión de PPSSPP", Icons.Default.SystemUpdate, "xmb_net_update", actionType = XmbAction.OPEN_UPDATE),
                    XmbItemDef("net_web", "Internet Browser", "Navegador Web PSP", Icons.Default.Language, "xmb_net_browser", actionType = XmbAction.CUSTOM),
                    XmbItemDef("net_ppsspp", "Cambiar a Modo PPSSPP", "Vista en cuadrícula", Icons.Default.ViewModule, "xmb_switch_mode", actionType = XmbAction.SWITCH_UI_MODE)
                )
            )
        )
    }

    var selectedCatIndex by remember { mutableIntStateOf(4) } // Default to "Game"
    var selectedItemIndex by remember { mutableIntStateOf(1) } // Default to first game

    // Dialog & Options states
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showInternalCoverDialog by remember { mutableStateOf(false) }
    var showVideoPlayerDialog by remember { mutableStateOf(false) }
    var showMusicPlayerDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var selectedGameForOptions by remember { mutableStateOf<PspGame?>(null) }

    val activeCategory = categories.getOrElse(selectedCatIndex) { categories[4] }
    val currentSelectedGame = activeCategory.items.getOrNull(selectedItemIndex)?.gamePayload

    // Date & Time String matching PSP header
    val headerDateTime = remember {
        val dayName = SimpleDateFormat("EEEE", Locale.US).format(Date()).replaceFirstChar { it.uppercase() }
        val dateRest = SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US).format(Date()).lowercase()
        "($dayName) $dateRest"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("psp_xmb_screen")
    ) {
        // Dynamic Ribbon Wave Canvas
        PspWaveBackground(waveColor = config.waveColor)

        // 1. TOP STATUS HEADER (matching Image 1 & 2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerDateTime,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wi-Fi",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            PspBatteryIcon()
        }

        // 2. MAIN XMB CROSS MEDIA BAR
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp)
        ) {
            // Horizontal Categories Rail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(42.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEachIndexed { index, cat ->
                        val isSelected = index == selectedCatIndex
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.25f else 0.88f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "cat_scale"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .scale(iconScale)
                                .clickable {
                                    if (selectedCatIndex != index) {
                                        soundEngine.playClick()
                                        soundEngine.vibrate(18)
                                        selectedCatIndex = index
                                        selectedItemIndex = 0
                                    }
                                }
                                .testTag("xmb_cat_${cat.id}")
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = cat.title,
                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = cat.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vertical Sub-Items List (aligned underneath active category)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 48.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(activeCategory.items) { itemIdx, item ->
                    val isSelected = itemIdx == selectedItemIndex
                    val itemScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.02f else 0.98f,
                        label = "item_scale"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(itemScale)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        soundEngine.playConfirm()
                                        soundEngine.vibrate(25)
                                        selectedItemIndex = itemIdx

                                        when (item.actionType) {
                                            XmbAction.LAUNCH_GAME -> {
                                                item.gamePayload?.let { onLaunchGame(it) }
                                            }
                                            XmbAction.OPEN_SETTINGS -> {
                                                onOpenSettings(0)
                                            }
                                            XmbAction.OPEN_LOAD_ISO -> {
                                                onOpenIsoPicker()
                                            }
                                            XmbAction.OPEN_LOAD_PS1 -> {
                                                onOpenPs1Picker()
                                            }
                                            XmbAction.REPLAY_BOOT -> {
                                                onReplayBoot()
                                            }
                                            XmbAction.CYCLE_WAVE_COLOR -> {
                                                onNextWaveColor()
                                            }
                                            XmbAction.SWITCH_UI_MODE -> {
                                                onSwitchUiMode(UiMode.PPSSPP_GRID)
                                            }
                                            XmbAction.OPEN_UPDATE -> {
                                                onOpenUpdate()
                                            }
                                            XmbAction.OPEN_FILE_MANAGER -> {
                                                onOpenFileManager()
                                            }
                                            XmbAction.OPEN_CAMERA -> {
                                                onOpenCamera()
                                            }
                                            XmbAction.OPEN_VIDEO_PLAYER -> {
                                                showVideoPlayerDialog = true
                                            }
                                            XmbAction.OPEN_MUSIC_PLAYER -> {
                                                showMusicPlayerDialog = true
                                            }
                                            XmbAction.OPEN_SAVED_DATA -> {
                                                selectedGameForOptions = games.firstOrNull()
                                                showOptionsMenu = true
                                            }
                                            XmbAction.CUSTOM -> {}
                                        }
                                    },
                                    onLongPress = {
                                        if (item.gamePayload != null) {
                                            soundEngine.playConfirm()
                                            selectedItemIndex = itemIdx
                                            selectedGameForOptions = item.gamePayload
                                            showOptionsMenu = true
                                        }
                                    }
                                )
                            }
                            .testTag(item.tag),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Game Cover Banner / Thumbnail
                        if (item.gamePayload != null) {
                            GameCoverBox(
                                game = item.gamePayload,
                                isSelected = isSelected,
                                modifier = Modifier.size(width = 110.dp, height = 54.dp)
                            )
                        } else {
                            // Standard XMB Item Icon
                            Box(
                                modifier = Modifier
                                    .size(width = 54.dp, height = 40.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Title & Subtitle
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = if (isSelected) 17.sp else 15.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif
                                )
                                item.gamePayload?.let { g ->
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                if (g.platform == ConsolePlatform.PS1) Color(0xFFD50000)
                                                else Color(0xFF003882)
                                            )
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = if (g.platform == ConsolePlatform.PS1) "PS1" else "PSP",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            if (item.subtitle.isNotEmpty()) {
                                Text(
                                    text = item.subtitle,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Options hint button
                        if (isSelected && item.gamePayload != null) {
                            Text(
                                text = "△ OPCIONES",
                                color = Color.Cyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .clickable {
                                        soundEngine.playConfirm()
                                        selectedGameForOptions = item.gamePayload
                                        showOptionsMenu = true
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. BOTTOM BUTTON LEGEND BAR
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode switch pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable {
                        soundEngine.playClick()
                        onSwitchUiMode(UiMode.PPSSPP_GRID)
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ViewModule,
                    contentDescription = "PPSSPP Mode",
                    tint = Color.Cyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Vista PPSSPP", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // PSP Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PspActionLegend(symbol = "✕", label = "Entrar", color = Color(0xFF40C4FF))
                PspActionLegend(symbol = "○", label = "Atrás", color = Color(0xFFFF5252))
                Box(modifier = Modifier.clickable {
                    soundEngine.playConfirm()
                    selectedGameForOptions = currentSelectedGame ?: games.firstOrNull()
                    showOptionsMenu = true
                }) {
                    PspActionLegend(symbol = "△", label = "Opciones", color = Color(0xFF00E676))
                }
            }
        }

        // 4. PSP OPTIONS (△) SIDE MENU (seen in Image 3)
        AnimatedVisibility(
            visible = showOptionsMenu,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val gameTarget = selectedGameForOptions ?: games.firstOrNull()

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(235.dp)
                    .background(Color(0xFF283548).copy(alpha = 0.94f))
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.15f))
                    .padding(vertical = 14.dp)
                    .testTag("psp_options_side_menu")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OPCIONES",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    soundEngine.playCancel()
                                    showOptionsMenu = false
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Start Game
                    PspSideOptionItem(title = "Start Game", icon = Icons.Default.PlayArrow) {
                        soundEngine.playConfirm()
                        showOptionsMenu = false
                        gameTarget?.let { onLaunchGame(it) }
                    }

                    // Details
                    PspSideOptionItem(title = "Details", icon = Icons.Default.Info) {
                        soundEngine.playConfirm()
                        showDetailsDialog = true
                    }

                    // Change Cover via Photo Picker (PNG / JPG)
                    PspSideOptionItem(title = "Buscar Carátula (PNG/JPG)", icon = Icons.Default.AddPhotoAlternate) {
                        soundEngine.playConfirm()
                        showOptionsMenu = false
                        gameTarget?.let { onPickCoverImage(it.id) }
                    }

                    // Select Internal PSP/PS1 Cover
                    PspSideOptionItem(title = "Carátula Interna PSP/PS1", icon = Icons.Default.Collections) {
                        soundEngine.playClick()
                        showInternalCoverDialog = true
                    }

                    // Cheats
                    PspSideOptionItem(title = "Cheats (CWCheat)", icon = Icons.Default.Settings) {
                        soundEngine.playClick()
                        showOptionsMenu = false
                        onOpenSettings(4)
                    }

                    // Change Name
                    PspSideOptionItem(title = "Change Name", icon = Icons.Default.Edit) {
                        soundEngine.playClick()
                        renameText = gameTarget?.title ?: ""
                        showRenameDialog = true
                    }

                    // Remove
                    PspSideOptionItem(title = "Remove", icon = Icons.Default.Close) {
                        soundEngine.playConfirm()
                        showOptionsMenu = false
                        gameTarget?.let { onRemoveGame(it.id) }
                    }
                }
            }
        }

        // DETAILS DIALOG
        if (showDetailsDialog) {
            val gameTarget = selectedGameForOptions ?: games.firstOrNull()
            AlertDialog(
                onDismissRequest = { showDetailsDialog = false },
                title = {
                    Text(
                        text = "Información del Juego (${if (gameTarget?.platform == ConsolePlatform.PS1) "PS1" else "PSP"})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PspDetailRow("Título", gameTarget?.title ?: "Desconocido")
                        PspDetailRow("Sistema", if (gameTarget?.platform == ConsolePlatform.PS1) "PlayStation 1 (POPS)" else "PSP UMD")
                        PspDetailRow("Formato", gameTarget?.format ?: "ISO")
                        PspDetailRow("Código", gameTarget?.discId ?: "SLUS-00000")
                        PspDetailRow("Región", gameTarget?.region ?: "USA / NTSC")
                        PspDetailRow("Tamaño", gameTarget?.sizeText ?: "1.2 GB")
                        PspDetailRow("Carátula", if (gameTarget?.customCoverUri != null) "Imagen PNG/JPG Personalizada" else "Carátula Interna")
                        PspDetailRow("Motor Gráfico", config.backend.displayName)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showDetailsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                    ) {
                        Text("Aceptar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color(0xFF192230)
            )
        }

        // RENAME GAME DIALOG
        if (showRenameDialog) {
            val gameTarget = selectedGameForOptions ?: games.firstOrNull()
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = {
                    Text(
                        text = "Cambiar Nombre del Juego",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        label = { Text("Nuevo Título") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Cyan,
                            focusedLabelColor = Color.Cyan
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            gameTarget?.let { onRenameGame(it.id, renameText) }
                            showRenameDialog = false
                            showOptionsMenu = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                    ) {
                        Text("Guardar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
                    }
                },
                containerColor = Color(0xFF192230)
            )
        }

        // SELECT INTERNAL COVER ART DIALOG
        if (showInternalCoverDialog) {
            val gameTarget = selectedGameForOptions ?: games.firstOrNull()
            val context = LocalContext.current

            AlertDialog(
                onDismissRequest = { showInternalCoverDialog = false },
                title = {
                    Text(
                        text = "Seleccionar Carátula Interna PSP / PS1",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Elige una de las carátulas oficiales integradas:",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        INTERNAL_COVERS.forEach { cover ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF243044))
                                    .clickable {
                                        gameTarget?.let {
                                            onUpdateGameCover(it.id, cover.resName)
                                        }
                                        soundEngine.playConfirm()
                                        showInternalCoverDialog = false
                                        showOptionsMenu = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val resId = context.resources.getIdentifier(cover.resName, "drawable", context.packageName)
                                if (resId != 0) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = cover.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = cover.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = cover.system, color = Color.Cyan, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showInternalCoverDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                    ) {
                        Text("Cerrar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color(0xFF192230)
            )
        }

        // Real-time UMD Video Player Dialog
        if (showVideoPlayerDialog) {
            PspUmdVideoPlayerDialog(
                title = "UMD® Video • PSP Showcase 1080p",
                soundEngine = soundEngine,
                onDismiss = { showVideoPlayerDialog = false }
            )
        }

        // Real-time Audio Visualizer Player Dialog
        if (showMusicPlayerDialog) {
            PspMusicPlayerDialog(
                trackTitle = "PSP Ambient Silk Theme",
                artist = "Sony Computer Entertainment Inc.",
                soundEngine = soundEngine,
                onDismiss = { showMusicPlayerDialog = false }
            )
        }
    }
}

@Composable
fun GameCoverBox(
    game: PspGame,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resId = remember(game.drawableResName) {
        if (!game.drawableResName.isNullOrEmpty()) {
            context.resources.getIdentifier(game.drawableResName, "drawable", context.packageName)
        } else 0
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1A2232))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!game.customCoverUri.isNullOrEmpty()) {
            // User-picked PNG or JPG via Photo Picker
            AsyncImage(
                model = game.customCoverUri,
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (resId != 0) {
            // Built-in high-resolution cover
            Image(
                painter = painterResource(id = resId),
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fallback console card
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (game.platform == ConsolePlatform.PS1) "PS1" else "PSP",
                    color = Color.Cyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = game.format,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Platform badge at bottom left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(
                    if (game.platform == ConsolePlatform.PS1) Color(0xFFD50000).copy(alpha = 0.9f)
                    else Color(0xFF003882).copy(alpha = 0.9f)
                )
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = if (game.platform == ConsolePlatform.PS1) "PS1" else "PSP",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PspSideOptionItem(
    title: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.Cyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun PspDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PspBatteryIcon() {
    Canvas(modifier = Modifier.size(width = 24.dp, height = 12.dp)) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(0f, 0f),
            size = Size(w - 3f, h),
            cornerRadius = CornerRadius(2f, 2f),
            style = Stroke(width = 1.5f)
        )
        drawRect(
            color = Color.White,
            topLeft = Offset(w - 2f, h * 0.28f),
            size = Size(2f, h * 0.44f)
        )

        val barW = 4f
        val gap = 2f
        for (i in 0..2) {
            drawRect(
                color = Color.White,
                topLeft = Offset(2.5f + i * (barW + gap), 2f),
                size = Size(barW, h - 4f)
            )
        }
    }
}

@Composable
fun PspActionLegend(symbol: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.25f))
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp
        )
    }
}

