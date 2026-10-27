package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PspSoundEngine
import com.example.model.ConsolePlatform
import com.example.model.EmulatorConfig
import com.example.model.GameType
import com.example.model.PspGame
import com.example.model.UiMode

@Composable
fun PpssppGridScreen(
    config: EmulatorConfig,
    games: List<PspGame>,
    soundEngine: PspSoundEngine,
    onLaunchGame: (PspGame) -> Unit,
    onOpenSettings: (tab: Int) -> Unit,
    onOpenIsoPicker: () -> Unit,
    onOpenPs1Picker: () -> Unit = {},
    onSwitchUiMode: (UiMode) -> Unit,
    onOpenUpdate: () -> Unit = {},
    onOpenFileManager: () -> Unit = {},
    onOpenCamera: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Recientes", "Juegos PSP", "Juegos PS1", "Homebrew")

    val filteredGames = remember(selectedTab, games) {
        when (selectedTab) {
            0 -> games.take(4) // Recientes
            1 -> games.filter { it.platform == ConsolePlatform.PSP && it.type != GameType.BENCHMARK_3D }
            2 -> games.filter { it.platform == ConsolePlatform.PS1 }
            3 -> games.filter { it.type == GameType.BENCHMARK_3D || it.id == "crisis_core" }
            else -> games
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141923))
            .testTag("ppsspp_grid_screen")
    ) {
        // Main Content Area (Tabs & Grid)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            // Header: PPSSPP Title & Version
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PPSSPP",
                    color = Color(0xFF29B6F6),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "v1.17.1-Ultra • Lanzador PSP + PS1 (${config.backend.techName})",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF29B6F6),
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            soundEngine.playClick()
                            selectedTab = index
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFF29B6F6) else Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Games Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 12.dp)
            ) {
                items(filteredGames) { game ->
                    PpssppGameCard(
                        game = game,
                        onClick = {
                            soundEngine.playConfirm()
                            onLaunchGame(game)
                        }
                    )
                }
            }
        }

        // Right Sidebar Panel
        Column(
            modifier = Modifier
                .width(190.dp)
                .fillMaxHeight()
                .background(Color(0xFF0F1218))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(0.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "LANZADOR",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            PpssppSidebarButton(
                label = "Cargar PSP (ISO/CSO)",
                icon = Icons.Default.FileOpen,
                tag = "sidebar_load_iso"
            ) {
                soundEngine.playClick()
                onOpenIsoPicker()
            }

            PpssppSidebarButton(
                label = "Cargar PS1 (BIN/ISO)",
                icon = Icons.Default.Gamepad,
                tag = "sidebar_load_ps1",
                highlight = true
            ) {
                soundEngine.playConfirm()
                onOpenPs1Picker()
            }

            PpssppSidebarButton(
                label = "Ajustes",
                icon = Icons.Default.Settings,
                tag = "sidebar_settings"
            ) {
                soundEngine.playClick()
                onOpenSettings(0)
            }

            PpssppSidebarButton(
                label = "Modo PSP XMB",
                icon = Icons.Default.ViewCarousel,
                tag = "sidebar_xmb_mode"
            ) {
                soundEngine.playConfirm()
                onSwitchUiMode(UiMode.XMB)
            }

            PpssppSidebarButton(
                label = "Trucos (CWCheat)",
                icon = Icons.Default.Code,
                tag = "sidebar_cheats"
            ) {
                soundEngine.playClick()
                onOpenSettings(4)
            }

            PpssppSidebarButton(
                label = "Explorador PSP",
                icon = Icons.Default.Folder,
                tag = "sidebar_file_manager"
            ) {
                soundEngine.playClick()
                onOpenFileManager()
            }

            PpssppSidebarButton(
                label = "Cámara Go!Cam",
                icon = Icons.Default.CameraAlt,
                tag = "sidebar_camera"
            ) {
                soundEngine.playConfirm()
                onOpenCamera()
            }

            PpssppSidebarButton(
                label = "Buscar Actualizaciones",
                icon = Icons.Default.SystemUpdate,
                tag = "sidebar_update",
                highlight = true
            ) {
                soundEngine.playConfirm()
                onOpenUpdate()
            }

            Spacer(modifier = Modifier.weight(1f))

            // System specs box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "MOTOR POPS & PSP",
                        color = Color.Cyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${config.backend.techName}\nRes: ${config.resolutionScale.label}\nCPU: ${config.cpuClock.mhz} MHz",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PpssppGameCard(
    game: PspGame,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("ppsspp_card_${game.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2530)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Art banner / thumbnail using GameCoverBox
            GameCoverBox(
                game = game,
                isSelected = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = game.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (game.platform == ConsolePlatform.PS1) Color(0xFFD50000)
                            else Color(0xFF003882)
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
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${game.format} • ${game.sizeText}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun PpssppSidebarButton(
    label: String,
    icon: ImageVector,
    tag: String,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (highlight) Color(0xFF0288D1).copy(alpha = 0.35f)
                else Color.White.copy(alpha = 0.07f)
            )
            .border(
                1.dp,
                if (highlight) Color.Cyan.copy(alpha = 0.6f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (highlight) Color.Cyan else Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium
        )
    }
}
