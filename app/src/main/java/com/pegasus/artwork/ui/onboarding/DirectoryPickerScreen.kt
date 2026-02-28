package com.pegasus.artwork.ui.onboarding

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryPickerScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val directoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let {
            // Take persistable permission
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, flags)
            viewModel.onDirectorySelected(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Select ROM Directory") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Select the root folder containing your ROM subfolders (e.g., nes/, snes/, gba/).",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { directoryLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = if (uiState.selectedUri != null) Icons.Default.FolderOpen
                        else Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        if (uiState.selectedUri != null) "Change Directory"
                        else "Select Directory",
                    )
                }

                if (uiState.selectedUri != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Selected: ${uiState.selectedPath}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )

                            if (uiState.isScanning) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Scanning folders...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else if (uiState.detectedSystems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Detected ${uiState.detectedSystems.size} systems, ${uiState.totalRoms} ROMs:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                uiState.detectedSystems.forEach { system ->
                                    Text(
                                        text = "  \u2022 $system",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No recognized system folders found. Make sure your ROMs are organized in subfolders named after their system (e.g., nes, snes, gba).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedUri != null && uiState.detectedSystems.isNotEmpty(),
            ) {
                Text("Next")
            }
        }
    }
}
