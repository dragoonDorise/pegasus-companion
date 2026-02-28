package com.pegasus.artwork.domain.model

enum class ArtworkType(
    val fileName: String,
    val screenScraperTypes: List<String>,
) {
    BOX_FRONT("boxFront.png", listOf("box-2D", "box-2D-back")),
    WHEEL("wheel.png", listOf("wheel-hd", "wheel")),
    SCREENSHOT("screenshot.png", listOf("ss", "sstitle")),
}
