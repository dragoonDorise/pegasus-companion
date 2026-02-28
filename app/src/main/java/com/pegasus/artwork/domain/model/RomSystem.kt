package com.pegasus.artwork.domain.model

data class RomSystem(
    val folderName: String,
    val systemId: Int,
    val displayName: String,
    val roms: List<RomFile>,
)
