package com.pegasus.artwork.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadyScreen(
    viewModel: OnboardingViewModel,
    onStart: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ready to Go") })
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
                    text = "Everything is set up. Here's a summary:",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ConfigRow(
                            label = "ROM Directory",
                            value = uiState.selectedPath,
                            isConfigured = uiState.selectedUri != null,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ConfigRow(
                            label = "Systems Detected",
                            value = "${uiState.detectedSystems.size} systems, ${uiState.totalRoms} ROMs",
                            isConfigured = uiState.detectedSystems.isNotEmpty(),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ConfigRow(
                            label = "ScreenScraper Account",
                            value = if (uiState.username.isNotBlank()) uiState.username else "Not configured",
                            isConfigured = uiState.username.isNotBlank(),
                        )
                    }
                }

                if (uiState.detectedSystems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Detected Systems",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            uiState.detectedSystems.forEach { system ->
                                Text(
                                    text = "\u2022 $system",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun ConfigRow(
    label: String,
    value: String,
    isConfigured: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isConfigured) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp),
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
