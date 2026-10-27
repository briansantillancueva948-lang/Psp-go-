package com.example.model

enum class RenderingBackend(val displayName: String, val techName: String) {
    VULKAN("Vulkan (Recomendado)", "Vulkan 1.3"),
    OPENGL("OpenGL ES", "GLES 3.2"),
    SOFTWARE("Software", "CPU Direct")
}

enum class ResolutionScale(val multiplier: Int, val label: String, val resolution: String) {
    X1(1, "1x PSP", "480 × 272"),
    X2(2, "2x PSP (HD)", "960 × 544"),
    X3(3, "3x PSP (FHD)", "1440 × 816"),
    X4(4, "4x PSP (QHD)", "1920 × 1088"),
    X5(5, "5x PSP (4K)", "2400 × 1360")
}

enum class FrameSkip(val label: String, val skipFrames: Int) {
    OFF("Apagado (Fluido)", 0),
    SKIP_1("1 Cuadro", 1),
    SKIP_2("2 Cuadros", 2),
    AUTO("Automático (Adaptive)", -1)
}

enum class PostProcessingShader(val label: String, val desc: String) {
    NONE("Ninguno", "Renderizado original"),
    CRT_SCANLINES("CRT Scanlines", "Líneas de barrido retro TV"),
    VIBRANT("Colores Vibrantes", "Saturación y contraste PSP OLED"),
    FXAA("FXAA Suave", "Antialiasing ultra rápido"),
    RETRO_AMBER("Retro Amber", "Tono fósforo clásico"),
    MONOCHROME("Escala de Grises", "Monocromo cinematográfico")
}

enum class TextureFilter(val label: String) {
    AUTO("Automático"),
    LINEAR("Lineal"),
    NEAREST("Más Cercano (Pixel Art)"),
    ANISOTROPIC_16X("Anisotrópico 16x (Máxima Calidad)")
}

enum class PspModel(val label: String, val memoryRam: String) {
    PSP_1000("PSP-1000 (Fat)", "32 MB RAM"),
    PSP_2000("PSP-2000 (Slim & Lite)", "64 MB RAM"),
    PSP_3000("PSP-3000 (Brite Display)", "64 MB RAM"),
    PSP_GO("PSP Go (N1000)", "64 MB RAM")
}

enum class PspFirmwareVersion(val label: String, val desc: String, val isCfw: Boolean) {
    FW_661_PROC("6.61 PRO-C∞", "CFW de máxima compatibilidad con POPSloader", true),
    FW_660_ME("6.60 ME-2.3", "Minimal Edition de neur0n (Kernel 1.50)", true),
    FW_661_OFW("6.61 Oficial", "Firmware oficial Sony Computer Entertainment", false),
    FW_500_M33("5.00 M33-6", "Firmware legendario de Dark_AleX", true),
    FW_150_KERNEL("1.50 Kernel Addon", "Modo kernel histórico para homebrew", true)
}

enum class CpuClock(val mhz: Int, val label: String) {
    MHZ_222(222, "222 MHz (Stock Original)"),
    MHZ_266(266, "266 MHz (Balanceado)"),
    MHZ_333(333, "333 MHz (Overclock Oficial)"),
    MHZ_444(444, "444 MHz (Turbo Extreme)")
}

enum class UiMode(val label: String) {
    XMB("PSP XMB Original"),
    PPSSPP_GRID("PPSSPP Clásico")
}

enum class DisplayLayout(val label: String) {
    FULLSCREEN_OVERLAY("Pantalla Completa (Táctil)"),
    PSP_CONSOLE_FRAME("Modo Consola PSP Físico")
}

enum class XmbWaveColor(val label: String, val primaryHex: Long, val secondaryHex: Long) {
    CLASSIC_BLUE("Azul Clásico PSP", 0xFF003882, 0xFF00B0FF),
    ROYAL_PURPLE("Púrpura Neón", 0xFF2A0845, 0xFF9B51E0),
    CRIMSON_RED("Rojo Carmesí", 0xFF6B0B18, 0xFFFF3366),
    EMERALD_GREEN("Verde Esmeralda", 0xFF084523, 0xFF00E676),
    GOLDEN_SUN("Dorado Sunset", 0xFF664400, 0xFFFFB74D),
    MIDNIGHT_BLACK("Negro Medianoche", 0xFF0F141C, 0xFF4A6572)
}

enum class ControllerSkin(val label: String) {
    PSP_OFFICIAL_BLACK("PSP Negro Oficial"),
    PSP_SILVER("PSP Plata Metálico"),
    PSP_CERAMIC_WHITE("PSP Blanco Cerámica"),
    NEON_CYAN("Neón Cyan Glow")
}

data class EmulatorConfig(
    // Gráficos
    val backend: RenderingBackend = RenderingBackend.VULKAN,
    val resolutionScale: ResolutionScale = ResolutionScale.X2,
    val frameSkip: FrameSkip = FrameSkip.OFF,
    val targetFps: Int = 60,
    val turboSpeedMultiplier: Float = 2.0f,
    val shader: PostProcessingShader = PostProcessingShader.VIBRANT,
    val textureFilter: TextureFilter = TextureFilter.AUTO,
    val hardwareTransform: Boolean = true,
    val fastMemory: Boolean = true,
    val showFpsCounter: Boolean = true,
    val showSpeedPercentage: Boolean = true,
    val showRamViewer: Boolean = true,
    val showVramViewer: Boolean = true,
    val vsync: Boolean = true,

    // Audio
    val masterVolume: Float = 1.0f,
    val soundEffectsVolume: Float = 1.0f,
    val audioLatencyMs: Int = 64,
    val audioSync: Boolean = true,
    val pspReverb: Boolean = true,

    // Controles PSP
    val showTouchControls: Boolean = true,
    val buttonOpacity: Float = 0.75f,
    val buttonScale: Float = 1.0f,
    val controllerSkin: ControllerSkin = ControllerSkin.PSP_OFFICIAL_BLACK,
    val hapticFeedback: Boolean = true,
    val hapticIntensity: Float = 1.0f,
    val analogDeadzone: Float = 0.15f,
    val analogSensitivity: Float = 1.0f,
    val showTurboButton: Boolean = true,
    val swapConfirmButton: Boolean = false, // false: Cross is enter, true: Circle is enter
    val dpadDiagonalEnabled: Boolean = true,

    // Sistema & Multi-idioma
    val pspModel: PspModel = PspModel.PSP_3000,
    val cpuClock: CpuClock = CpuClock.MHZ_333,
    val username: String = "Player1",
    val appLanguage: AppLanguage = AppLanguage.SPANISH,
    val language: String = "Español",
    val uiMode: UiMode = UiMode.XMB,
    val displayLayout: DisplayLayout = DisplayLayout.FULLSCREEN_OVERLAY,
    val waveColor: XmbWaveColor = XmbWaveColor.CLASSIC_BLUE,
    val showOriginalIntroOnLaunch: Boolean = true,
    val firmware: PspFirmwareVersion = PspFirmwareVersion.FW_661_PROC,
    val systemFirmware: String = "6.61 PRO-C∞"
)
