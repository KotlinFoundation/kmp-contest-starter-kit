package com.kotlinfoundation.koko.util

import com.kotlinfoundation.koko.root.AppConfiguration
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI
import java.net.URLEncoder

class AppUtilImpl : AppUtil {

    override fun shareApp() {
        val link = getWebsiteOrStoreLink()

        // Copy to clipboard
        val selection = StringSelection(link)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)

        // Try opening browser
        runCatching {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(link))
            }
        }
    }

    override fun openFeedbackMail() {
        val subject = "${getAppName()} Feedback/Bug Report"
        val uri = URI(
            "mailto:${AppConfiguration.CONTACT_EMAIL}?subject=${encode(subject)}",
        )

        runCatching {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().mail(uri)
            }
        }.onFailure {
            // Fallback: open default browser mail handler
            runCatching {
                Desktop.getDesktop().browse(uri)
            }
        }
    }

    override fun getAppName(): String {
        return System.getProperty("app.name") ?: "Koko" // TODO Update name
    }

    override fun getAppVersionInfo(): String {
        return System.getProperty("app.version") ?: "1.0.0" // Update version
    }

    private fun getWebsiteOrStoreLink(): String {
        return "https://kotlinfoundation.org" // TODO Update url
    }

    private fun encode(text: String): String = URLEncoder.encode(text, Charsets.UTF_8)
}
