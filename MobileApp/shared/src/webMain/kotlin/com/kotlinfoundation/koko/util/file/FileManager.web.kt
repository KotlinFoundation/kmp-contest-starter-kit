package com.kotlinfoundation.koko.util.file

import com.kotlinfoundation.koko.data.BackgroundExecutor
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.download
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Web (js/wasmJs) file manager. The browser has no filesystem paths, so this keeps file bytes in an
 * in-memory map keyed by the same unique names the other platforms use, and resolves a name to a
 * `data:` URL for display (Coil on web loads it through the Ktor/fetch engine). Byte storage is
 * **session-scoped** — images do not survive a page reload (Room rows do, but their bytes are gone).
 *
 * Any string that leaves this class as an "absolute path" is a `data:` URL, and any web-internal
 * resolution accepts either such a URL or a bare store name.
 */
@OptIn(ExperimentalEncodingApi::class)
class FileManagerImpl(
    private val backgroundExecutor: BackgroundExecutor = BackgroundExecutor.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val httpClient: HttpClient = HttpClient(),
) : FileManager {

    // name -> raw bytes, for the current session only.
    private val store = mutableMapOf<String, ByteArray>()

    override fun getAbsoluteFilePathRelativeToInternal(relativePathToInternal: String): String {
        val bytes = store[relativePathToInternal] ?: return ""
        return bytes.toDataUrl(mimeTypeForFileName(relativePathToInternal))
    }

    override suspend fun copyFileToInternalDirectory(
        originalFileAbsolutePath: String,
        newFileName: String?,
    ): Result<String> = backgroundExecutor.execute {
        val bytes = resolveBytes(originalFileAbsolutePath)
            ?: return@execute Result.failure(Exception("No file to copy for: $originalFileAbsolutePath"))
        val extension = originalFileAbsolutePath.fileExtension().ifEmpty { "png" }
        val name = newFileName ?: createNewUniqueFileNameWithExtension(extension)
        store[name] = bytes
        Result.success(name)
    }

    override suspend fun saveBase64ImageToInternalDirectory(
        base64Image: String,
        imageExtension: String,
    ): String? = backgroundExecutor.execute {
        val bytes = base64Image.decodeBase64OrNull()
            ?: return@execute Result.failure(Exception("Invalid base64 image"))
        val name = createNewUniqueFileNameWithExtension(fileExtension = imageExtension)
        store[name] = bytes
        Result.success(name)
    }.getOrNull()

    override suspend fun saveImageToGallery(absoluteFilePath: String): Result<Unit> = backgroundExecutor.execute {
        val bytes = resolveBytes(absoluteFilePath)
            ?: return@execute Result.failure(Exception("Nothing to save for: $absoluteFilePath"))
        val extension = absoluteFilePath.fileExtension().ifEmpty { "png" }
        FileKit.download(bytes = bytes, fileName = createNewUniqueFileNameWithExtension(extension))
        Result.success(Unit)
    }

    override suspend fun saveFileByPickingUpLocation(absoluteFilePath: String): Result<Unit> = saveImageToGallery(absoluteFilePath)

    override suspend fun downloadFileFromNetworkToInternalDirectory(
        url: String,
        fileExtension: String?,
    ): Result<String> = backgroundExecutor.execute {
        val extension = fileExtension
            ?: url.fileExtension().ifEmpty { null }
            ?: return@execute Result.failure(Exception("Failed to download. Please specify file extension"))
        val bytes = httpClient.get(url).readRawBytes()
        val name = createNewUniqueFileNameWithExtension(fileExtension = extension)
        store[name] = bytes
        Result.success(name)
    }

    override suspend fun saveFileFromNetworkToGalleryByPickingUpLocation(
        url: String,
        fileExtension: String,
    ): Result<String> = downloadFileFromNetworkToInternalDirectory(url, fileExtension)
        .onSuccess { name -> saveImageToGallery(name) }

    // Sharing on web is a download.
    override suspend fun shareFile(absoluteFilePath: String): Result<Unit> = saveImageToGallery(absoluteFilePath)

    override suspend fun readInternalFileBytes(fileNameWithExtension: String): ByteArray = resolveBytes(fileNameWithExtension) ?: error("No file bytes for: $fileNameWithExtension")

    // Web has no file handles. The upload path reads bytes via readInternalFileBytes and sharing is a
    // download, so nothing in the app calls this on web.
    override fun getPlatformFile(absoluteFilePath: String): PlatformFile = throw UnsupportedOperationException("getPlatformFile is not supported on web")

    private fun resolveBytes(pathOrDataUrl: String): ByteArray? = when {
        pathOrDataUrl.startsWith("data:") -> pathOrDataUrl.substringAfter(',', "").decodeBase64OrNull()
        else -> store[pathOrDataUrl]
    }

    private fun ByteArray.toDataUrl(mimeType: String): String = "data:$mimeType;base64,${Base64.encode(this)}"

    private fun String.decodeBase64OrNull(): ByteArray? = runCatching { Base64.decode(this) }.getOrNull()

    private fun String.fileExtension(): String = substringAfterLast('.', "").substringBefore(';').lowercase()
}

// The picked PlatformFile's bytes become a self-contained data: URL — usable both as an immediate
// Coil preview and as the input to copyFileToInternalDirectory (which decodes it back).
@OptIn(ExperimentalEncodingApi::class)
actual suspend fun PlatformFile.absolutePathCommon(): String {
    val bytes = readBytes()
    return "data:${mimeTypeForFileName(name)};base64,${Base64.encode(bytes)}"
}
