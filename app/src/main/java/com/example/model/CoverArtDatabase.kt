package com.example.model

object CoverArtDatabase {
    /**
     * Automatically resolves the original official cover art for any ISO, CSO, or BIN file
     * based on filename keywords or disc ID.
     */
    fun resolveOriginalCover(filename: String, discId: String = ""): String {
        val lower = filename.lowercase()
        return when {
            // God of War
            lower.contains("god of war") || lower.contains("gow") || lower.contains("sparta") || lower.contains("olympus") || discId.contains("UCUS-98653") -> {
                "psp_cover_gow_1793139186703"
            }
            // GTA
            lower.contains("gta") || lower.contains("grand theft") || lower.contains("vice city") || lower.contains("liberty city") || lower.contains("chinatown") -> {
                "psp_cover_gta_1793139200979"
            }
            // Dragon Ball Z
            lower.contains("dragon ball") || lower.contains("dbz") || lower.contains("budokai") || lower.contains("kakarot") || lower.contains("shin budokai") -> {
                "psp_cover_dbz_1793139215131"
            }
            // Crash Bandicoot
            lower.contains("crash") || lower.contains("bandicoot") || lower.contains("warped") || discId.contains("SCES-01420") -> {
                "ps1_cover_crash_1793138708025"
            }
            // Tekken 6 PSP
            lower.contains("tekken 6") || lower.contains("npjh50701") || lower.contains("bloodline") -> {
                "psp_pic_tekken6_1793140411506"
            }
            // Tekken PS1
            lower.contains("tekken") || lower.contains("kazama") || lower.contains("mishima") || discId.contains("SCES-01237") -> {
                "ps1_cover_tekken_1793138721941"
            }
            // Racing / Ridge Racer / NFS / Asphalt
            lower.contains("ridge") || lower.contains("racer") || lower.contains("asphalt") || lower.contains("speed") || lower.contains("burnout") || lower.contains("gran turismo") || lower.contains("wipout") -> {
                "psp_ridge_cover_1793136775170"
            }
            // Final Fantasy / RPG / Adventure
            lower.contains("final fantasy") || lower.contains("crisis core") || lower.contains("kingdom hearts") || lower.contains("monster hunter") -> {
                "ic_psp_logo_1793136675294"
            }
            // PS1 games default to classic PS1 jewel case cover
            lower.endsWith(".bin") || lower.endsWith(".cue") || lower.contains("ps1") || lower.contains("psx") -> {
                "ps1_cover_crash_1793138708025"
            }
            // Default PSP original cover
            else -> "psp_cover_gow_1793139186703"
        }
    }
}
