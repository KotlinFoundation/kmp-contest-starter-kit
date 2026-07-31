package com.kotlinfoundation.koko.util.file

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.dialogs.shareFile

actual suspend fun FileKit.shareFileCommon(file: PlatformFile) {
    FileKit.shareFile(file)
}

actual suspend fun FileKit.openCameraPicker(): PlatformFile? = FileKit.openCameraPicker(cameraFacing = FileKitCameraFacing.Back)

actual fun isCameraCaptureSupported(): Boolean = true
