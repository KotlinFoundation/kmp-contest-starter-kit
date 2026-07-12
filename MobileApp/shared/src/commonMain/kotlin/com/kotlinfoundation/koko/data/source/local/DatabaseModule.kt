package com.kotlinfoundation.koko.data.source.local

import com.kotlinfoundation.koko.util.defaultAsyncDispatcher
import org.koin.dsl.module

// Koin wiring for the database + DAOs. The single AppDatabase is built from the platform's
// DatabaseProvider.
val databaseModule = module {
    single<AppDatabase> {
        get<DatabaseProvider>()
            .provideAppDatabaseBuilder()
            // No real migrations yet: on any schema change Room drops & recreates all tables.
            // Replace with proper Migrations before shipping data you can't lose.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .setQueryCoroutineContext(defaultAsyncDispatcher)
            .build()
    }
    single { get<AppDatabase>().exampleDao() }
    single { get<AppDatabase>().creditTransactionDao() }
    single { get<AppDatabase>().generationOutputDao() }
    // Add new DAO accessors above — make_local.sh inserts here.
}
