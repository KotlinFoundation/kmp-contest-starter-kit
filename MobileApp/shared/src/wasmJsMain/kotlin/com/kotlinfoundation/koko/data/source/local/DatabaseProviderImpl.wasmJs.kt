package com.kotlinfoundation.koko.data.source.local

import org.w3c.dom.Worker

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => new Worker(new URL('sqlite-wasm-worker/worker.js', import.meta.url), { type: 'module' })")
internal actual external fun createSQLiteWorker(): Worker
