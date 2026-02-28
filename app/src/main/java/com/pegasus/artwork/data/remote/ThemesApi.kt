package com.pegasus.artwork.data.remote

import com.pegasus.artwork.data.remote.dto.ThemeDto
import retrofit2.http.GET

interface ThemesApi {
    @GET("file/emudeck-assets/android/pegasus-themes.json")
    suspend fun getThemes(): List<ThemeDto>
}
