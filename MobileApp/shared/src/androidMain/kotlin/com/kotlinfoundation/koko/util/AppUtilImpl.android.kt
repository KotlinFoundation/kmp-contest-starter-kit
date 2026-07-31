package com.kotlinfoundation.koko.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.PackageInfoCompat
import com.kotlinfoundation.koko.root.AppConfiguration
import com.kotlinfoundation.koko.util.logging.AppLogger

class AppUtilImpl(private val context: Context) : AppUtil {

    override fun shareApp() {
        val flag = Intent.FLAG_ACTIVITY_NEW_TASK
        val appName = context.packageManager.getApplicationLabel(context.applicationInfo).toString()
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, appName)
            putExtra(
                Intent.EXTRA_TEXT,
                "\n" +
                    "\n${getPlayStoreLink()}",
            )
            flags = flag
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Choose one").apply {
                flags = flag
            },
        )
    }

    override fun openFeedbackMail() {
        try {
            val appName = getAppName()
            val subject = "$appName Feedback/Bug Report"
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfiguration.CONTACT_EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse("mailto:${AppConfiguration.CONTACT_EMAIL}?subject=${Uri.encode(subject)}")
            }
            context.startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {
            AppLogger.e("Activity not found to send email")
        }
    }

    override fun getAppName(): String = context.packageManager.getApplicationLabel(context.applicationInfo).toString()

    override fun getAppVersionInfo(): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = runCatching { packageInfo.versionName }.getOrDefault("")
        val versionCode =
            runCatching { PackageInfoCompat.getLongVersionCode(packageInfo).toString() }.getOrDefault("")
        return "$versionName ($versionCode)"
    }

    private fun getPlayStoreLink(): String {
        val appId = context.packageName
        return "https://play.google.com/store/apps/details?id=$appId"
    }
}
