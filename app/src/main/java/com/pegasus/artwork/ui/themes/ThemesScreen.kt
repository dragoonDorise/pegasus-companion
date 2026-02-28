package com.pegasus.artwork.ui.themes

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.pegasus.artwork.domain.model.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(
    onBack: () -> Unit,
    viewModel: ThemesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.checkPermission()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Themes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (!uiState.hasStoragePermission) {
            PermissionGate(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onGrantPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:${context.packageName}"),
                        )
                        context.startActivity(intent)
                    }
                },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    uiState.error != null && uiState.themes.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error!!,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(onClick = { viewModel.loadThemes() }) {
                                Text("Retry")
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(uiState.themes) { theme ->
                                ThemeCard(
                                    theme = theme,
                                    isDownloading = theme.url in uiState.downloadingThemes,
                                    onDownload = { viewModel.downloadTheme(theme) },
                                    onDelete = { viewModel.deleteTheme(theme) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionGate(
    modifier: Modifier = Modifier,
    onGrantPermission: () -> Unit,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Storage Permission Required",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Themes need to be installed to your device storage. " +
                "Please grant \"All Files Access\" permission to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGrantPermission) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun ThemeCard(
    theme: Theme,
    isDownloading: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Screenshots
            if (theme.screenshots.isNotEmpty()) {
                AsyncImage(
                    model = theme.screenshots.first(),
                    contentDescription = "${theme.name} screenshot",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Name and author
            Text(
                text = theme.name,
                style = MaterialTheme.typography.titleMedium,
            )
            if (theme.author.isNotBlank()) {
                Text(
                    text = "by ${theme.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (theme.isInstalled) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Installed",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                } else if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Downloading…",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Button(onClick = onDownload) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download")
                    }
                }
            }
        }
    }
}
