package com.pegasus.artwork.domain.model

sealed interface DownloadResult {
    data class Success(val romName: String, val artworkTypes: List<ArtworkType>) : DownloadResult
    data class PartialSuccess(val romName: String, val downloaded: List<ArtworkType>, val failed: List<ArtworkType>) : DownloadResult
    data class NotFound(val romName: String) : DownloadResult
    data class Error(val romName: String, val message: String) : DownloadResult
    data class AuthError(val message: String) : DownloadResult
    data class QuotaExceeded(val message: String) : DownloadResult
    data object Skipped : DownloadResult
}
