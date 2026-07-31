package com.kotlinfoundation.koko.util.file

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Interface defining common file operations for internal storage and gallery management.
 * All paths returned by functions are **relative to the app's internal directory** unless specified.
 */
interface FileManager {

    // Convert relative path to absolute path; returns absolute file path
    fun getAbsoluteFilePathRelativeToInternal(relativePathToInternal: String): String

    // Copy file into internal dir; if newFileName is null, new unique file name will be used
    // Returns Result containing relative path to internal directory of copied file
    suspend fun copyFileToInternalDirectory(
        originalFileAbsolutePath: String,
        newFileName: String? = null,
    ): Result<String>

    // Save Base64 image to internal dir
    // Returns relative path to internal directory of saved image, or null if failed
    suspend fun saveBase64ImageToInternalDirectory(
        base64Image: String,
        imageExtension: String = "png",
    ): String?

    suspend fun saveImageToGallery(absoluteFilePath: String): Result<Unit>

    suspend fun saveFileByPickingUpLocation(absoluteFilePath: String): Result<Unit>

    // Download file from network to internal dir
    // Returns Result containing relative path  to internal directory of saved file
    suspend fun downloadFileFromNetworkToInternalDirectory(
        url: String,
        fileExtension: String? = null,
    ): Result<String>

    suspend fun saveFileFromNetworkToGalleryByPickingUpLocation(
        url: String,
        fileExtension: String,
    ): Result<String>

    /**
     * Reads the raw bytes of a file stored under [fileNameWithExtension] in the internal directory.
     * Prefer this over [getPlatformFile] for cross-platform byte access — the web target has no file
     * handles, only bytes.
     */
    suspend fun readInternalFileBytes(fileNameWithExtension: String): ByteArray

    suspend fun shareFile(absoluteFilePath: String): Result<Unit> {
        FileKit.shareFileCommon(file = getPlatformFile(absoluteFilePath))
        return Result.success(Unit)
    }

    @OptIn(ExperimentalTime::class)
    fun createNewUniqueFileNameWithExtension(
        fileExtension: String,
        includeExtension: Boolean = true,
    ): String {
        val fileNamePrefix = when (fileExtension.lowercase()) {
            "png", "jpg", "jpeg", "webp" -> "IMG"
            "mp4", "mov", "mkv" -> "VID"
            "mp3", "wav", "m4a", "aac", "ogg", "flac" -> "AUD"
            else -> "FILE"
        }
        val timestamp = Clock.System.now().toEpochMilliseconds()
        return if (includeExtension) {
            "${fileNamePrefix}_$timestamp.${fileExtension.lowercase()}"
        } else {
            "${fileNamePrefix}_$timestamp"
        }
    }

    fun getPlatformFile(absoluteFilePath: String): PlatformFile
}

suspend fun FileManager.saveImageToGalleryFromNetwork(
    url: String,
    imageExtension: String = "png",
): Result<Unit> {
    val downloadedFileResult = downloadFileFromNetworkToInternalDirectory(
        url = url,
        fileExtension = imageExtension,
    )
    return downloadedFileResult.map { downloadedFileName ->
        saveImageToGallery(getAbsoluteFilePathRelativeToInternal(downloadedFileName))
    }
}

/** Best-effort MIME type derived from a file name's extension, for uploads/downloads. */
fun mimeTypeForFileName(fileName: String): String = when (fileName.substringAfterLast('.', "").lowercase()) {
    "png" -> "image/png"
    "jpg", "jpeg" -> "image/jpeg"
    "webp" -> "image/webp"
    "gif" -> "image/gif"
    "bmp" -> "image/bmp"
    "svg" -> "image/svg+xml"
    "mp4" -> "video/mp4"
    "mov" -> "video/quicktime"
    "webm" -> "video/webm"
    else -> "application/octet-stream"
}

expect suspend fun FileKit.shareFileCommon(file: PlatformFile)
expect suspend fun PlatformFile.absolutePathCommon(): String

/** Opens the device camera to capture a photo. Returns null if cancelled or unsupported (desktop/web). */
expect suspend fun FileKit.openCameraPicker(): PlatformFile?

/**
 * Whether in-app camera capture is available on this platform. False on desktop/web, where
 * [openCameraPicker] is a no-op — callers should hide the capture action and use the file picker.
 */
expect fun isCameraCaptureSupported(): Boolean
