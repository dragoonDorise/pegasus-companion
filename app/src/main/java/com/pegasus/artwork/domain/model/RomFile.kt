package com.pegasus.artwork.domain.model

import android.net.Uri
import java.io.File

data class RomFile(
    val name: String,
    val file: File,
    val uri: Uri,
    val size: Long,
    val systemFolderName: String,
) {
    val gameNameWithoutExtension: String
        get() = name.substringBeforeLast('.')
}
