package com.pegasus.artwork.domain.repository

import com.pegasus.artwork.domain.model.Theme

interface ThemesRepository {
    suspend fun getThemes(): List<Theme>
    suspend fun downloadTheme(theme: Theme)
    suspend fun deleteTheme(theme: Theme)
    fun isThemeInstalled(name: String): Boolean
}
