package com.pegasus.artwork.domain.repository

import android.net.Uri
import com.pegasus.artwork.domain.model.ArtworkType

interface ArtworkRepository {
    suspend fun downloadAndSaveArtwork(
        imageUrl: String,
        rootUri: Uri,
        systemFolder: String,
        gameName: String,
        artworkType: ArtworkType,
    ): Boolean

    suspend fun artworkExists(
        rootUri: Uri,
        systemFolder: String,
        gameName: String,
        artworkType: ArtworkType,
    ): Boolean
}
