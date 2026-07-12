package com.kotlinfoundation.koko.data.source.local

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// Both wasmJs and js use Room 3 + sqlite-web (OPFS) with the same DI wiring. The only
// per-target difference (Web Worker construction) lives in `createSQLiteWorker()`, so this
// module is identical across web targets and needs no expect/actual.
internal val webDatabaseModule: Module = module {
    includes(databaseModule)
    singleOf(::DatabaseProviderImpl) bind DatabaseProvider::class
}
