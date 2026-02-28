package com.pegasus.artwork.data.local

object SystemIdMapping {

    data class SystemInfo(
        val id: Int,
        val displayName: String,
    )

    private val mapping: Map<String, SystemInfo> = mapOf(
        // Nintendo
        "nes" to SystemInfo(3, "Nintendo Entertainment System"),
        "famicom" to SystemInfo(3, "Nintendo Entertainment System"),
        "snes" to SystemInfo(4, "Super Nintendo"),
        "superfamicom" to SystemInfo(4, "Super Nintendo"),
        "n64" to SystemInfo(14, "Nintendo 64"),
        "gamecube" to SystemInfo(13, "Nintendo GameCube"),
        "gc" to SystemInfo(13, "Nintendo GameCube"),
        "wii" to SystemInfo(16, "Nintendo Wii"),
        "wiiu" to SystemInfo(18, "Nintendo Wii U"),
        "switch" to SystemInfo(225, "Nintendo Switch"),
        "gb" to SystemInfo(9, "Game Boy"),
        "gameboy" to SystemInfo(9, "Game Boy"),
        "gbc" to SystemInfo(10, "Game Boy Color"),
        "gameboycolor" to SystemInfo(10, "Game Boy Color"),
        "gba" to SystemInfo(12, "Game Boy Advance"),
        "gameboyadvance" to SystemInfo(12, "Game Boy Advance"),
        "nds" to SystemInfo(15, "Nintendo DS"),
        "ds" to SystemInfo(15, "Nintendo DS"),
        "3ds" to SystemInfo(17, "Nintendo 3DS"),
        "n3ds" to SystemInfo(17, "Nintendo 3DS"),
        "nintendo3ds" to SystemInfo(17, "Nintendo 3DS"),
        "virtualboy" to SystemInfo(11, "Virtual Boy"),

        // Sega
        "genesis" to SystemInfo(1, "Sega Genesis"),
        "megadrive" to SystemInfo(1, "Sega Genesis"),
        "md" to SystemInfo(1, "Sega Genesis"),
        "mastersystem" to SystemInfo(2, "Sega Master System"),
        "sms" to SystemInfo(2, "Sega Master System"),
        "gamegear" to SystemInfo(21, "Sega Game Gear"),
        "gg" to SystemInfo(21, "Sega Game Gear"),
        "saturn" to SystemInfo(22, "Sega Saturn"),
        "dreamcast" to SystemInfo(23, "Sega Dreamcast"),
        "dc" to SystemInfo(23, "Sega Dreamcast"),
        "segacd" to SystemInfo(20, "Sega CD"),
        "scd" to SystemInfo(20, "Sega CD"),
        "sega32x" to SystemInfo(19, "Sega 32X"),
        "32x" to SystemInfo(19, "Sega 32X"),
        "sg1000" to SystemInfo(109, "Sega SG-1000"),

        // Sony
        "psx" to SystemInfo(57, "PlayStation"),
        "ps1" to SystemInfo(57, "PlayStation"),
        "playstation" to SystemInfo(57, "PlayStation"),
        "ps2" to SystemInfo(58, "PlayStation 2"),
        "ps3" to SystemInfo(59, "PlayStation 3"),
        "psp" to SystemInfo(61, "PlayStation Portable"),
        "psvita" to SystemInfo(62, "PlayStation Vita"),
        "vita" to SystemInfo(62, "PlayStation Vita"),

        // Atari
        "atari2600" to SystemInfo(26, "Atari 2600"),
        "atari5200" to SystemInfo(40, "Atari 5200"),
        "atari7800" to SystemInfo(41, "Atari 7800"),
        "atarilynx" to SystemInfo(28, "Atari Lynx"),
        "lynx" to SystemInfo(28, "Atari Lynx"),
        "atarijaguar" to SystemInfo(27, "Atari Jaguar"),
        "jaguar" to SystemInfo(27, "Atari Jaguar"),
        "atarist" to SystemInfo(42, "Atari ST"),

        // NEC
        "pcengine" to SystemInfo(31, "PC Engine"),
        "pce" to SystemInfo(31, "PC Engine"),
        "tg16" to SystemInfo(31, "PC Engine"),
        "turbografx16" to SystemInfo(31, "PC Engine"),
        "pcenginecd" to SystemInfo(114, "PC Engine CD"),
        "pcecd" to SystemInfo(114, "PC Engine CD"),
        "supergrafx" to SystemInfo(105, "SuperGrafx"),
        "sgx" to SystemInfo(105, "SuperGrafx"),

        // SNK
        "neogeo" to SystemInfo(142, "Neo Geo"),
        "neogeocd" to SystemInfo(70, "Neo Geo CD"),
        "ngp" to SystemInfo(25, "Neo Geo Pocket"),
        "ngpc" to SystemInfo(82, "Neo Geo Pocket Color"),

        // Other
        "arcade" to SystemInfo(75, "Arcade"),
        "mame" to SystemInfo(75, "Arcade"),
        "fba" to SystemInfo(75, "Arcade"),
        "fbneo" to SystemInfo(75, "Arcade"),
        "colecovision" to SystemInfo(48, "ColecoVision"),
        "intellivision" to SystemInfo(115, "Intellivision"),
        "vectrex" to SystemInfo(102, "Vectrex"),
        "wonderswan" to SystemInfo(45, "WonderSwan"),
        "ws" to SystemInfo(45, "WonderSwan"),
        "wonderswancolor" to SystemInfo(46, "WonderSwan Color"),
        "wsc" to SystemInfo(46, "WonderSwan Color"),
        "msx" to SystemInfo(113, "MSX"),
        "msx2" to SystemInfo(116, "MSX2"),
        "amstradcpc" to SystemInfo(65, "Amstrad CPC"),
        "zxspectrum" to SystemInfo(76, "ZX Spectrum"),
        "c64" to SystemInfo(66, "Commodore 64"),
        "amiga" to SystemInfo(64, "Amiga"),
        "dos" to SystemInfo(135, "DOS"),
        "scummvm" to SystemInfo(123, "ScummVM"),
        "3do" to SystemInfo(29, "3DO"),
    )

    fun getSystemInfo(folderName: String): SystemInfo? {
        val normalized = folderName.lowercase().replace(Regex("[-_ ]"), "")
        return mapping[normalized]
    }

    fun isKnownSystem(folderName: String): Boolean {
        return getSystemInfo(folderName) != null
    }
}
