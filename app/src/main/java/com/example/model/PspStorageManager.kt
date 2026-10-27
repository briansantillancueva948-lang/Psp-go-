package com.example.model

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PspStorageItem(
    val name: String,
    val relativePath: String,
    val isDirectory: Boolean,
    val itemCount: Int = 0,
    val dateText: String = "",
    val sizeText: String = ""
)

object PspStorageManager {

    val REQUIRED_FOLDERS = listOf(
        "Cheats",
        "GAME",
        "PLUGINS",
        "PPSSPP_STATE",
        "SAVEDATA",
        "SCREENSHOT",
        "SYSTEM",
        "TEXTURES",
        "PHOTO",
        "MUSIC",
        "VIDEO"
    )

    val SAMPLE_GAME_IDS = listOf(
        "NPEG00044" to "WipEout Pure [PSP]",
        "NPEW00072" to "PixelJunk Monsters [PSP]",
        "NPHG00092" to "Invizimals Shadow Zone [PSP]",
        "NPJH00134" to "Monster Hunter Portable 3rd",
        "NPJH50043" to "God of War: Ghost of Sparta",
        "NPJH50044" to "God of War: Chains of Olympus",
        "NPJH50332" to "Kingdom Hearts: Birth by Sleep",
        "NPJH50343" to "Final Fantasy Type-0",
        "NPJH50352" to "Danganronpa: Trigger Happy Havoc",
        "NPJH50453" to "Grand Theft Auto: Vice City Stories",
        "NPJH50468" to "Dragon Ball Z: Tenkaichi Tag Team",
        "NPJH50701" to "Tekken 6 [PSP]",
        "NPJH50721" to "Persona 3 Portable",
        "NPJH50789" to "Dissidia 012 Duodecim",
        "NPJH50832" to "Ridge Racer [PSP]",
        "NPJH50878" to "Naruto Shippuden: Ultimate Ninja",
        "UPDATE" to "Actualización del sistema PSP™ 6.61"
    )

    fun getPspRootDir(context: Context): File {
        // Priority: Context external files / Pictures or external files dir
        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: context.getExternalFilesDir(null)
            ?: context.filesDir
        val pspDir = File(baseDir, "PSP")
        if (!pspDir.exists()) {
            pspDir.mkdirs()
        }
        return pspDir
    }

    /**
     * Initializes and creates the authentic Sony PSP / PPSSPP folder layout
     * matching the exact hierarchy shown in the user's ZArchiver screenshot.
     */
    fun initializePspFileSystem(context: Context): File {
        val root = getPspRootDir(context)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = dateFormat.format(Date())

        // 1. Create main folders
        for (folder in REQUIRED_FOLDERS) {
            val dir = File(root, folder)
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }

        // 2. Populate GAME directory with disc IDs from screenshot
        val gameDir = File(root, "GAME")
        for ((gameId, desc) in SAMPLE_GAME_IDS) {
            val gDir = File(gameDir, gameId)
            if (!gDir.exists()) {
                gDir.mkdirs()
            }
            // Add authentic PARAM.SFO and EBOOT.PBP marker files
            val sfoFile = File(gDir, "PARAM.SFO")
            if (!sfoFile.exists()) {
                sfoFile.writeText("PSPSFO_v1.1\nTITLE=$desc\nDISC_ID=$gameId\nCATEGORY=UG\n")
            }
            val pbpFile = File(gDir, "EBOOT.PBP")
            if (!pbpFile.exists()) {
                pbpFile.writeBytes(byteArrayOf(0x00, 0x50, 0x42, 0x50)) // \0PBP header magic
            }
        }

        // 3. Populate SYSTEM directory with ppsspp.ini and controls.ini
        val systemDir = File(root, "SYSTEM")
        val ppssppIni = File(systemDir, "ppsspp.ini")
        if (!ppssppIni.exists()) {
            ppssppIni.writeText(
                """
                [General]
                FirstRun = false
                Version = 1.19.0
                CurrentDirectory = /PSP/GAME
                Language = es_ES
                
                [Graphics]
                RenderingBackend = 0
                Resolution = 2
                FrameSkip = 0
                ShowFPS = true
                ShowRAM = true
                Vulkan = true
                
                [System]
                PSPModel = 1
                CpuClock = 333
                FastMemory = true
                """.trimIndent()
            )
        }
        val controlsIni = File(systemDir, "controls.ini")
        if (!controlsIni.exists()) {
            controlsIni.writeText("[ControlMappings]\nTriangle=button_triangle\nCircle=button_circle\nCross=button_cross\nSquare=button_square\n")
        }

        // 4. Populate Cheats with initial cheat files
        val cheatsDir = File(root, "Cheats")
        val cheatFile = File(cheatsDir, "ULES00123.ini")
        if (!cheatFile.exists()) {
            cheatFile.writeText("_S ULES-00123\n_G Ridge Racer\n_C1 Desbloquear Todos los Circuitos\n_L 0x2004A810 0x00000001\n")
        }

        // 5. Populate SAVEDATA
        val savedataDir = File(root, "SAVEDATA")
        val sampleSave = File(savedataDir, "ULES001230000")
        if (!sampleSave.exists()) {
            sampleSave.mkdirs()
            File(sampleSave, "PARAM.SFO").writeText("TITLE=Ridge Racer Save Data\nSAVEDATA_VERSION=1.00\n")
            File(sampleSave, "SECURE.BIN").writeBytes(byteArrayOf(0x53, 0x41, 0x56, 0x45))
        }

        return root
    }

    /**
     * Reads a directory and returns sorted storage items for the file manager.
     */
    fun listDirectoryItems(context: Context, relativePath: String = ""): List<PspStorageItem> {
        val root = getPspRootDir(context)
        val targetDir = if (relativePath.isEmpty()) root else File(root, relativePath)
        if (!targetDir.exists() || !targetDir.isDirectory) return emptyList()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val files = targetDir.listFiles() ?: return emptyList()

        return files.map { file ->
            val isDir = file.isDirectory
            val itemCount = if (isDir) (file.listFiles()?.size ?: 0) else 0
            val sizeText = if (isDir) {
                if (itemCount == 0) "Vacío" else "$itemCount elementos"
            } else {
                formatFileSize(file.length())
            }
            PspStorageItem(
                name = file.name,
                relativePath = file.relativeTo(root).path,
                isDirectory = isDir,
                itemCount = itemCount,
                dateText = dateFormat.format(Date(file.lastModified())),
                sizeText = sizeText
            )
        }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    fun createFolder(context: Context, parentRelativePath: String, folderName: String): Boolean {
        val root = getPspRootDir(context)
        val parent = if (parentRelativePath.isEmpty()) root else File(root, parentRelativePath)
        val newFolder = File(parent, folderName.trim())
        return if (!newFolder.exists()) newFolder.mkdirs() else false
    }

    fun savePhoto(context: Context, imageBytes: ByteArray, prefix: String = "PSP_PHOTO"): File {
        val root = getPspRootDir(context)
        val photoDir = File(root, "PHOTO")
        if (!photoDir.exists()) photoDir.mkdirs()

        val filename = "${prefix}_${System.currentTimeMillis()}.jpg"
        val target = File(photoDir, filename)
        target.writeBytes(imageBytes)

        // Also duplicate to SCREENSHOT for game album integration
        val screenDir = File(root, "SCREENSHOT")
        if (!screenDir.exists()) screenDir.mkdirs()
        File(screenDir, filename).writeBytes(imageBytes)

        return target
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
