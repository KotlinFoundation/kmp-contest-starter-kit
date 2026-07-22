package com.kotlinfoundation.koko.util.file

/**
 * OPFS (Origin Private File System) persistence for the web [FileManagerImpl]'s bytes, so generated /
 * saved images survive a page reload or a new tab — the in-memory map alone is session-scoped.
 *
 * Bytes cross the JS boundary as **base64 strings** (clean on both js and wasmJs; avoids typed-array
 * interop differences). Files live under an `koko_files/` OPFS subdirectory so they don't collide with
 * the SQLite database file at the OPFS root. The only per-target difference is these two functions
 * (`@JsFun` on wasmJs, `js("…")` on js) — same split as `createSQLiteWorker()`.
 */
internal expect suspend fun opfsWriteFile(name: String, base64: String)

/** Reads every persisted file back as a JSON array `[{ "n": name, "d": base64 }, …]` (for warm-up). */
internal expect suspend fun opfsReadAllFiles(): String
