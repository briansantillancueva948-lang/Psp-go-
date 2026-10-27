package com.example.model

enum class ConsolePlatform(val displayName: String, val badge: String) {
    PSP("PlayStation Portable", "PSP UMD"),
    PS1("PlayStation (PS1 Classic)", "PS1 POPS")
}

enum class GameType {
    RIDGE_RACER,
    GOD_OF_OLYMPUS,
    CRISIS_CORE,
    BENCHMARK_3D,
    PS1_CRASH,
    PS1_TEKKEN,
    CUSTOM_ISO
}

data class CheatCode(
    val id: String,
    val title: String,
    val description: String,
    var enabled: Boolean = false,
    val cwcheatCode: String
)

data class SaveStateSlot(
    val slotNumber: Int,
    val timestamp: Long? = null,
    val gameTitle: String = "",
    val playTimeSeconds: Long = 0,
    val description: String = "Vacío"
)

data class PspGame(
    val id: String,
    val discId: String,
    val title: String,
    val subtitle: String,
    val type: GameType,
    val platform: ConsolePlatform = ConsolePlatform.PSP,
    val format: String = "ISO", // ISO, CSO, BIN/CUE, EBOOT.PBP
    val region: String = "USA / NTSC",
    val sizeText: String = "1.2 GB",
    val drawableResName: String? = null,
    val backdropResName: String? = null,
    val customCoverUri: String? = null, // URI from Photo Picker (PNG/JPG/GIF)
    val headerInfo: String = "UMD-DATA.BIN [PSP_GAME]",
    val cheats: List<CheatCode> = emptyList(),
    val customFilePath: String? = null
)

object SamplePspGames {
    val BUILT_IN_GAMES = listOf(
        // PSP Games
        PspGame(
            id = "tekken_6_psp",
            discId = "NPJH-50701",
            title = "Tekken 6 [PSP]",
            subtitle = "The Bloodline Rebellion • PIC1.PNG HD",
            type = GameType.PS1_TEKKEN,
            platform = ConsolePlatform.PSP,
            format = "ISO",
            region = "USA / NTSC",
            sizeText = "812 MB (ISO)",
            drawableResName = "psp_pic_tekken6_1793140411506",
            backdropResName = "psp_pic_tekken6_1793140411506",
            headerInfo = "BANDAI NAMCO • ELECTRIC GOD FIST",
            cheats = listOf(
                CheatCode("t6_1", "Rage Mode Permanente", "_C1 Infinite Rage Mode\n_L 0x0052B100 0x00000001", true, "_C1 Infinite Rage Mode"),
                CheatCode("t6_2", "Desbloquear Todos los Trajes", "_C1 Unlock All Outfits\n_L 0x0052B104 0xFFFFFFFF", false, "_C1 Unlock All Outfits")
            )
        ),
        PspGame(
            id = "ridge_racer",
            discId = "ULUS-10001",
            title = "Ridge Racer PSP",
            subtitle = "Turbo Drift 3D • 60 FPS",
            type = GameType.RIDGE_RACER,
            platform = ConsolePlatform.PSP,
            format = "CSO",
            region = "USA",
            sizeText = "866 MB (CSO)",
            drawableResName = "psp_ridge_cover_1793136775170",
            headerInfo = "NAMCO SYSTEM PSP • HIGH SPEED ENGINE",
            cheats = listOf(
                CheatCode("c1", "Nitro Infinito", "_C1 Infinite Nitrous\n_L 0x0045B120 0x00000003", true, "_C1 Infinite Nitrous"),
                CheatCode("c2", "Desbloquear Todas las Pistas", "_C1 Unlock All Tracks\n_L 0x0045B130 0xFFFFFFFF", false, "_C1 Unlock All Tracks")
            )
        ),
        PspGame(
            id = "god_of_olympus",
            discId = "UCUS-98653",
            title = "God of Olympus",
            subtitle = "Chains of Sparta • Hack 'n Slash",
            type = GameType.GOD_OF_OLYMPUS,
            platform = ConsolePlatform.PSP,
            format = "ISO",
            region = "EUR",
            sizeText = "1.42 GB (ISO)",
            drawableResName = "psp_wave_bg_1793136763810",
            headerInfo = "SANTA MONICA STUDIOS • BLADES COMBAT",
            cheats = listOf(
                CheatCode("c4", "Vida Infinita (Dios Inmortal)", "_C1 Infinite Health\n_L 0x0052A100 0x000003E8", false, "_C1 Infinite Health"),
                CheatCode("c5", "Furia de Esparta Máxima", "_C1 Max Spartan Rage\n_L 0x0052A108 0x000003E8", true, "_C1 Max Spartan Rage")
            )
        ),
        PspGame(
            id = "crisis_core",
            discId = "ULUS-10336",
            title = "Crisis Core",
            subtitle = "Final Buster • DMW Tactical JRPG",
            type = GameType.CRISIS_CORE,
            platform = ConsolePlatform.PSP,
            format = "CSO",
            region = "USA",
            sizeText = "1.58 GB (CSO)",
            drawableResName = "ic_psp_logo_1793136675294",
            headerInfo = "SQUARE ENIX • SOLDIER 1st CLASS",
            cheats = listOf(
                CheatCode("c7", "DMW Limit Break Permanente", "_C1 Constant Limit Break\n_L 0x00612A04 0x00000007", true, "_C1 Constant Limit Break")
            )
        ),
        // PS1 on PSP Classics (POPS Emulator)
        PspGame(
            id = "ps1_crash_3",
            discId = "SCES-01420",
            title = "Crash Bandicoot 3: Warped",
            subtitle = "PS1 Classic on PSP • EBOOT.PBP",
            type = GameType.PS1_CRASH,
            platform = ConsolePlatform.PS1,
            format = "BIN/CUE (PBP)",
            region = "PAL / EUR",
            sizeText = "472 MB (POPS)",
            drawableResName = "ps1_cover_crash_1793138708025",
            headerInfo = "SONY INTERACTIVE • NAUGHTY DOG POPS",
            cheats = listOf(
                CheatCode("c10", "99 Vidas Máximas", "_C1 99 Lives\n_L 0x8006927C 0x0063", true, "_C1 99 Lives"),
                CheatCode("c11", "Aku Aku Permanente", "_C1 Invincible Aku Aku\n_L 0x80069280 0x0002", false, "_C1 Invincible Aku Aku")
            )
        ),
        PspGame(
            id = "ps1_tekken_3",
            discId = "SCES-01237",
            title = "Tekken 3",
            subtitle = "PS1 Classic on PSP • BIN/ISO",
            type = GameType.PS1_TEKKEN,
            platform = ConsolePlatform.PS1,
            format = "BIN / ISO",
            region = "PAL / EUR",
            sizeText = "540 MB (BIN)",
            drawableResName = "ps1_cover_tekken_1793138721941",
            headerInfo = "NAMCO SYSTEM 12 • PS1 CLASSIC EMULATOR",
            cheats = listOf(
                CheatCode("c12", "Desbloquear Todos los Luchadores", "_C1 Unlock All Characters\n_L 0x800AA8C0 0xFFFF", true, "_C1 Unlock All Characters"),
                CheatCode("c13", "Modo Tekken Ball Activo", "_C1 Tekken Ball Mode\n_L 0x800AA8D0 0x0001", false, "_C1 Tekken Ball Mode")
            )
        ),
        // Homebrew
        PspGame(
            id = "psp_benchmark",
            discId = "HOME-00001",
            title = "PSP 3D Benchmark",
            subtitle = "Homebrew • PolyEngine & Shaders",
            type = GameType.BENCHMARK_3D,
            platform = ConsolePlatform.PSP,
            format = "PBP",
            region = "REGION FREE",
            sizeText = "48 MB (PBP)",
            drawableResName = "ic_psp_logo_1793136675294",
            headerInfo = "HOMEBREW COMMUNITY • VULKAN PIPELINE",
            cheats = emptyList()
        )
    )
}
