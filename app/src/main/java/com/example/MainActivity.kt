package com.example

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.PspSoundEngine
import com.example.model.PspStorageManager
import com.example.model.UiMode
import com.example.ui.screens.PspBootScreen
import com.example.ui.screens.PspCameraUtilityScreen
import com.example.ui.screens.PspFileManagerScreen
import com.example.ui.screens.PspGameScreen
import com.example.ui.screens.PspSettingsScreen
import com.example.ui.screens.PspSystemUpdateScreen
import com.example.ui.screens.PspXmbScreen
import com.example.ui.screens.PpssppGridScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EmulatorScreen
import com.example.viewmodel.EmulatorViewModel

import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PspStorageManager.initializePspFileSystem(this)

        val imageLoader = ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
        coil.Coil.setImageLoader(imageLoader)

        setContent {
            MyApplicationTheme {
                val viewModel: EmulatorViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val soundEngine = remember { PspSoundEngine(this@MainActivity) }

                var targetGameIdForCover by remember { mutableStateOf<String?>(null) }

                // 1. Android Photo Picker for Custom Cover Art (PNG / JPG)
                val coverImagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri: Uri? ->
                    uri?.let {
                        targetGameIdForCover?.let { gameId ->
                            soundEngine.playConfirm()
                            viewModel.updateGameCover(gameId, it.toString())
                        }
                    }
                }

                // 2. File picker for PS1 Games (.bin, .cue, .iso, .img, .pbp)
                val ps1PickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        var fileName = "PS1_GAME.BIN"
                        contentResolver.query(it, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex != -1 && cursor.moveToFirst()) {
                                fileName = cursor.getString(nameIndex)
                            }
                        }
                        soundEngine.playConfirm()
                        viewModel.importPs1Game(it, fileName)
                    }
                }

                // 3. File picker for PSP ISO / CSO
                val isoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        var fileName = "PSP_JUEGO.ISO"
                        contentResolver.query(it, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex != -1 && cursor.moveToFirst()) {
                                fileName = cursor.getString(nameIndex)
                            }
                        }
                        soundEngine.playConfirm()
                        viewModel.importCustomGame(it, fileName)
                    }
                }

                // Hardware / System Back Button Navigation handling
                BackHandler(enabled = true) {
                    when (uiState.currentScreen) {
                        EmulatorScreen.SETTINGS -> viewModel.closeSettings()
                        EmulatorScreen.SYSTEM_UPDATE -> viewModel.closeSystemUpdate()
                        EmulatorScreen.FILE_MANAGER -> viewModel.closeFileManager()
                        EmulatorScreen.CAMERA_UTILITY -> viewModel.closeCameraUtility()
                        EmulatorScreen.IN_GAME -> viewModel.exitGame()
                        EmulatorScreen.MAIN_MENU -> finish()
                        EmulatorScreen.BOOT_INTRO -> viewModel.finishBoot()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    Crossfade(
                        targetState = uiState.currentScreen,
                        label = "screen_crossfade"
                    ) { screen ->
                        when (screen) {
                            EmulatorScreen.BOOT_INTRO -> {
                                PspBootScreen(
                                    soundEngine = soundEngine,
                                    onBootFinished = { viewModel.finishBoot() }
                                )
                            }

                            EmulatorScreen.MAIN_MENU -> {
                                if (uiState.config.uiMode == UiMode.XMB) {
                                    PspXmbScreen(
                                        config = uiState.config,
                                        games = uiState.games,
                                        soundEngine = soundEngine,
                                        onLaunchGame = { game -> viewModel.launchGame(game) },
                                        onOpenSettings = { tab -> viewModel.openSettings(tab) },
                                        onOpenIsoPicker = { isoPickerLauncher.launch("*/*") },
                                        onOpenPs1Picker = { ps1PickerLauncher.launch("*/*") },
                                        onPickCoverImage = { gameId ->
                                            targetGameIdForCover = gameId
                                            coverImagePickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        onReplayBoot = { viewModel.replayBoot() },
                                        onSwitchUiMode = { mode -> viewModel.switchUiMode(mode) },
                                        onNextWaveColor = { viewModel.cycleWaveColor() },
                                        onOpenUpdate = { viewModel.openSystemUpdate() },
                                        onOpenCamera = { viewModel.openCameraUtility() },
                                        onOpenFileManager = { viewModel.openFileManager() },
                                        onRenameGame = { id, name -> viewModel.renameGame(id, name) },
                                        onRemoveGame = { id -> viewModel.removeGame(id) },
                                        onUpdateGameCover = { id, cover -> viewModel.updateGameCover(id, cover) }
                                    )
                                } else {
                                    PpssppGridScreen(
                                        config = uiState.config,
                                        games = uiState.games,
                                        soundEngine = soundEngine,
                                        onLaunchGame = { game -> viewModel.launchGame(game) },
                                        onOpenSettings = { tab -> viewModel.openSettings(tab) },
                                        onOpenIsoPicker = { isoPickerLauncher.launch("*/*") },
                                        onOpenPs1Picker = { ps1PickerLauncher.launch("*/*") },
                                        onSwitchUiMode = { mode -> viewModel.switchUiMode(mode) },
                                        onOpenUpdate = { viewModel.openSystemUpdate() },
                                        onOpenCamera = { viewModel.openCameraUtility() },
                                        onOpenFileManager = { viewModel.openFileManager() }
                                    )
                                }
                            }

                            EmulatorScreen.IN_GAME -> {
                                uiState.activeGame?.let { game ->
                                    PspGameScreen(
                                        game = game,
                                        config = uiState.config,
                                        soundEngine = soundEngine,
                                        onExitGame = { viewModel.exitGame() },
                                        onOpenSettings = { tab -> viewModel.openSettings(tab) }
                                    )
                                } ?: run {
                                    viewModel.exitGame()
                                }
                            }

                            EmulatorScreen.SETTINGS -> {
                                PspSettingsScreen(
                                    config = uiState.config,
                                    games = uiState.games,
                                    soundEngine = soundEngine,
                                    initialTab = uiState.settingsInitialTab,
                                    onConfigChange = { newConfig -> viewModel.updateConfig(newConfig) },
                                    onBack = { viewModel.closeSettings() },
                                    onOpenUpdate = { viewModel.openSystemUpdate() },
                                    onOpenFileManager = { viewModel.openFileManager() }
                                )
                            }

                            EmulatorScreen.SYSTEM_UPDATE -> {
                                PspSystemUpdateScreen(
                                    config = uiState.config,
                                    soundEngine = soundEngine,
                                    onBack = { viewModel.closeSystemUpdate() },
                                    onUpdateComplete = { newVer ->
                                        viewModel.applyFirmwareUpdate(newVer)
                                    }
                                )
                            }

                            EmulatorScreen.FILE_MANAGER -> {
                                PspFileManagerScreen(
                                    soundEngine = soundEngine,
                                    onBack = { viewModel.closeFileManager() },
                                    onLaunchGameOrFile = { fileName ->
                                        // Auto-load game if it is an ISO or BIN
                                        if (fileName.endsWith(".iso", true) || fileName.endsWith(".cso", true)) {
                                            viewModel.importCustomGame(android.net.Uri.EMPTY, fileName)
                                        }
                                    }
                                )
                            }

                            EmulatorScreen.CAMERA_UTILITY -> {
                                PspCameraUtilityScreen(
                                    config = uiState.config,
                                    soundEngine = soundEngine,
                                    onBack = { viewModel.closeCameraUtility() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
