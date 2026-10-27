package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.model.EmulatorConfig
import com.example.model.GameType
import com.example.model.PspGame
import com.example.model.SamplePspGames
import com.example.model.UiMode
import com.example.model.XmbWaveColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class EmulatorScreen {
    BOOT_INTRO,
    MAIN_MENU,
    IN_GAME,
    SETTINGS,
    SYSTEM_UPDATE,
    FILE_MANAGER,
    CAMERA_UTILITY
}

data class EmulatorUiState(
    val currentScreen: EmulatorScreen = EmulatorScreen.BOOT_INTRO,
    val config: EmulatorConfig = EmulatorConfig(),
    val games: List<PspGame> = SamplePspGames.BUILT_IN_GAMES,
    val activeGame: PspGame? = null,
    val settingsInitialTab: Int = 0,
    val returnScreenAfterSettings: EmulatorScreen = EmulatorScreen.MAIN_MENU,
    val pspStoragePath: String = ""
)

class EmulatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EmulatorUiState())
    val uiState: StateFlow<EmulatorUiState> = _uiState.asStateFlow()

    fun finishBoot() {
        _uiState.update { it.copy(currentScreen = EmulatorScreen.MAIN_MENU) }
    }

    fun replayBoot() {
        _uiState.update { it.copy(currentScreen = EmulatorScreen.BOOT_INTRO) }
    }

    fun openSystemUpdate() {
        _uiState.update { it.copy(currentScreen = EmulatorScreen.SYSTEM_UPDATE) }
    }

    fun closeSystemUpdate() {
        _uiState.update { it.copy(currentScreen = EmulatorScreen.MAIN_MENU) }
    }

    fun openFileManager() {
        _uiState.update { it.copy(currentScreen = EmulatorScreen.FILE_MANAGER) }
    }

    fun closeFileManager() {
        _uiState.update { it.copy(currentScreen = EmulatorScreen.MAIN_MENU) }
    }

    fun openCameraUtility() {
        _uiState.update { it.copy(currentScreen = EmulatorScreen.CAMERA_UTILITY) }
    }

    fun closeCameraUtility() {
        _uiState.update { it.copy(currentScreen = EmulatorScreen.MAIN_MENU) }
    }

    fun applyFirmwareUpdate(newVersion: String = "6.61 PRO-C∞") {
        _uiState.update {
            it.copy(
                config = it.config.copy(systemFirmware = newVersion),
                currentScreen = EmulatorScreen.BOOT_INTRO // Replay authentic boot upon update completion!
            )
        }
    }

    fun launchGame(game: PspGame) {
        _uiState.update {
            it.copy(
                activeGame = game,
                currentScreen = EmulatorScreen.IN_GAME
            )
        }
    }

    fun exitGame() {
        _uiState.update {
            it.copy(
                activeGame = null,
                currentScreen = EmulatorScreen.MAIN_MENU
            )
        }
    }

    fun openSettings(tab: Int = 0) {
        _uiState.update {
            it.copy(
                settingsInitialTab = tab,
                returnScreenAfterSettings = it.currentScreen,
                currentScreen = EmulatorScreen.SETTINGS
            )
        }
    }

    fun closeSettings() {
        _uiState.update {
            it.copy(
                currentScreen = it.returnScreenAfterSettings
            )
        }
    }

    fun updateConfig(newConfig: EmulatorConfig) {
        _uiState.update { it.copy(config = newConfig) }
    }

    fun switchUiMode(mode: UiMode) {
        _uiState.update {
            it.copy(config = it.config.copy(uiMode = mode))
        }
    }

    fun cycleWaveColor() {
        val colors = XmbWaveColor.values()
        val nextIdx = (colors.indexOf(_uiState.value.config.waveColor) + 1) % colors.size
        _uiState.update {
            it.copy(config = it.config.copy(waveColor = colors[nextIdx]))
        }
    }

    fun importCustomGame(uri: Uri, filename: String) {
        val cleanName = filename.substringBeforeLast(".").replace("_", " ")
        val ext = filename.substringAfterLast(".", "ISO").uppercase()
        val autoOriginalCover = com.example.model.CoverArtDatabase.resolveOriginalCover(filename)
        val newGame = PspGame(
            id = "custom_${System.currentTimeMillis()}",
            discId = "ULES-${(10000..99999).random()}",
            title = cleanName,
            subtitle = "PSP UMD • $ext Disc",
            type = GameType.CUSTOM_ISO,
            platform = com.example.model.ConsolePlatform.PSP,
            format = ext,
            region = "USA / NTSC",
            sizeText = "1.2 GB ($ext)",
            drawableResName = autoOriginalCover,
            customFilePath = uri.toString()
        )
        _uiState.update { state ->
            state.copy(
                games = listOf(newGame) + state.games,
                activeGame = newGame,
                currentScreen = EmulatorScreen.IN_GAME
            )
        }
    }

    fun renameGame(gameId: String, newName: String) {
        _uiState.update { state ->
            val updatedGames = state.games.map { g ->
                if (g.id == gameId) g.copy(title = newName) else g
            }
            state.copy(games = updatedGames)
        }
    }

    fun removeGame(gameId: String) {
        _uiState.update { state ->
            val updatedGames = state.games.filterNot { it.id == gameId }
            state.copy(games = updatedGames)
        }
    }

    fun updateGameCover(gameId: String, coverUriOrRes: String) {
        _uiState.update { state ->
            val updatedGames = state.games.map { g ->
                if (g.id == gameId) {
                    if (coverUriOrRes.startsWith("content://") || coverUriOrRes.startsWith("file://") || coverUriOrRes.startsWith("http")) {
                        g.copy(customCoverUri = coverUriOrRes)
                    } else {
                        g.copy(drawableResName = coverUriOrRes, customCoverUri = null)
                    }
                } else g
            }
            state.copy(games = updatedGames)
        }
    }

    fun setLanguage(lang: com.example.model.AppLanguage) {
        _uiState.update {
            it.copy(config = it.config.copy(appLanguage = lang, language = lang.displayName))
        }
    }

    fun importPs1Game(uri: Uri, filename: String) {
        val cleanName = filename.substringBeforeLast(".").replace("_", " ")
        val ext = filename.substringAfterLast(".", "BIN").uppercase()
        val autoOriginalCover = com.example.model.CoverArtDatabase.resolveOriginalCover(filename)
        val newGame = PspGame(
            id = "ps1_${System.currentTimeMillis()}",
            discId = "SLUS-${(10000..99999).random()}",
            title = cleanName,
            subtitle = "PS1 Classic • $ext Disc",
            type = if (cleanName.contains("Tekken", ignoreCase = true)) GameType.PS1_TEKKEN else GameType.PS1_CRASH,
            platform = com.example.model.ConsolePlatform.PS1,
            format = ext,
            region = "USA / NTSC",
            sizeText = "520 MB ($ext)",
            drawableResName = autoOriginalCover,
            customFilePath = uri.toString()
        )
        _uiState.update { state ->
            state.copy(
                games = listOf(newGame) + state.games,
                activeGame = newGame,
                currentScreen = EmulatorScreen.IN_GAME
            )
        }
    }
}
