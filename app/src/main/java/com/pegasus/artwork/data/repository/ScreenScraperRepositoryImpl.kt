package com.pegasus.artwork.data.repository

import com.pegasus.artwork.data.local.PreferencesDataStore
import com.pegasus.artwork.data.remote.AuthConfig
import com.pegasus.artwork.data.remote.ScreenScraperApi
import com.pegasus.artwork.data.remote.dto.GameInfoDto
import com.pegasus.artwork.domain.repository.ScreenScraperRepository
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import javax.inject.Inject

class ScreenScraperRepositoryImpl @Inject constructor(
    private val api: ScreenScraperApi,
    private val preferencesDataStore: PreferencesDataStore,
) : ScreenScraperRepository {

    override suspend fun searchByFileName(
        fileName: String,
        systemId: Int,
        crc: String?,
        md5: String?,
        sha1: String?,
    ): GameInfoDto? {
        val ssUsername = preferencesDataStore.ssUsername.first().ifBlank { null }
        val ssPassword = preferencesDataStore.ssPassword.first().ifBlank { null }

        // Try by filename first
        val response = api.getGameInfo(
            devId = AuthConfig.devId,
            devPassword = AuthConfig.devPassword,
            softName = AuthConfig.softName,
            ssId = ssUsername,
            ssPassword = ssPassword,
            systemId = systemId,
            romName = fileName,
        )

        if (response.isSuccessful) {
            return response.body()?.response?.jeu
        }

        val code = response.code()
        if (code == 403) throw AuthException("Authentication failed")
        if (code == 430) throw QuotaExceededException("Daily quota exceeded")

        // If not found by name and we have hashes, try hash-based search
        if (code == 404 && (crc != null || md5 != null || sha1 != null)) {
            val hashResponse = api.getGameInfo(
                devId = AuthConfig.devId,
                devPassword = AuthConfig.devPassword,
                softName = AuthConfig.softName,
                ssId = ssUsername,
                ssPassword = ssPassword,
                systemId = systemId,
                crc = crc,
                md5 = md5,
                sha1 = sha1,
            )

            if (hashResponse.isSuccessful) {
                return hashResponse.body()?.response?.jeu
            }

            val hashCode = hashResponse.code()
            if (hashCode == 403) throw AuthException("Authentication failed")
            if (hashCode == 430) throw QuotaExceededException("Daily quota exceeded")
        }

        if (code == 429) throw RateLimitException("Rate limited")

        return null
    }
}

class AuthException(message: String) : Exception(message)
class QuotaExceededException(message: String) : Exception(message)
class RateLimitException(message: String) : Exception(message)
