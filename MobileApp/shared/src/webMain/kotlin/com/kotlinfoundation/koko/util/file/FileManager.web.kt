package com.kotlinfoundation.koko.util.file

import com.kotlinfoundation.koko.data.BackgroundExecutor
import com.kotlinfoundation.koko.util.Constants
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.download
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Web (js/wasmJs) file manager. The browser has no filesystem paths, so this keeps file bytes in an
 * in-memory map keyed by the same unique names the other platforms use, and resolves a name to a
 * `data:` URL for display (Coil on web loads it through the Ktor/fetch engine).
 *
 * The map is a **cache over OPFS**, not the source of truth: writes also persist to OPFS (see
 * [WebFileStore]) and a warm-up on construction reloads every persisted file back into the map, so
 * images survive a page reload / new tab (matching the OPFS-backed Room database). The warm-up races the
 * first render — a not-yet-loaded image resolves to an empty string until the map fills — but boot +
 * navigation is far slower than reading a handful of files, so in practice the map is warm before any
 * gallery read.
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

    private val store = mutableMapOf<String, ByteArray>()

    // SupervisorJob: a failed OPFS write must not tear down the warm-up or later persists.
    private val scope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    init {
        scope.launch { warmUpFromOpfs() }
    }

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
        put(name, bytes)
        Result.success(name)
    }

    override suspend fun saveBase64ImageToInternalDirectory(
        base64Image: String,
        imageExtension: String,
    ): String? = backgroundExecutor.execute {
        val bytes = base64Image.decodeBase64OrNull()
            ?: return@execute Result.failure(Exception("Invalid base64 image"))
        val name = createNewUniqueFileNameWithExtension(fileExtension = imageExtension)
        put(name, bytes)
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
        val bytes = httpClient.get(url.throughDevProxy()).readRawBytes()
        val name = createNewUniqueFileNameWithExtension(fileExtension = extension)
        put(name, bytes)
        Result.success(name)
    }

    override suspend fun saveFileFromNetworkToGalleryByPickingUpLocation(
        url: String,
        fileExtension: String,
    ): Result<String> = downloadFileFromNetworkToInternalDirectory(url, fileExtension)
        .onSuccess { name -> saveImageToGallery(name) }

    override suspend fun shareFile(absoluteFilePath: String): Result<Unit> = saveImageToGallery(absoluteFilePath)

    override suspend fun readInternalFileBytes(fileNameWithExtension: String): ByteArray = resolveBytes(fileNameWithExtension) ?: error("No file bytes for: $fileNameWithExtension")

    override fun getPlatformFile(absoluteFilePath: String): PlatformFile = throw UnsupportedOperationException("getPlatformFile is not supported on web")

    /** Cache the bytes in memory and persist them to OPFS (fire-and-forget) so they survive a reload. */
    private fun put(name: String, bytes: ByteArray) {
        store[name] = bytes
        scope.launch { opfsWriteFile(Constants.WEB_INTERNAL_FILES_DIR_NAME, name, Base64.encode(bytes)) }
    }

    /** Reload every OPFS-persisted file into the in-memory cache. Best-effort — a failure leaves the map empty. */
    private suspend fun warmUpFromOpfs() {
        val json = opfsReadAllFiles(Constants.WEB_INTERNAL_FILES_DIR_NAME)
        val entries = runCatching { Json.parseToJsonElement(json).jsonArray }.getOrNull() ?: return
        entries.forEach { entry ->
            val obj = entry.jsonObject
            val name = obj["n"]?.jsonPrimitive?.content ?: return@forEach
            val bytes = obj["d"]?.jsonPrimitive?.content?.decodeBase64OrNull() ?: return@forEach
            store.getOrPut(name) { bytes } // don't clobber a write that landed during warm-up
        }
    }

    private fun resolveBytes(pathOrDataUrl: String): ByteArray? = when {
        pathOrDataUrl.startsWith("data:") -> pathOrDataUrl.substringAfter(',', "").decodeBase64OrNull()
        else -> store[pathOrDataUrl]
    }

    private fun ByteArray.toDataUrl(mimeType: String): String = "data:$mimeType;base64,${Base64.encode(this)}"

    private fun String.decodeBase64OrNull(): ByteArray? = runCatching { Base64.decode(this) }.getOrNull()

    private fun String.fileExtension(): String = substringAfterLast('.', "").substringBefore(';').lowercase()

    /**
     * Rewrite a provider CDN URL to the same-origin dev-server proxy so the browser can fetch the
     * generated image (the CDNs, e.g. `replicate.delivery`, send no `Access-Control-Allow-Origin`, so a
     * direct cross-origin fetch is CORS-blocked). Matches the `devServer.proxy` block in
     * `webApp/build.gradle.kts`. Local-dev only; a deployed web build fetches via the Cloud Functions proxy.
     */
    private fun String.throughDevProxy(): String = CDN_PROXY_REWRITES.entries
        .firstOrNull { (origin, _) -> startsWith(origin) }
        ?.let { (origin, proxyPath) -> proxyPath + removePrefix(origin) }
        ?: this

    private companion object {
        val CDN_PROXY_REWRITES = mapOf(
            "https://replicate.delivery" to "http://localhost:8080/rdelivery",
        )
    }
}

@OptIn(ExperimentalEncodingApi::class)
actual suspend fun PlatformFile.absolutePathCommon(): String {
    val bytes = readBytes()
    return "data:${mimeTypeForFileName(name)};base64,${Base64.encode(bytes)}"
}
