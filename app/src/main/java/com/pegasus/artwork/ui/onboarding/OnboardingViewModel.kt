package com.pegasus.artwork.ui.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pegasus.artwork.data.local.PreferencesDataStore
import com.pegasus.artwork.data.local.SystemIdMapping
import com.pegasus.artwork.data.remote.AuthConfig
import com.pegasus.artwork.data.remote.ScreenScraperApi
import com.pegasus.artwork.data.saf.SafFileHelper
import com.pegasus.artwork.ui.settings.TestResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val selectedUri: Uri? = null,
    val selectedPath: String = "",
    val detectedSystems: List<String> = emptyList(),
    val totalRoms: Int = 0,
    val username: String = "",
    val password: String = "",
    val isScanning: Boolean = false,
    val testResult: TestResult = TestResult.NONE,
    val testMessage: String = "",
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val safFileHelper: SafFileHelper,
    private val api: ScreenScraperApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    init {
        viewModelScope.launch {
            _isOnboarded.value = preferencesDataStore.isOnboardingCompleted.first()

            // Restore previously selected URI if any
            val savedUri = preferencesDataStore.romDirectoryUri.first()
            if (savedUri != null) {
                onDirectorySelected(savedUri)
            }

            val username = preferencesDataStore.ssUsername.first()
            val password = preferencesDataStore.ssPassword.first()
            _uiState.value = _uiState.value.copy(
                username = username,
                password = password,
            )
        }
    }

    fun onDirectorySelected(uri: Uri) {
        viewModelScope.launch {
            preferencesDataStore.saveRomDirectoryUri(uri)

            _uiState.value = _uiState.value.copy(
                selectedUri = uri,
                selectedPath = uri.lastPathSegment ?: uri.toString(),
                isScanning = true,
                detectedSystems = emptyList(),
                totalRoms = 0,
            )

            val subfolders = safFileHelper.listSubfolders(uri)
            val detectedSystems = subfolders.mapNotNull { folder ->
                val info = SystemIdMapping.getSystemInfo(folder.name) ?: return@mapNotNull null
                val romCount = safFileHelper.listRomFiles(folder).size
                if (romCount > 0) "${info.displayName} ($romCount ROMs)" else null
            }

            val totalRoms = subfolders.sumOf { folder ->
                safFileHelper.listRomFiles(folder).size
            }

            _uiState.value = _uiState.value.copy(
                detectedSystems = detectedSystems,
                totalRoms = totalRoms,
                isScanning = false,
            )
        }
    }

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun saveCredentials() {
        viewModelScope.launch {
            preferencesDataStore.saveCredentials(
                _uiState.value.username,
                _uiState.value.password,
            )
        }
    }

    fun testCredentials() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                testResult = TestResult.TESTING, testMessage = "",
            )

            try {
                val response = api.getUserInfo(
                    devId = AuthConfig.devId,
                    devPassword = AuthConfig.devPassword,
                    softName = AuthConfig.softName,
                    ssId = _uiState.value.username,
                    ssPassword = _uiState.value.password,
                )

                if (response.isSuccessful) {
                    val threads = response.body()?.response?.ssuser?.maxthreads ?: 1
                    preferencesDataStore.saveMaxThreads(threads)
                    _uiState.value = _uiState.value.copy(
                        testResult = TestResult.SUCCESS,
                        testMessage = "OK — $threads thread${if (threads > 1) "s" else ""}",
                    )
                } else {
                    val msg = when (response.code()) {
                        403 -> "Invalid credentials"
                        430 -> "Daily quota exceeded"
                        else -> "Error ${response.code()}"
                    }
                    _uiState.value = _uiState.value.copy(
                        testResult = TestResult.FAILED,
                        testMessage = msg,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    testResult = TestResult.FAILED,
                    testMessage = "Connection error: ${e.message}",
                )
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            saveCredentials()
            preferencesDataStore.setOnboardingCompleted(true)
            _isOnboarded.value = true
        }
    }
}
