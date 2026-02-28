package com.pegasus.artwork.domain.repository

import com.pegasus.artwork.data.remote.dto.GameInfoDto

interface ScreenScraperRepository {
    suspend fun searchByFileName(
        fileName: String,
        systemId: Int,
        crc: String?,
        md5: String?,
        sha1: String?,
    ): GameInfoDto?
}
