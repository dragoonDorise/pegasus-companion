package com.pegasus.artwork.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onDirectoryChanged: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.testResult, uiState.testMessage) {
        if (uiState.testMessage.isNotBlank() &&
            (uiState.testResult == TestResult.SUCCESS || uiState.testResult == TestResult.FAILED)
        ) {
            snackbarHostState.showSnackbar(uiState.testMessage)
        }
    }

    val directoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, flags)
            viewModel.onDirectorySelected(it)
            onDirectoryChanged()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Credentials section
            Text(
                text = "ScreenScraper Credentials",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password"
                            else "Show password",
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { viewModel.saveCredentials() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (uiState.isSaved) "Saved" else "Save")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { viewModel.testCredentials() },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.testResult != TestResult.TESTING,
                ) {
                    if (uiState.testResult == TestResult.TESTING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Login")
                    }
                }
            }

            if (uiState.testMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.testMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (uiState.testResult) {
                        TestResult.SUCCESS -> MaterialTheme.colorScheme.primary
                        TestResult.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // ROM Directory section
            Text(
                text = "ROM Directory",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.romDirectoryPath,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { directoryLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Folder, contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp))
                Text("Change Directory")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // About
            Text(
                text = "Pegasus Companion v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = "Artwork data provided by ScreenScraper.fr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
