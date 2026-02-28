package com.pegasus.artwork.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pegasus.artwork.ui.download.DownloadScreen
import com.pegasus.artwork.ui.home.HomeScreen
import com.pegasus.artwork.ui.onboarding.CredentialsScreen
import com.pegasus.artwork.ui.onboarding.DirectoryPickerScreen
import com.pegasus.artwork.ui.onboarding.OnboardingViewModel
import com.pegasus.artwork.ui.onboarding.ReadyScreen
import com.pegasus.artwork.ui.onboarding.WelcomeScreen
import com.pegasus.artwork.ui.settings.SettingsScreen
import com.pegasus.artwork.ui.themes.ThemesScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val isOnboarded by onboardingViewModel.isOnboarded.collectAsState()

    val startDestination: Screen = if (isOnboarded) Screen.Home else Screen.Welcome

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<Screen.Welcome> {
            WelcomeScreen(
                onGetStarted = { navController.navigate(Screen.DirectoryPicker) },
            )
        }
        composable<Screen.DirectoryPicker> {
            DirectoryPickerScreen(
                viewModel = onboardingViewModel,
                onNext = { navController.navigate(Screen.Credentials) },
            )
        }
        composable<Screen.Credentials> {
            CredentialsScreen(
                viewModel = onboardingViewModel,
                onNext = { navController.navigate(Screen.Ready) },
                onSkip = { navController.navigate(Screen.Ready) },
            )
        }
        composable<Screen.Ready> {
            ReadyScreen(
                viewModel = onboardingViewModel,
                onStart = {
                    onboardingViewModel.completeOnboarding()
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Welcome> { inclusive = true }
                    }
                },
            )
        }
        composable<Screen.Home> {
            HomeScreen(
                onDownloadAll = { navController.navigate(Screen.Download) },
                onSettings = { navController.navigate(Screen.Settings) },
                onThemes = { navController.navigate(Screen.Themes) },
            )
        }
        composable<Screen.Download> {
            DownloadScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Screen.Themes> {
            ThemesScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Screen.Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onDirectoryChanged = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                },
            )
        }
    }
}
