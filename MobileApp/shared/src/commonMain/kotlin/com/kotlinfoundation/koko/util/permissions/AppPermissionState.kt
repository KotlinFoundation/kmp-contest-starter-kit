package com.kotlinfoundation.koko.util.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.mohamedrejeb.calf.permissions.Camera
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.FineLocation
import com.mohamedrejeb.calf.permissions.Gallery
import com.mohamedrejeb.calf.permissions.Notification
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.PermissionState
import com.mohamedrejeb.calf.permissions.RecordAudio
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import com.mohamedrejeb.calf.permissions.shouldShowRationale

/**
 * App-level runtime-permission state, backed by Calf on Android/iOS and
 * granted no-op implementations on desktop/web.
 *
 * Obtain via [rememberAppPermissionState] or one of the per-permission helpers
 * ([rememberNotificationPermissionState], [rememberCameraPermissionState], ...).
 */
@Stable
interface AppPermissionState {
    /** `true` when the permission is currently granted. */
    val isGranted: Boolean

    /**
     * `true` when the user denied the permission once and the platform recommends
     * explaining why the app needs it before asking again.
     */
    val shouldShowRationale: Boolean

    /**
     * Shows the system permission dialog. Call from a non-composable scope
     * (e.g. a click callback or LaunchedEffect). The dialog may not appear if the
     * user has permanently denied the permission — use [openSettings] for that case.
     */
    fun request()

    /** Opens the app's settings page so the user can grant the permission manually. */
    fun openSettings()
}

/**
 * Remembers an [AppPermissionState] for any Calf [Permission].
 *
 * To support a permission that has no dedicated helper below, call this directly:
 * `rememberAppPermissionState(Permission.Bluetooth)` (after adding the matching
 * iOS usage-description key to Info.plist).
 *
 * @param onResult called with the grant result after [AppPermissionState.request].
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberAppPermissionState(
    permission: Permission,
    onResult: (Boolean) -> Unit = {},
): AppPermissionState {
    val calfState = rememberPermissionState(permission, onResult)
    return remember(calfState) { CalfAppPermissionState(calfState) }
}

/** Notification permission (Android 13+ POST_NOTIFICATIONS / iOS UNUserNotificationCenter). */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberNotificationPermissionState(onResult: (Boolean) -> Unit = {}): AppPermissionState = rememberAppPermissionState(Permission.Notification, onResult)

/** Camera permission. iOS requires NSCameraUsageDescription in Info.plist. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberCameraPermissionState(onResult: (Boolean) -> Unit = {}): AppPermissionState = rememberAppPermissionState(Permission.Camera, onResult)

/** Photo gallery permission. iOS requires NSPhotoLibraryUsageDescription in Info.plist. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberGalleryPermissionState(onResult: (Boolean) -> Unit = {}): AppPermissionState = rememberAppPermissionState(Permission.Gallery, onResult)

/** Precise location permission. iOS requires NSLocationWhenInUseUsageDescription in Info.plist. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationPermissionState(onResult: (Boolean) -> Unit = {}): AppPermissionState = rememberAppPermissionState(Permission.FineLocation, onResult)

/** Microphone permission. iOS requires NSMicrophoneUsageDescription in Info.plist. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberMicrophonePermissionState(onResult: (Boolean) -> Unit = {}): AppPermissionState = rememberAppPermissionState(Permission.RecordAudio, onResult)

/**
 * Requests [permissionState] once when it enters composition, unless already granted
 * or the user previously denied it (in which case prompting again unasked is hostile UX —
 * surface a rationale UI and call [AppPermissionState.request] from a button instead).
 */
@Composable
fun RequestPermissionOnEntry(permissionState: AppPermissionState) {
    LaunchedEffect(Unit) {
        if (!permissionState.isGranted && !permissionState.shouldShowRationale) {
            permissionState.request()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private class CalfAppPermissionState(
    private val delegate: PermissionState,
) : AppPermissionState {
    override val isGranted: Boolean get() = delegate.status.isGranted
    override val shouldShowRationale: Boolean get() = delegate.status.shouldShowRationale
    override fun request() = delegate.launchPermissionRequest()
    override fun openSettings() = delegate.openAppSettings()
}
