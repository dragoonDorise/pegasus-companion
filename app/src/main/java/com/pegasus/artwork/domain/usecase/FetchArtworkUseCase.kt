package com.pegasus.artwork.domain.usecase

import android.net.Uri
import com.pegasus.artwork.data.local.PreferencesDataStore
import com.pegasus.artwork.data.remote.dto.MediaDto
import com.pegasus.artwork.data.repository.AuthException
import com.pegasus.artwork.data.repository.QuotaExceededException
import com.pegasus.artwork.data.repository.RateLimitException
import com.pegasus.artwork.data.saf.HashCalculator
import com.pegasus.artwork.domain.model.ArtworkType
import com.pegasus.artwork.domain.model.DownloadResult
import com.pegasus.artwork.domain.model.RomFile
import com.pegasus.artwork.domain.repository.ArtworkRepository
import com.pegasus.artwork.domain.repository.ScreenScraperRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class FetchArtworkUseCase @Inject constructor(
    private val screenScraperRepository: ScreenScraperRepository,
    private val artworkRepository: ArtworkRepository,
    private val hashCalculator: HashCalculator,
    private val preferencesDataStore: PreferencesDataStore,
) {
    companion object {
        private val REGION_PRIORITY = listOf("us", "eu", "wor")
    }

    suspend operator fun invoke(
        rom: RomFile,
        rootUri: Uri,
        systemId: Int,
    ): DownloadResult {
        // Check which artwork types are needed
        val neededTypes = ArtworkType.entries.filter { type ->
            !artworkRepository.artworkExists(
                rootUri, rom.systemFolderName, rom.gameNameWithoutExtension, type,
            )
        }

        if (neededTypes.isEmpty()) return DownloadResult.Skipped

        // Skip ROMs previously marked as not found
        val notFoundKey = "${rom.systemFolderName}/${rom.name}"
        if (preferencesDataStore.isRomNotFound(notFoundKey)) {
            return DownloadResult.Skipped
        }

        // Query ScreenScraper API
        val gameInfo = try {
            var result = screenScraperRepository.searchByFileName(
                fileName = rom.name,
                systemId = systemId,
                crc = null,
                md5 = null,
                sha1 = null,
            )

            // If not found by name, try hash-based search
            if (result == null) {
                val hashes = hashCalculator.calculateHashes(rom.file)
                if (hashes != null) {
                    result = screenScraperRepository.searchByFileName(
                        fileName = rom.name,
                        systemId = systemId,
                        crc = hashes.crc32,
                        md5 = hashes.md5,
                        sha1 = hashes.sha1,
                    )
                }
            }

            result
        } catch (e: AuthException) {
            return DownloadResult.AuthError(e.message ?: "Authentication failed")
        } catch (e: QuotaExceededException) {
            return DownloadResult.QuotaExceeded(e.message ?: "Quota exceeded")
        } catch (e: RateLimitException) {
            // Retry with exponential backoff
            return retryWithBackoff(rom, rootUri, systemId)
        } catch (e: Exception) {
            return DownloadResult.Error(rom.name, e.message ?: "Unknown error")
        }

        if (gameInfo == null) {
            preferencesDataStore.addNotFoundRom(notFoundKey)
            return DownloadResult.NotFound(rom.name)
        }

        // Download each needed artwork type
        val downloaded = mutableListOf<ArtworkType>()
        val failed = mutableListOf<ArtworkType>()

        for (type in neededTypes) {
            val mediaUrl = selectMediaUrl(gameInfo.medias, type)
            if (mediaUrl == null) {
                failed.add(type)
                continue
            }

            val success = artworkRepository.downloadAndSaveArtwork(
                imageUrl = mediaUrl,
                rootUri = rootUri,
                systemFolder = rom.systemFolderName,
                gameName = rom.gameNameWithoutExtension,
                artworkType = type,
            )

            if (success) downloaded.add(type) else failed.add(type)
        }

        return when {
            downloaded.isEmpty() && failed.isEmpty() -> DownloadResult.NotFound(rom.name)
            failed.isEmpty() -> DownloadResult.Success(rom.name, downloaded)
            downloaded.isNotEmpty() -> DownloadResult.PartialSuccess(rom.name, downloaded, failed)
            else -> DownloadResult.Error(rom.name, "Failed to download all artwork")
        }
    }

    private fun selectMediaUrl(medias: List<MediaDto>, artworkType: ArtworkType): String? {
        for (ssType in artworkType.screenScraperTypes) {
            val matchingMedias = medias.filter { it.type == ssType }
            if (matchingMedias.isEmpty()) continue

            // Try regions in priority order
            for (region in REGION_PRIORITY) {
                val regionMatch = matchingMedias.find { it.region == region }
                if (regionMatch != null) return regionMatch.url
            }

            // Fallback to first available
            return matchingMedias.first().url
        }
        return null
    }

    private suspend fun retryWithBackoff(
        rom: RomFile,
        rootUri: Uri,
        systemId: Int,
    ): DownloadResult {
        var delayMs = 2000L
        val maxRetries = 3
        for (attempt in 1..maxRetries) {
            delay(delayMs)
            try {
                val result = invoke(rom, rootUri, systemId)
                if (result !is DownloadResult.Error) return result
            } catch (_: RateLimitException) {
                // continue retrying
            }
            delayMs = (delayMs * 2).coerceAtMost(30_000L)
        }
        return DownloadResult.Error(rom.name, "Rate limited after retries")
    }
}
