package com.kotlinfoundation.kmpstarterkit.data.source.local

import org.w3c.dom.Worker

internal actual fun createSQLiteWorker(): Worker = js("new Worker(new URL('sqlite-wasm-worker/worker.js', import.meta.url), { type: 'module' })")
