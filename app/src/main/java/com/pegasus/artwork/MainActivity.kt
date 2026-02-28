package com.pegasus.artwork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pegasus.artwork.ui.navigation.AppNavGraph
import com.pegasus.artwork.ui.theme.PegasusCompanionTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PegasusCompanionTheme {
                AppNavGraph()
            }
        }
    }
}
