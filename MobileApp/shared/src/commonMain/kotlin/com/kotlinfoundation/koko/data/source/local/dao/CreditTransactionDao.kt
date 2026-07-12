package com.kotlinfoundation.koko.data.source.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import com.kotlinfoundation.koko.data.source.local.entity.CreditTransactionEntity
import kotlinx.coroutines.flow.Flow

// Room DAO for the credit ledger. getRecentsFlow backs the credit-balance screen's history list.
@Dao
interface CreditTransactionDao {
    @Query("SELECT * FROM credit_transaction WHERE id = :id")
    suspend fun getById(id: String): CreditTransactionEntity?

    @Query("SELECT * FROM credit_transaction WHERE id = :id")
    fun getByIdFlow(id: String): Flow<CreditTransactionEntity?>

    @Query("SELECT * FROM credit_transaction ORDER BY created_at DESC")
    fun getAllFlow(): Flow<List<CreditTransactionEntity>>

    @Query("SELECT * FROM credit_transaction")
    suspend fun getAll(): List<CreditTransactionEntity>

    @Query("SELECT * FROM credit_transaction ORDER BY created_at DESC LIMIT :limit")
    fun getRecentsFlow(limit: Int): Flow<List<CreditTransactionEntity>>

    @Query("SELECT * FROM credit_transaction ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecents(limit: Int): List<CreditTransactionEntity>

    @Upsert
    suspend fun upsert(entity: CreditTransactionEntity)

    @Query("DELETE FROM credit_transaction WHERE id = :id")
    suspend fun deleteById(id: String)

    @Delete
    suspend fun delete(entity: CreditTransactionEntity)

    @Query("DELETE FROM credit_transaction")
    suspend fun deleteAll()
}
