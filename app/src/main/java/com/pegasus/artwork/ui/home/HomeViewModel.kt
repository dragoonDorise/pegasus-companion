package com.pegasus.artwork.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pegasus.artwork.data.local.PreferencesDataStore
import com.pegasus.artwork.domain.model.ArtworkType
import com.pegasus.artwork.domain.model.RomSystem
import com.pegasus.artwork.domain.repository.ArtworkRepository
import com.pegasus.artwork.domain.usecase.ScanRomDirectoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SystemUiModel(
    val system: RomSystem,
    val artworkComplete: Int = 0,
    val artworkMissing: Int = 0,
    val isCheckingArtwork: Boolean = true,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val systems: List<SystemUiModel> = emptyList(),
    val totalRoms: Int = 0,
    val totalWithArtwork: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val scanRomDirectoryUseCase: ScanRomDirectoryUseCase,
    private val artworkRepository: ArtworkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var _rootUri: Uri? = null
    val rootUri: Uri? get() = _rootUri

    private var _systems: List<RomSystem> = emptyList()
    val systems: List<RomSystem> get() = _systems

    init {
        loadSystems()
    }

    fun loadSystems() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)

            val uri = preferencesDataStore.romDirectoryUri.first()
            if (uri == null) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = "No ROM directory configured",
                )
                return@launch
            }

            _rootUri = uri

            try {
                val systems = scanRomDirectoryUseCase(uri)
                _systems = systems

                // Show systems immediately with ROM counts
                val systemModels = systems.map { system ->
                    SystemUiModel(
                        system = system,
                        artworkComplete = 0,
                        artworkMissing = system.roms.size,
                        isCheckingArtwork = true,
                    )
                }

                _uiState.value = HomeUiState(
                    isLoading = false,
                    systems = systemModels,
                    totalRoms = systems.sumOf { it.roms.size },
                    totalWithArtwork = 0,
                )

                // Check artwork status per system in background
                for ((index, system) in systems.withIndex()) {
                    launch {
                        var complete = 0
                        for (rom in system.roms) {
                            val hasAll = ArtworkType.entries.all { type ->
                                artworkRepository.artworkExists(
                                    uri, system.folderName, rom.gameNameWithoutExtension, type,
                                )
                            }
                            if (hasAll) complete++
                        }

                        val current = _uiState.value
                        val updatedSystems = current.systems.toMutableList()
                        updatedSystems[index] = updatedSystems[index].copy(
                            artworkComplete = complete,
                            artworkMissing = system.roms.size - complete,
                            isCheckingArtwork = false,
                        )
                        _uiState.value = current.copy(
                            systems = updatedSystems,
                            totalWithArtwork = updatedSystems.sumOf { it.artworkComplete },
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = "Failed to scan directory: ${e.message}",
                )
            }
        }
    }
}
