@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.kotlinfoundation.koko.data.repository

import com.kotlinfoundation.koko.data.source.local.dao.CreditTransactionDao
import com.kotlinfoundation.koko.data.source.local.entity.toEntity
import com.kotlinfoundation.koko.data.source.local.entity.toModel
import com.kotlinfoundation.koko.data.source.preferences.UserPreferences
import com.kotlinfoundation.koko.domain.exceptions.CreditRequiredException
import com.kotlinfoundation.koko.domain.exceptions.PurchaseRequiredException
import com.kotlinfoundation.koko.domain.model.credit.CreditRenewableType
import com.kotlinfoundation.koko.domain.model.credit.CreditSourceConfig
import com.kotlinfoundation.koko.domain.model.credit.CreditSystemConfig
import com.kotlinfoundation.koko.domain.model.credit.CreditTransaction
import com.kotlinfoundation.koko.domain.model.credit.RecurringCredit
import com.kotlinfoundation.koko.util.ApplicationScope
import com.kotlinfoundation.koko.util.logging.AppLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The single source of truth for the user's credit balance. Applies the [CreditSystemConfig]
 * rules (one-time bonuses + recurring refills), records a local ledger of [CreditTransaction]s,
 * and exposes the live [balance]. All state is local (DataStore + Room); guarded by a mutex so
 * concurrent grant/deduct stays consistent.
 */
class CreditRepository(
    private val config: CreditSystemConfig,
    private val creditTransactionDao: CreditTransactionDao,
    private val subscriptionRepository: SubscriptionRepository,
    private val userPreferences: UserPreferences,
    private val applicationScope: ApplicationScope,
) {

    private val mutex = Mutex()
    private val _balance = MutableStateFlow(0)
    val balance = _balance.asStateFlow().onStart {
        listenForSubscriptionUpdate()
    }

    var subscriptionUpdateJob: Job? = null

    init {
        listenForSubscriptionUpdate()
    }

    private fun listenForSubscriptionUpdate() {
        subscriptionUpdateJob?.cancel()
        subscriptionUpdateJob = applicationScope.launch {
            subscriptionRepository.currentSubscriptionFlow
                .collectLatest { refreshAndGetBalance() }
        }
    }

    suspend fun useCredits(amount: Int = 1, description: String? = null) = mutex.withLock {
        require(amount > 0) { "amount must be > 0" }

        val currentBalance = refreshAndGetBalance()

        if (currentBalance < amount) {
            // Out of credits: premium users can top up with a credit pack; free users are pushed
            // to subscribe first (PurchaseRequiredException routes to the subscription paywall).
            if (subscriptionRepository.hasPremiumAccess()) {
                throw CreditRequiredException()
            } else {
                throw PurchaseRequiredException()
            }
        }

        var requiredCredits = amount

        // Deduct from configured sources in order
        for (src in config.sources) {
            if (requiredCredits <= 0) break
            val creditSourceKey = getCreditSourceKey(src.id)
            val creditSourceAmount = userPreferences.getInt(creditSourceKey, 0) ?: 0
            if (creditSourceAmount > 0) {
                val deduct = minOf(creditSourceAmount, requiredCredits)
                userPreferences.putInt(creditSourceKey, creditSourceAmount - deduct)
                requiredCredits -= deduct
            }
        }

        // Deduct remaining from general credit balance
        val generalCreditBalance = userPreferences.getInt(KEY_CREDIT_GENERAL_BALANCE, 0) ?: 0
        if (generalCreditBalance >= requiredCredits) {
            userPreferences.putInt(
                KEY_CREDIT_GENERAL_BALANCE,
                generalCreditBalance - requiredCredits,
            )
        } else {
            // Shouldn't happen if balance check passed and refreshBalance made current balance >= requiredCredits
            AppLogger.e("IllegalState: General balance is not enough to deduct $requiredCredits credits")
            throw CreditRequiredException()
        }

        recordTransaction(
            amount = -amount,
            type = CreditTransaction.Type.DEDUCTION,
            description = description,
        )
        updateTotalUsedCredits(amount)
        refreshAndGetBalance()
    }

    suspend fun addCredits(
        amount: Int,
        type: CreditTransaction.Type = CreditTransaction.Type.PURCHASE,
        description: String? = null,
    ) = mutex.withLock {
        require(amount > 0) { "amount must be > 0" }

        val currentBalance = userPreferences.getInt(KEY_CREDIT_GENERAL_BALANCE, 0) ?: 0
        val updatedBalance = currentBalance + amount
        userPreferences.putInt(KEY_CREDIT_GENERAL_BALANCE, updatedBalance)
        recordTransaction(amount, type, description)
        refreshAndGetBalance()
    }

    fun getRecentTransactionsFlow(limit: Int = 100): Flow<List<CreditTransaction>> = creditTransactionDao.getRecentsFlow(limit = limit)
        .map { entities -> entities.map { it.toModel() } }
        .catch { error ->
            AppLogger.e("Error observing recent credit transactions: $error")
            emit(emptyList())
        }

    suspend fun getRecurringCredits(): List<RecurringCredit> {
        val renewableCreditSources = config.sources.filter {
            it.renewableType.duration > 0.days
        }
        return renewableCreditSources.mapNotNull { source ->
            val (keyCredits, keyReset, _) = getCreditSourceKeys(source.id)
            val remainingCredit = userPreferences.getInt(keyCredits, 0) ?: 0
            val nextRenewalTime = userPreferences.getLong(keyReset, null)
            nextRenewalTime?.let {
                RecurringCredit(
                    id = source.id,
                    total = source.amount,
                    renewableDuration = source.renewableType.duration,
                    remaining = remainingCredit,
                    nextRenewalTime = nextRenewalTime,
                )
            }
        }
    }

    private suspend fun refreshAndGetBalance(): Int {
        // 1. Refresh pre-configured credit sources
        for (source in config.sources) {
            refreshPreConfiguredCreditSource(source)
        }

        // 2. Sum credits from pre-configured sources
        var total = 0
        for (source in config.sources) {
            val keyCredits = getCreditSourceKey(source.id)
            total += userPreferences.getInt(keyCredits, 0) ?: 0
        }

        // 3. Add credits from purchases and usage
        val currentBalance = userPreferences.getInt(KEY_CREDIT_GENERAL_BALANCE, 0) ?: 0
        total += currentBalance

        _balance.update { total }
        return total
    }

    private suspend fun refreshPreConfiguredCreditSource(source: CreditSourceConfig) {
        val (keyCredits, keyReset, keyGranted) = getCreditSourceKeys(source.id)

        val now = Clock.System.now()

        // Handle one–time bonuses
        if (source.renewableType is CreditRenewableType.None) {
            val granted = userPreferences.getBoolean(keyGranted, false)
            val conditionMet = source.condition()
            if (!granted && conditionMet) {
                userPreferences.putInt(keyCredits, source.amount)
                userPreferences.putBoolean(keyGranted, true)

                recordTransaction(
                    amount = source.amount,
                    type = CreditTransaction.Type.BONUS,
                    description = source.id,
                )
            }
            return
        }

        // Handle conditions
        if (!source.condition()) {
            // If user loses condition → zero out the source and remove next renewal date
            userPreferences.putInt(keyCredits, 0)
            userPreferences.remove(keyReset)
            return
        }

        val nextResetMs = userPreferences.getLong(keyReset, null)
        val nextReset = nextResetMs?.let { Instant.fromEpochMilliseconds(it) }
        val resetPeriod = source.renewableType.duration

        // First-time initialization
        if (nextReset == null) {
            val newReset = now + resetPeriod
            userPreferences.putInt(keyCredits, source.amount)
            userPreferences.putLong(keyReset, newReset.toEpochMilliseconds())

            recordTransaction(
                amount = source.amount,
                type = CreditTransaction.Type.RECURRING,
                description = source.id,
            )

            return
        }

        // If time passed → reset forward until aligned
        if (now >= nextReset && resetPeriod.isPositive()) {
            var updatedReset = nextReset
            while (updatedReset <= now) {
                updatedReset += resetPeriod
            }

            userPreferences.putInt(keyCredits, source.amount)
            userPreferences.putLong(keyReset, updatedReset.toEpochMilliseconds())

            recordTransaction(
                amount = source.amount,
                type = CreditTransaction.Type.RECURRING,
                description = "Renewed: ${source.id}",
            )
        }
    }

    private fun recordTransaction(
        amount: Int,
        type: CreditTransaction.Type,
        description: String?,
    ) = applicationScope.launch {
        val transaction = CreditTransaction(
            id = Uuid.random().toString(),
            type = type,
            amount = amount,
            description = description,
        )

        creditTransactionDao.upsert(transaction.toEntity())
    }

    private suspend fun updateTotalUsedCredits(cost: Int) {
        val nbUsedCredits = userPreferences.getInt(KEY_NB_TOTAL_USED_CREDIT, 0) ?: 0
        val newUsedCredits = nbUsedCredits + cost
        userPreferences.putInt(KEY_NB_TOTAL_USED_CREDIT, newUsedCredits)

        // TODO Update analytics if needed or subscription attribute to define power users
    }

    private fun getCreditSourceKey(id: String): String = "${id}_credits"

    private fun getCreditSourceKeys(id: String): Triple<String, String, String> {
        val keyCredits = getCreditSourceKey(id)
        val keyReset = "${id}_next_reset_time"
        val keyGranted = "${id}_granted"
        return Triple(keyCredits, keyReset, keyGranted)
    }

    companion object {
        const val KEY_NB_TOTAL_USED_CREDIT = "KEY_NB_TOTAL_USED_CREDIT"
        const val KEY_CREDIT_GENERAL_BALANCE = "KEY_CREDIT_GENERAL_BALANCE"
    }
}
