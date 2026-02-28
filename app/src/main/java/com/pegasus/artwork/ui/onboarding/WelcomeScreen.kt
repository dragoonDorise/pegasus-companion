package com.pegasus.artwork.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pegasus.artwork.R

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Pegasus Companion",
                modifier = Modifier.size(128.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pegasus Companion",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Download box art, wheels, and screenshots for your ROM collection. " +
                    "Artwork is saved in a format compatible with Pegasus Frontend.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Get Started")
            }
        }
    }
}
