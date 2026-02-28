package com.pegasus.artwork.ui.download

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pegasus.artwork.data.local.PreferencesDataStore
import com.pegasus.artwork.domain.model.DownloadProgress
import com.pegasus.artwork.domain.model.DownloadResult
import com.pegasus.artwork.domain.model.RomSystem
import com.pegasus.artwork.domain.usecase.DownloadAllArtworkUseCase
import com.pegasus.artwork.domain.usecase.ScanRomDirectoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadUiState(
    val isDownloading: Boolean = false,
    val isComplete: Boolean = false,
    val progress: DownloadProgress? = null,
    val results: List<DownloadResultEntry> = emptyList(),
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val skippedCount: Int = 0,
    val notFoundCount: Int = 0,
    val error: String? = null,
)

data class DownloadResultEntry(
    val romName: String,
    val result: DownloadResult,
)

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val scanRomDirectoryUseCase: ScanRomDirectoryUseCase,
    private val downloadAllArtworkUseCase: DownloadAllArtworkUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    private var downloadJob: Job? = null

    init {
        startDownload()
    }

    fun startDownload() {
        if (_uiState.value.isDownloading) return

        downloadJob = viewModelScope.launch {
            _uiState.value = DownloadUiState(isDownloading = true)

            val uri = preferencesDataStore.romDirectoryUri.first()
            if (uri == null) {
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    error = "No ROM directory configured",
                )
                return@launch
            }

            val systems = scanRomDirectoryUseCase(uri)
            if (systems.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    error = "No systems found",
                )
                return@launch
            }

            val maxThreads = preferencesDataStore.maxThreads.first()

            var successCount = 0
            var failedCount = 0
            var skippedCount = 0
            var notFoundCount = 0
            val results = mutableListOf<DownloadResultEntry>()

            downloadAllArtworkUseCase(systems, uri, maxThreads).collect { event ->
                val romName = when (val r = event.result) {
                    is DownloadResult.Success -> r.romName
                    is DownloadResult.PartialSuccess -> r.romName
                    is DownloadResult.NotFound -> r.romName
                    is DownloadResult.Error -> r.romName
                    is DownloadResult.AuthError -> "Auth Error"
                    is DownloadResult.QuotaExceeded -> "Quota Exceeded"
                    is DownloadResult.Skipped -> "Skipped"
                }

                when (event.result) {
                    is DownloadResult.Success -> successCount++
                    is DownloadResult.PartialSuccess -> successCount++
                    is DownloadResult.NotFound -> notFoundCount++
                    is DownloadResult.Error -> failedCount++
                    is DownloadResult.Skipped -> skippedCount++
                    is DownloadResult.AuthError -> {
                        _uiState.value = _uiState.value.copy(
                            isDownloading = false,
                            isComplete = true,
                            error = "Authentication failed. Please check your credentials in Settings.",
                        )
                        return@collect
                    }
                    is DownloadResult.QuotaExceeded -> {
                        _uiState.value = _uiState.value.copy(
                            isDownloading = false,
                            isComplete = true,
                            error = "Daily API quota exceeded. Please try again tomorrow.",
                        )
                        return@collect
                    }
                }

                if (event.result !is DownloadResult.Skipped) {
                    results.add(DownloadResultEntry(romName, event.result))
                } else {
                    // Don't accumulate skipped entries to save memory
                }

                _uiState.value = _uiState.value.copy(
                    progress = event.progress,
                    results = results.takeLast(50), // Keep last 50 for UI
                    successCount = successCount,
                    failedCount = failedCount,
                    skippedCount = skippedCount,
                    notFoundCount = notFoundCount,
                    isComplete = event.isComplete,
                    isDownloading = !event.isComplete,
                )
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isDownloading = false,
            isComplete = true,
        )
    }

    override fun onCleared() {
        super.onCleared()
        downloadJob?.cancel()
    }
}
