package com.kotlinfoundation.koko.data.source.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import com.kotlinfoundation.koko.data.source.local.entity.GenerationOutputEntity
import kotlinx.coroutines.flow.Flow

// Room DAO for the local generation history (gallery). Flows emit on every table change.
@Dao
interface GenerationOutputDao {
    @Query("SELECT * FROM generation_outputs WHERE id = :id")
    suspend fun getById(id: String): GenerationOutputEntity?

    @Query("SELECT * FROM generation_outputs WHERE id = :id")
    fun getByIdFlow(id: String): Flow<GenerationOutputEntity?>

    @Query("SELECT * FROM generation_outputs ORDER BY created_at DESC")
    fun getAllFlow(): Flow<List<GenerationOutputEntity>>

    @Query("SELECT * FROM generation_outputs")
    suspend fun getAll(): List<GenerationOutputEntity>

    @Upsert
    suspend fun upsert(entity: GenerationOutputEntity)

    @Query("DELETE FROM generation_outputs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Delete
    suspend fun delete(entity: GenerationOutputEntity)

    @Query("DELETE FROM generation_outputs")
    suspend fun deleteAll()
}
