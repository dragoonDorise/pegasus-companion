package com.pegasus.artwork.data.repository

import com.pegasus.artwork.data.remote.ThemesApi
import com.pegasus.artwork.di.DownloadClient
import com.pegasus.artwork.domain.model.Theme
import com.pegasus.artwork.domain.repository.ThemesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemesRepositoryImpl @Inject constructor(
    private val themesApi: ThemesApi,
    @DownloadClient private val httpClient: OkHttpClient,
) : ThemesRepository {

    companion object {
        private val THEMES_DIR = File("/storage/emulated/0/pegasus-frontend/themes")
    }

    override suspend fun getThemes(): List<Theme> {
        val dtos = themesApi.getThemes()
        return dtos.map { dto ->
            val folderName = deriveFolderName(dto.url)
            Theme(
                name = dto.name,
                url = dto.url,
                screenshots = dto.screenshots,
                author = dto.author,
                isInstalled = isThemeInstalled(folderName),
            )
        }
    }

    override suspend fun downloadTheme(theme: Theme): Unit = withContext(Dispatchers.IO) {
        val folderName = deriveFolderName(theme.url)
        val themeDir = File(THEMES_DIR, folderName)

        val tempFile = File.createTempFile("theme_", ".zip", THEMES_DIR.apply { mkdirs() })
        try {
            downloadZipToFile(theme.url, tempFile)
            extractZip(tempFile, themeDir)
        } finally {
            tempFile.delete()
        }
    }

    override suspend fun deleteTheme(theme: Theme): Unit = withContext(Dispatchers.IO) {
        val folderName = deriveFolderName(theme.url)
        val themeDir = File(THEMES_DIR, folderName)
        if (themeDir.exists()) {
            themeDir.deleteRecursively()
        }
    }

    override fun isThemeInstalled(name: String): Boolean {
        val themeDir = File(THEMES_DIR, name)
        return themeDir.exists() && themeDir.isDirectory
    }

    private suspend fun downloadZipToFile(githubUrl: String, destFile: File): Unit = withContext(Dispatchers.IO) {
        val mainUrl = "$githubUrl/archive/refs/heads/main.zip"
        val masterUrl = "$githubUrl/archive/refs/heads/master.zip"

        var response = httpClient.newCall(Request.Builder().url(mainUrl).build()).execute()
        if (!response.isSuccessful) {
            response.close()
            response = httpClient.newCall(Request.Builder().url(masterUrl).build()).execute()
        }
        if (!response.isSuccessful) {
            response.close()
            throw IOException("Failed to download theme zip: HTTP ${response.code}")
        }

        response.body?.byteStream()?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("Empty response body")
    }

    private fun extractZip(zipFile: File, targetDir: File) {
        targetDir.mkdirs()

        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            // Detect the top-level directory to strip (e.g. "library-main/")
            val topLevelPrefix = entry?.name?.substringBefore("/")?.plus("/") ?: ""

            while (entry != null) {
                val entryName = entry.name
                // Strip the top-level directory
                val relativePath = if (topLevelPrefix.isNotEmpty() && entryName.startsWith(topLevelPrefix)) {
                    entryName.removePrefix(topLevelPrefix)
                } else {
                    entryName
                }

                if (relativePath.isNotEmpty()) {
                    val outFile = File(targetDir, relativePath)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        outFile.outputStream().use { out ->
                            zis.copyTo(out)
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun deriveFolderName(githubUrl: String): String {
        return githubUrl.trimEnd('/').substringAfterLast('/')
    }
}
