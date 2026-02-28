package com.pegasus.artwork.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pegasus.artwork.data.local.PreferencesDataStore
import com.pegasus.artwork.data.remote.AuthConfig
import com.pegasus.artwork.data.remote.ScreenScraperApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TestResult {
    NONE, TESTING, SUCCESS, FAILED
}

data class SettingsUiState(
    val romDirectoryPath: String = "",
    val username: String = "",
    val password: String = "",
    val isSaved: Boolean = false,
    val testResult: TestResult = TestResult.NONE,
    val testMessage: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val api: ScreenScraperApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val uri = preferencesDataStore.romDirectoryUri.first()
            val username = preferencesDataStore.ssUsername.first()
            val password = preferencesDataStore.ssPassword.first()

            _uiState.value = SettingsUiState(
                romDirectoryPath = uri?.lastPathSegment ?: "Not set",
                username = username,
                password = password,
            )
        }
    }

    fun onDirectorySelected(uri: Uri) {
        viewModelScope.launch {
            preferencesDataStore.saveRomDirectoryUri(uri)
            _uiState.value = _uiState.value.copy(
                romDirectoryPath = uri.lastPathSegment ?: uri.toString(),
            )
        }
    }

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username, isSaved = false, testResult = TestResult.NONE,
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password, isSaved = false, testResult = TestResult.NONE,
        )
    }

    fun saveCredentials() {
        viewModelScope.launch {
            preferencesDataStore.saveCredentials(
                _uiState.value.username,
                _uiState.value.password,
            )
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun testCredentials() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(testResult = TestResult.TESTING, testMessage = "")

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
}
