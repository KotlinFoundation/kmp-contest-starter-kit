package com.kotlinfoundation.koko.util.file

import com.kotlinfoundation.koko.data.BackgroundExecutor
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.download
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class FileManagerImpl(
    private val backgroundExecutor: BackgroundExecutor = BackgroundExecutor.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : FileManager {
    override fun getAbsoluteFilePathRelativeToInternal(relativePathToInternal: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun copyFileToInternalDirectory(
        originalFileAbsolutePath: String,
        newFileName: String?,
    ): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun saveBase64ImageToInternalDirectory(
        base64Image: String,
        imageExtension: String,
    ): String? {
        TODO("Not yet implemented")
    }

    override suspend fun saveImageToGallery(absoluteFilePath: String): Result<Unit> {
        FileKit.download(getPlatformFile(absoluteFilePath))
        return Result.success(Unit)
    }

    override suspend fun saveFileByPickingUpLocation(absoluteFilePath: String): Result<Unit> = saveImageToGallery(absoluteFilePath)

    override suspend fun downloadFileFromNetworkToInternalDirectory(
        url: String,
        fileExtension: String?,
    ): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun saveFileFromNetworkToGalleryByPickingUpLocation(
        url: String,
        fileExtension: String,
    ): Result<String> {
        TODO("Not yet implemented")
    }

    override fun getPlatformFile(absoluteFilePath: String): PlatformFile {
        TODO("Not yet implemented")
    }
}

actual suspend fun PlatformFile.absolutePathCommon(): String {
    TODO("Not yet implemented")
}
