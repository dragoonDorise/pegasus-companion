package com.pegasus.artwork.ui.themes

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pegasus.artwork.domain.model.Theme
import com.pegasus.artwork.domain.repository.ThemesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemesUiState(
    val isLoading: Boolean = false,
    val themes: List<Theme> = emptyList(),
    val error: String? = null,
    val hasStoragePermission: Boolean = false,
    val downloadingThemes: Set<String> = emptySet(),
)

@HiltViewModel
class ThemesViewModel @Inject constructor(
    private val themesRepository: ThemesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemesUiState())
    val uiState: StateFlow<ThemesUiState> = _uiState.asStateFlow()

    init {
        checkPermission()
    }

    fun checkPermission() {
        val hasPermission = Environment.isExternalStorageManager()
        _uiState.update { it.copy(hasStoragePermission = hasPermission) }
        if (hasPermission) {
            loadThemes()
        }
    }

    fun loadThemes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val themes = themesRepository.getThemes()
                _uiState.update { it.copy(isLoading = false, themes = themes) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load themes")
                }
            }
        }
    }

    fun downloadTheme(theme: Theme) {
        viewModelScope.launch {
            _uiState.update { it.copy(downloadingThemes = it.downloadingThemes + theme.url) }
            try {
                themesRepository.downloadTheme(theme)
                // Refresh to update isInstalled status
                val updatedThemes = _uiState.value.themes.map { t ->
                    if (t.url == theme.url) t.copy(isInstalled = true) else t
                }
                _uiState.update {
                    it.copy(
                        themes = updatedThemes,
                        downloadingThemes = it.downloadingThemes - theme.url,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to download ${theme.name}: ${e.message}",
                        downloadingThemes = it.downloadingThemes - theme.url,
                    )
                }
            }
        }
    }

    fun deleteTheme(theme: Theme) {
        viewModelScope.launch {
            try {
                themesRepository.deleteTheme(theme)
                val updatedThemes = _uiState.value.themes.map { t ->
                    if (t.url == theme.url) t.copy(isInstalled = false) else t
                }
                _uiState.update { it.copy(themes = updatedThemes) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete ${theme.name}: ${e.message}")
                }
            }
        }
    }
}
