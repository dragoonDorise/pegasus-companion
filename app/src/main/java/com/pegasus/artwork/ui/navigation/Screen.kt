package com.pegasus.artwork.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Welcome : Screen
    @Serializable data object DirectoryPicker : Screen
    @Serializable data object Credentials : Screen
    @Serializable data object Ready : Screen
    @Serializable data object Home : Screen
    @Serializable data object Download : Screen
    @Serializable data object Settings : Screen
    @Serializable data object Themes : Screen
}
