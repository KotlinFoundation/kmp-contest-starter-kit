package com.kotlinfoundation.koko.util.file

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

actual suspend fun FileKit.shareFileCommon(file: PlatformFile) {
    println("Sharing file is not supported on this platform")
}

// Camera capture is not available on desktop/web — use the gallery / file picker instead.
actual suspend fun FileKit.openCameraPicker(): PlatformFile? = null

actual fun isCameraCaptureSupported(): Boolean = false
