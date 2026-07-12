package com.kotlinfoundation.koko.presentation.components.credit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_coin_credits
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.UiText
import com.kotlinfoundation.koko.domain.model.credit.CreditTransaction
import com.kotlinfoundation.koko.util.extensions.asRelativeTimeString
import org.jetbrains.compose.resources.painterResource

@Composable
fun CreditTransactionsList(
    transactions: List<CreditTransaction>,
    modifier: Modifier = Modifier,
) {
    if (transactions.isEmpty()) {
        EmptyTransactionState(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            items(transactions, key = { it.id }) { transaction ->
                CreditTransactionItem(transaction = transaction)

                if (transaction != transactions.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = AppTheme.spacing.defaultSpacing),
                        color = AppTheme.colors.background,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditTransactionItem(transaction: CreditTransaction) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = transaction.description ?: transaction.type.displayText.value,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.text.primary,
            )

            Text(
                text = transaction.createdAt.asRelativeTimeString(),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
        }

        Spacer(modifier = Modifier.width(AppTheme.spacing.defaultSpacing))

        Text(
            text = "${if (transaction.amount > 0) "+" else ""}${transaction.amount}",
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (transaction.amount > 0) AppTheme.colors.status.success else AppTheme.colors.status.error,
        )
    }
}

@Composable
private fun EmptyTransactionState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        modifier = modifier.fillMaxWidth().padding(AppTheme.spacing.defaultSpacing),
    ) {
        Icon(
            painter = painterResource(UiRes.drawable.ic_coin_credits),
            contentDescription = null,
            tint = AppTheme.colors.text.secondary,
            modifier = Modifier.size(32.dp),
        )

        Text(
            text = "No transactions yet",
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.text.primary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Your credit transactions will appear here",
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.secondary,
            textAlign = TextAlign.Center,
        )
    }
}

private val CreditTransaction.Type.displayText: UiText
    get() = when (this) {
        CreditTransaction.Type.PURCHASE -> UiText.of("Purchased")
        CreditTransaction.Type.RECURRING -> UiText.of("Auto Refill Credits")
        CreditTransaction.Type.DEDUCTION -> UiText.of("Credits Used")
        CreditTransaction.Type.BONUS -> UiText.of("Bonus Credits")
        CreditTransaction.Type.ADMIN_ADJUSTMENT -> UiText.of("Credit Adjustment")
    }

@Preview
@Composable
private fun CreditTransactionItemDeductionPreview() {
    val transactions = listOf(
        CreditTransaction(
            id = "1",
            amount = 100,
            type = CreditTransaction.Type.PURCHASE,
            description = "Purchased 100 credits",
        ),

        CreditTransaction(
            id = "2",
            amount = 2,
            type = CreditTransaction.Type.DEDUCTION,
            description = "Used 1 credits",
        ),

        CreditTransaction(
            id = "3",
            amount = 10,
            type = CreditTransaction.Type.BONUS,
            description = "Welcome bonus",
        ),

        CreditTransaction(
            id = "4",
            amount = 3,
            type = CreditTransaction.Type.ADMIN_ADJUSTMENT,
            description = "Admin adjusted 3 credits",
        ),

    )

    AppTheme {
        CreditTransactionsList(
            transactions = transactions,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
