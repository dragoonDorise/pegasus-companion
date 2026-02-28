package com.pegasus.artwork.data.repository

import android.net.Uri
import androidx.core.net.toUri
import com.pegasus.artwork.data.local.SystemIdMapping
import com.pegasus.artwork.data.saf.SafFileHelper
import com.pegasus.artwork.di.IoDispatcher
import com.pegasus.artwork.domain.model.RomFile
import com.pegasus.artwork.domain.model.RomSystem
import com.pegasus.artwork.domain.repository.RomScannerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RomScannerRepositoryImpl @Inject constructor(
    private val safFileHelper: SafFileHelper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RomScannerRepository {

    override suspend fun scanDirectory(rootUri: Uri): List<RomSystem> = withContext(ioDispatcher) {
        val subfolders = safFileHelper.listSubfolders(rootUri)

        subfolders.mapNotNull { folder ->
            val folderName = folder.name
            val systemInfo = SystemIdMapping.getSystemInfo(folderName) ?: return@mapNotNull null

            val romFiles = safFileHelper.listRomFiles(folder).map { romFile ->
                RomFile(
                    name = romFile.name,
                    file = romFile,
                    uri = romFile.toUri(),
                    size = romFile.length(),
                    systemFolderName = folderName,
                )
            }

            if (romFiles.isEmpty()) return@mapNotNull null

            RomSystem(
                folderName = folderName,
                systemId = systemInfo.id,
                displayName = systemInfo.displayName,
                roms = romFiles,
            )
        }.sortedBy { it.displayName }
    }
}
