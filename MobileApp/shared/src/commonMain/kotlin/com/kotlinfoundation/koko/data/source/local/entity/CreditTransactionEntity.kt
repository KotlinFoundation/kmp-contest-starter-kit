@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.kotlinfoundation.koko.data.source.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.kotlinfoundation.koko.domain.model.credit.CreditTransaction
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Room row for a credit ledger entry; indexed by created_at for recent-first queries.
// toModel()/toEntity() below convert to/from the domain CreditTransaction.
@Entity(
    tableName = "credit_transaction",
    indices = [Index(value = ["created_at"])],
)
data class CreditTransactionEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = Uuid.random().toString(),
    @ColumnInfo(name = "type") val type: CreditTransaction.Type,
    @ColumnInfo(name = "amount") val amount: Int,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo("created_at") val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
)

fun CreditTransactionEntity.toModel(): CreditTransaction = CreditTransaction(
    id = id,
    type = type,
    amount = amount,
    description = description,
    createdAt = createdAt,
)

fun CreditTransaction.toEntity(): CreditTransactionEntity = CreditTransactionEntity(
    id = id,
    type = type,
    amount = amount,
    description = description,
    createdAt = createdAt,
)
