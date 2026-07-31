package com.kotlinfoundation.koko.data.source.local

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import com.kotlinfoundation.koko.util.Constants
import org.w3c.dom.Worker

// Shared by both web targets (js + wasmJs): Room 3 + sqlite-web (OPFS). The only thing
// that genuinely can't be shared is how the Web Worker is constructed (`@JsFun` for wasmJs
// vs. a `js("…")` literal for js), which is the `createSQLiteWorker()` expect below.
class DatabaseProviderImpl : DatabaseProvider {
    override fun provideAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> = Room.databaseBuilder<AppDatabase>(name = Constants.LOCAL_DB_STORAGE_NAME)
        .setDriver(WebWorkerSQLiteDriver(createSQLiteWorker()))
}

// Webpack rewrites `new Worker(new URL(...))` and bundles the worker, allowing the worker
// to use ES module `import` syntax. The path resolves against the `sqlite-wasm-worker` npm
// package wired in `shared/build.gradle.kts`.
internal expect fun createSQLiteWorker(): Worker
