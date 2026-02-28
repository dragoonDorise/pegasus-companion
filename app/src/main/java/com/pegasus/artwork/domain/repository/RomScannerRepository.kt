package com.pegasus.artwork.domain.repository

import android.net.Uri
import com.pegasus.artwork.domain.model.RomSystem

interface RomScannerRepository {
    suspend fun scanDirectory(rootUri: Uri): List<RomSystem>
}
