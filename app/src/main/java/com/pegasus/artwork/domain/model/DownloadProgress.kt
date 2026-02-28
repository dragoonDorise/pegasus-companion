package com.pegasus.artwork.domain.model

data class DownloadProgress(
    val totalRoms: Int,
    val completedRoms: Int,
    val currentRomName: String,
    val currentSystem: String,
    val systemProgress: Map<String, SystemProgress> = emptyMap(),
) {
    val overallProgress: Float
        get() = if (totalRoms > 0) completedRoms.toFloat() / totalRoms else 0f
}

data class SystemProgress(
    val totalRoms: Int,
    val completedRoms: Int,
    val systemName: String,
)
