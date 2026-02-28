package com.pegasus.artwork.data.saf

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.pegasus.artwork.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafFileHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val contentResolver get() = context.contentResolver

    private val romExtensions = setOf(
        "zip", "7z", "nes", "sfc", "smc", "gb", "gbc", "gba", "n64", "z64", "v64",
        "nds", "3ds", "gen", "md", "smd", "bin", "iso", "cue", "chd", "cso",
        "pbp", "gcm", "gcz", "rvz", "wbfs", "wad", "nsp", "xci",
        "pce", "sgx", "ngp", "ngc", "ws", "wsc", "col", "int",
        "a26", "a52", "a78", "j64", "lnx", "vec",
        "rom", "img", "cdi", "gdi",
    )

    /**
     * Extracts the real filesystem path from a SAF tree URI.
     * e.g. content://com.android.externalstorage.documents/tree/primary%3Aroms
     * -> /storage/emulated/0/roms
     */
    fun resolveRealPath(treeUri: Uri): File? {
        return try {
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            val parts = docId.split(":", limit = 2)
            if (parts.size != 2) return null

            val volume = parts[0]
            val relativePath = parts[1]

            val basePath = if (volume == "primary") {
                Environment.getExternalStorageDirectory().absolutePath
            } else {
                // SD card or other volume
                "/storage/$volume"
            }

            File(basePath, relativePath)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun listSubfolders(rootUri: Uri): List<File> = withContext(ioDispatcher) {
        val rootDir = resolveRealPath(rootUri) ?: return@withContext emptyList()
        rootDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
    }

    suspend fun listRomFiles(folder: File): List<File> = withContext(ioDispatcher) {
        folder.listFiles()
            ?.filter { file ->
                file.isFile && file.extension.lowercase() in romExtensions
            }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
    }

    fun fileExists(
        rootPath: File,
        systemFolder: String,
        gameName: String,
        fileName: String,
    ): Boolean {
        return File(rootPath, "$systemFolder/media/$gameName/$fileName").exists()
    }

    suspend fun findOrCreateMediaFolder(
        rootUri: Uri,
        systemFolder: String,
        gameName: String,
    ): DocumentFile? = withContext(ioDispatcher) {
        val rootDoc = DocumentFile.fromTreeUri(context, rootUri) ?: return@withContext null
        val systemDoc = rootDoc.findFile(systemFolder) ?: return@withContext null

        val mediaDoc = systemDoc.findFile("media")
            ?: systemDoc.createDirectory("media")
            ?: return@withContext null

        mediaDoc.findFile(gameName)
            ?: mediaDoc.createDirectory(gameName)
    }

    suspend fun writeFile(
        parentDoc: DocumentFile,
        fileName: String,
        mimeType: String,
        inputStream: InputStream,
    ): Boolean = withContext(ioDispatcher) {
        try {
            val existing = parentDoc.findFile(fileName)
            existing?.delete()

            val newFile = parentDoc.createFile(mimeType, fileName) ?: return@withContext false
            contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
                inputStream.copyTo(outputStream, bufferSize = 8192)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun openInputStream(uri: Uri): InputStream? = withContext(ioDispatcher) {
        contentResolver.openInputStream(uri)
    }

    fun openFileInputStream(file: File): InputStream? {
        return try {
            file.inputStream()
        } catch (e: Exception) {
            null
        }
    }
}
