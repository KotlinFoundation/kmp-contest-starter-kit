package com.kotlinfoundation.koko.util.file

import com.kotlinfoundation.koko.data.BackgroundExecutor
import com.kotlinfoundation.koko.util.logging.AppLogger
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.saveImageToGallery
import io.github.vinceglb.filekit.sink
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.write
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.readByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FileManagerImpl(
    private val backgroundExecutor: BackgroundExecutor = BackgroundExecutor.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val httpClient: HttpClient = HttpClient(),
) : FileManager {

    private val rootFilesDirectory: PlatformFile get() = FileKit.filesDir

    override fun getAbsoluteFilePathRelativeToInternal(relativePathToInternal: String): String = PlatformFile(rootFilesDirectory, relativePathToInternal).absolutePath()

    override suspend fun copyFileToInternalDirectory(
        originalFileAbsolutePath: String,
        newFileName: String?,
    ): Result<String> = backgroundExecutor.execute {
        val originalFile = PlatformFile(originalFileAbsolutePath)
        val updatedNewFileName =
            newFileName ?: createNewUniqueFileNameWithExtension(originalFile.extension)
        val copyFile = PlatformFile(rootFilesDirectory, updatedNewFileName)
        originalFile.copyTo(copyFile)
        Result.success(updatedNewFileName)
    }

    override suspend fun saveBase64ImageToInternalDirectory(
        base64Image: String,
        imageExtension: String,
    ): String? = backgroundExecutor.execute {
        val imageByteArray = base64Image.decodeBase64ToByteArray()
        val imageName = createNewUniqueFileNameWithExtension(fileExtension = imageExtension)
        val imageFile = PlatformFile(rootFilesDirectory, imageName)
        imageFile.write(imageByteArray)
        Result.success(imageName)
    }.getOrNull()

    override suspend fun saveImageToGallery(absoluteFilePath: String) = backgroundExecutor.execute {
        val imageFile = PlatformFile(path = absoluteFilePath)
        FileKit.saveImageToGallery(file = imageFile)
        Result.success(Unit)
    }

    override suspend fun saveFileByPickingUpLocation(absoluteFilePath: String): Result<Unit> {
        val file = PlatformFile(absoluteFilePath)

        val pickedFileLocationToSave: PlatformFile? = FileKit.openFileSaver(
            suggestedName = file.nameWithoutExtension,
            extension = file.extension,
        )

        if (pickedFileLocationToSave == null) return Result.failure(Exception("Failed to save file"))

        pickedFileLocationToSave.write(file)
        return Result.success(Unit)
    }

    override suspend fun saveFileFromNetworkToGalleryByPickingUpLocation(
        url: String,
        fileExtension: String,
    ): Result<String> {
        val galleryFileNameWithoutExtension = createNewUniqueFileNameWithExtension(
            fileExtension = fileExtension,
            includeExtension = false,
        )
        val pickedFileLocationToSave: PlatformFile? = FileKit.openFileSaver(
            suggestedName = galleryFileNameWithoutExtension,
            extension = fileExtension,
        )

        if (pickedFileLocationToSave == null) return Result.failure(Exception("Failed to save file"))

        return downloadFileFromNetworkToInternalDirectory(
            url = url,
            fileExtension = fileExtension,
        ).map { downloadedFileName ->
            val file = PlatformFile(getAbsoluteFilePathRelativeToInternal(downloadedFileName))
            pickedFileLocationToSave.write(file)
            downloadedFileName
        }
    }

    override suspend fun downloadFileFromNetworkToInternalDirectory(
        url: String,
        fileExtension: String?,
    ): Result<String> = backgroundExecutor.execute {
        val finalFileExtension = fileExtension ?: extractFileExtensionFromUrl(url)
        if (finalFileExtension == null) return@execute Result.failure(Exception("Failed to download file. Please specify file extension"))

        val fileName = createNewUniqueFileNameWithExtension(fileExtension = finalFileExtension)
        val file = PlatformFile(rootFilesDirectory, fileName)
        val fileSink = file.sink(append = false).buffered()

        fileSink.use { bufferedSink ->
            AppLogger.d("Started Downloading file to ${file.absolutePath()}")
            val savedFileName = httpClient.prepareGet(url, {
                headers[HttpHeaders.ContentType] = ""
            }).execute { httpResponse ->

                val channel: ByteReadChannel = httpResponse.body()
                val responseLength = httpResponse.contentLength()
                if (httpResponse.status.value != 200) {
                    AppLogger.e("Failed to download file. Status code: ${httpResponse.status.value}")
                    return@execute null
                }

                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining((8 * 1024).toLong())
                    while (!packet.exhausted()) {
                        val bytes = packet.readByteArray()
                        bufferedSink.write(bytes)
                        if (responseLength != null) {
                            val percentage = (file.size() * 100f) / (responseLength)
                            AppLogger.d("Downloaded file Percentage: $percentage")
                        }
                    }
                }
                AppLogger.d("File is downloaded and saved to ${file.absolutePath()}")
                fileName
            }
            if (savedFileName == null) {
                AppLogger.e("Failed to download file")
                return@execute Result.failure(Exception("Failed to download file"))
            }
        }
        Result.success(fileName)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun String.decodeBase64ToByteArray(): ByteArray = withContext(defaultDispatcher) {
        val byteArray = encodeToByteArray()
        Base64.decode(byteArray, 0, byteArray.size)
    }

    private fun extractFileExtensionFromUrl(url: String): String? = url.substringAfterLast('.', "").takeIf { it.isNotEmpty() && it.length <= 5 }

    override fun getPlatformFile(absoluteFilePath: String): PlatformFile = PlatformFile(absoluteFilePath)

    override suspend fun readInternalFileBytes(fileNameWithExtension: String): ByteArray = PlatformFile(getAbsoluteFilePathRelativeToInternal(fileNameWithExtension)).readBytes()
}

actual suspend fun PlatformFile.absolutePathCommon(): String = absolutePath()
