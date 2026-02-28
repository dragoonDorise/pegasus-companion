package com.pegasus.artwork.ui.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pegasus.artwork.domain.model.DownloadResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    onBack: () -> Unit,
    viewModel: DownloadViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloading Artwork") },
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
                .padding(16.dp),
        ) {
            // Error message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Progress section
            uiState.progress?.let { progress ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = "${progress.completedRoms} / ${progress.totalRoms}",
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress.overallProgress },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (progress.currentRomName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${progress.currentSystem}: ${progress.currentRomName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatChip("Success", uiState.successCount, MaterialTheme.colorScheme.primary)
                StatChip("Not Found", uiState.notFoundCount, MaterialTheme.colorScheme.outline)
                StatChip("Failed", uiState.failedCount, MaterialTheme.colorScheme.error)
                StatChip("Skipped", uiState.skippedCount, MaterialTheme.colorScheme.tertiary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Results list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(uiState.results.asReversed()) { entry ->
                    ResultRow(entry)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            if (uiState.isDownloading) {
                OutlinedButton(
                    onClick = { viewModel.cancelDownload() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel")
                }
            } else if (uiState.isComplete) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultRow(entry: DownloadResultEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (icon, tint) = when (entry.result) {
            is DownloadResult.Success -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
            is DownloadResult.PartialSuccess -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.tertiary
            is DownloadResult.NotFound -> Icons.Default.SearchOff to MaterialTheme.colorScheme.outline
            is DownloadResult.Error -> Icons.Default.Error to MaterialTheme.colorScheme.error
            is DownloadResult.Skipped -> Icons.Default.SkipNext to MaterialTheme.colorScheme.outline
            else -> Icons.Default.Error to MaterialTheme.colorScheme.error
        }

        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = tint,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = entry.romName,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
