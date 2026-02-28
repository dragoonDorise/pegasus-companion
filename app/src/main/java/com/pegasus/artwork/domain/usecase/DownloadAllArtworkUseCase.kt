package com.pegasus.artwork.domain.usecase

import android.net.Uri
import com.pegasus.artwork.domain.model.DownloadProgress
import com.pegasus.artwork.domain.model.DownloadResult
import com.pegasus.artwork.domain.model.RomFile
import com.pegasus.artwork.domain.model.RomSystem
import com.pegasus.artwork.domain.model.SystemProgress
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import javax.inject.Inject

data class DownloadEvent(
    val progress: DownloadProgress,
    val result: DownloadResult,
    val isComplete: Boolean = false,
)

class DownloadAllArtworkUseCase @Inject constructor(
    private val fetchArtworkUseCase: FetchArtworkUseCase,
) {
    private data class RomJob(
        val rom: RomFile,
        val systemId: Int,
        val systemFolderName: String,
        val systemDisplayName: String,
    )

    operator fun invoke(
        systems: List<RomSystem>,
        rootUri: Uri,
        maxThreads: Int = 1,
    ): Flow<DownloadEvent> = flow {
        val totalRoms = systems.sumOf { it.roms.size }
        var completedRoms = 0
        val systemProgressMap = mutableMapOf<String, SystemProgress>()

        // Initialize system progress
        for (system in systems) {
            systemProgressMap[system.folderName] = SystemProgress(
                totalRoms = system.roms.size,
                completedRoms = 0,
                systemName = system.displayName,
            )
        }

        // Build flat list of all ROM jobs
        val allJobs = systems.flatMap { system ->
            system.roms.map { rom ->
                RomJob(rom, system.systemId, system.folderName, system.displayName)
            }
        }

        if (maxThreads <= 1) {
            // Sequential processing (original behavior)
            for (job in allJobs) {
                val result = fetchArtworkUseCase(job.rom, rootUri, job.systemId)

                completedRoms++
                val sp = systemProgressMap[job.systemFolderName]!!
                systemProgressMap[job.systemFolderName] = sp.copy(
                    completedRoms = sp.completedRoms + 1,
                )

                emit(DownloadEvent(
                    progress = DownloadProgress(
                        totalRoms = totalRoms,
                        completedRoms = completedRoms,
                        currentRomName = job.rom.name,
                        currentSystem = job.systemDisplayName,
                        systemProgress = systemProgressMap.toMap(),
                    ),
                    result = result,
                ))

                if (result is DownloadResult.AuthError || result is DownloadResult.QuotaExceeded) {
                    emit(DownloadEvent(
                        progress = DownloadProgress(
                            totalRoms = totalRoms,
                            completedRoms = completedRoms,
                            currentRomName = "",
                            currentSystem = "",
                            systemProgress = systemProgressMap.toMap(),
                        ),
                        result = result,
                        isComplete = true,
                    ))
                    return@flow
                }
            }
        } else {
            // Concurrent processing with maxThreads workers
            val semaphore = Semaphore(maxThreads)
            val resultsChannel = Channel<Pair<RomJob, DownloadResult>>(Channel.UNLIMITED)
            var stopRequested = false

            coroutineScope {
                // Launch producer coroutines
                val producerJob = launch {
                    for (job in allJobs) {
                        if (stopRequested) break
                        semaphore.acquire()
                        if (stopRequested) {
                            semaphore.release()
                            break
                        }
                        launch {
                            try {
                                val result = fetchArtworkUseCase(job.rom, rootUri, job.systemId)
                                resultsChannel.send(job to result)
                            } finally {
                                semaphore.release()
                            }
                        }
                    }
                }

                // Collect results
                var received = 0
                for ((job, result) in resultsChannel) {
                    received++
                    completedRoms++

                    val sp = systemProgressMap[job.systemFolderName]!!
                    systemProgressMap[job.systemFolderName] = sp.copy(
                        completedRoms = sp.completedRoms + 1,
                    )

                    emit(DownloadEvent(
                        progress = DownloadProgress(
                            totalRoms = totalRoms,
                            completedRoms = completedRoms,
                            currentRomName = job.rom.name,
                            currentSystem = job.systemDisplayName,
                            systemProgress = systemProgressMap.toMap(),
                        ),
                        result = result,
                    ))

                    if (result is DownloadResult.AuthError || result is DownloadResult.QuotaExceeded) {
                        stopRequested = true
                        producerJob.cancel()
                        emit(DownloadEvent(
                            progress = DownloadProgress(
                                totalRoms = totalRoms,
                                completedRoms = completedRoms,
                                currentRomName = "",
                                currentSystem = "",
                                systemProgress = systemProgressMap.toMap(),
                            ),
                            result = result,
                            isComplete = true,
                        ))
                        return@coroutineScope
                    }

                    if (received >= totalRoms) break
                }
            }
        }

        // Emit completion
        emit(DownloadEvent(
            progress = DownloadProgress(
                totalRoms = totalRoms,
                completedRoms = completedRoms,
                currentRomName = "",
                currentSystem = "",
                systemProgress = systemProgressMap.toMap(),
            ),
            result = DownloadResult.Skipped,
            isComplete = true,
        ))
    }
}
