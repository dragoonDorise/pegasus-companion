package com.pegasus.artwork.domain.model

data class Theme(
    val name: String,
    val url: String,
    val screenshots: List<String>,
    val author: String,
    val isInstalled: Boolean,
)
