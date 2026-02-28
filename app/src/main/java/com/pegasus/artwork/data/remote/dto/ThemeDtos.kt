package com.pegasus.artwork.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ThemeDto(
    val name: String,
    val url: String,
    val screenshots: List<String> = emptyList(),
    val author: String = "",
)
