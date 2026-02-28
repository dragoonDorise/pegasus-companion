package com.pegasus.artwork.domain.usecase

import android.net.Uri
import com.pegasus.artwork.domain.model.RomSystem
import com.pegasus.artwork.domain.repository.RomScannerRepository
import javax.inject.Inject

class ScanRomDirectoryUseCase @Inject constructor(
    private val romScannerRepository: RomScannerRepository,
) {
    suspend operator fun invoke(rootUri: Uri): List<RomSystem> {
        return romScannerRepository.scanDirectory(rootUri)
    }
}
