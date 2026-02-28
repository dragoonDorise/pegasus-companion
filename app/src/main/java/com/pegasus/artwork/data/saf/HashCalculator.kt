package com.pegasus.artwork.data.saf

import com.pegasus.artwork.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.zip.CRC32
import javax.inject.Inject
import javax.inject.Singleton

data class FileHashes(
    val crc32: String,
    val md5: String,
    val sha1: String,
)

@Singleton
class HashCalculator @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun calculateHashes(file: File): FileHashes? = withContext(ioDispatcher) {
        try {
            val crc32 = CRC32()
            val md5Digest = MessageDigest.getInstance("MD5")
            val sha1Digest = MessageDigest.getInstance("SHA-1")

            file.inputStream().use { inputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    crc32.update(buffer, 0, bytesRead)
                    md5Digest.update(buffer, 0, bytesRead)
                    sha1Digest.update(buffer, 0, bytesRead)
                }
            }

            FileHashes(
                crc32 = "%08X".format(crc32.value),
                md5 = md5Digest.digest().toHexString(),
                sha1 = sha1Digest.digest().toHexString(),
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }
}
