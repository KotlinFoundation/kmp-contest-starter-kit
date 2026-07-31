package com.kotlinfoundation.koko.data.source.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kotlinfoundation.koko.data.source.local.entity.ExampleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Room 3 DAO test running against an in-memory database with the bundled
 * SQLite driver — same driver the app uses on Android/iOS/JVM, no emulator
 * needed. The pattern applies to any DAO in [AppDatabase].
 */
class ExampleDaoTest {

    private lateinit var database: AppDatabase

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `upsert inserts a new row and getById reads it back`() = runTest {
        val dao = database.exampleDao()
        val entity = ExampleEntity(id = "example-1", title = "first")

        dao.upsert(entity)

        assertEquals(entity, dao.getById("example-1"))
    }

    @Test
    fun `upsert updates an existing row instead of duplicating it`() = runTest {
        val dao = database.exampleDao()
        dao.upsert(ExampleEntity(id = "example-1", title = "first"))

        dao.upsert(ExampleEntity(id = "example-1", title = "renamed"))

        assertEquals(1, dao.getAll().size)
        assertEquals("renamed", dao.getById("example-1")?.title)
    }

    @Test
    fun `getAllFlow emits current table contents`() = runTest {
        val dao = database.exampleDao()
        dao.upsert(ExampleEntity(id = "a", title = "one"))
        dao.upsert(ExampleEntity(id = "b", title = "two"))

        val rows = dao.getAllFlow().first()

        assertEquals(setOf("a", "b"), rows.map { it.id }.toSet())
    }

    @Test
    fun `deleteById removes only the targeted row`() = runTest {
        val dao = database.exampleDao()
        dao.upsert(ExampleEntity(id = "keep", title = "keep"))
        dao.upsert(ExampleEntity(id = "drop", title = "drop"))

        dao.deleteById("drop")

        assertNull(dao.getById("drop"))
        assertEquals(listOf("keep"), dao.getAll().map { it.id })
    }

    @Test
    fun `deleteAll empties the table`() = runTest {
        val dao = database.exampleDao()
        dao.upsert(ExampleEntity(id = "a"))
        dao.upsert(ExampleEntity(id = "b"))

        dao.deleteAll()

        assertTrue(dao.getAll().isEmpty())
    }
}
