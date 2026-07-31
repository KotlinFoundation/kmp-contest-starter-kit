package com.kotlinfoundation.koko.util.logging

import org.koin.core.component.KoinComponent

/** A log sink. Multiple implementations can be registered (Napier, Telegram, …); see [AppLogger]. */
interface Logger {
    // Should be called on Application start
    fun initialize(isDebug: Boolean = true)
    fun e(message: String, throwable: Throwable? = null, tag: String? = null)
    fun d(message: String, throwable: Throwable? = null, tag: String? = null)
    fun i(message: String, throwable: Throwable? = null, tag: String? = null)
}

// App-wide logging facade: fans every call out to all Logger implementations registered in Koin.
object AppLogger : Logger, KoinComponent {

    private val loggers = getKoin().getAll<Logger>()

    override fun initialize(isDebug: Boolean) {
        loggers.forEach { it.initialize(isDebug) }
    }

    override fun e(message: String, throwable: Throwable?, tag: String?) {
        loggers.forEach { it.e(message, throwable, tag) }
    }

    override fun d(message: String, throwable: Throwable?, tag: String?) {
        loggers.forEach { it.d(message, throwable, tag) }
    }

    override fun i(message: String, throwable: Throwable?, tag: String?) {
        loggers.forEach { it.i(message, throwable, tag) }
    }
}

fun AppLogger.logAppOpened() {
    val msg = "App is opened 🔓✨"
    i(msg)
}

fun AppLogger.logSuccessfulPurchase(extraInfo: String? = null) {
    val msg = "Purchase Success 💰✅\n ExtraInfo: $extraInfo"
    i(msg)
}
