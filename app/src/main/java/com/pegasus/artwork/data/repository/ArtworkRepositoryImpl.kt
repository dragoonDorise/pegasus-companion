package com.pegasus.artwork.data.repository

import android.net.Uri
import com.pegasus.artwork.data.saf.SafFileHelper
import com.pegasus.artwork.di.DownloadClient
import com.pegasus.artwork.di.IoDispatcher
import com.pegasus.artwork.domain.model.ArtworkType
import com.pegasus.artwork.domain.repository.ArtworkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class ArtworkRepositoryImpl @Inject constructor(
    private val safFileHelper: SafFileHelper,
    @DownloadClient private val downloadClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ArtworkRepository {

    override suspend fun downloadAndSaveArtwork(
        imageUrl: String,
        rootUri: Uri,
        systemFolder: String,
        gameName: String,
        artworkType: ArtworkType,
    ): Boolean = withContext(ioDispatcher) {
        try {
            val mediaDir = safFileHelper.findOrCreateMediaFolder(rootUri, systemFolder, gameName)
                ?: return@withContext false

            val request = Request.Builder().url(imageUrl).build()
            val response = downloadClient.newCall(request).execute()

            if (!response.isSuccessful) return@withContext false

            val body = response.body ?: return@withContext false
            body.byteStream().use { inputStream ->
                safFileHelper.writeFile(
                    parentDoc = mediaDir,
                    fileName = artworkType.fileName,
                    mimeType = "image/png",
                    inputStream = inputStream,
                )
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun artworkExists(
        rootUri: Uri,
        systemFolder: String,
        gameName: String,
        artworkType: ArtworkType,
    ): Boolean {
        val rootPath = safFileHelper.resolveRealPath(rootUri) ?: return false
        return safFileHelper.fileExists(rootPath, systemFolder, gameName, artworkType.fileName)
    }
}
