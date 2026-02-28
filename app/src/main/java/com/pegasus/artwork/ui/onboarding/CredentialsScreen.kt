package com.pegasus.artwork.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import com.pegasus.artwork.ui.settings.TestResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.testResult, uiState.testMessage) {
        if (uiState.testMessage.isNotBlank() &&
            (uiState.testResult == TestResult.SUCCESS || uiState.testResult == TestResult.FAILED)
        ) {
            snackbarHostState.showSnackbar(uiState.testMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("ScreenScraper Account") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text(
                text = "Enter your ScreenScraper credentials for higher API limits. " +
                    "You can create a free account at screenscraper.fr.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = viewModel::onUsernameChanged,
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.testCredentials() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.username.isNotBlank() && uiState.password.isNotBlank() &&
                    uiState.testResult != TestResult.TESTING,
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.saveCredentials()
                    onNext()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.username.isNotBlank() && uiState.password.isNotBlank(),
            ) {
                Text("Next")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Skip for now")
            }
        }
    }
}
