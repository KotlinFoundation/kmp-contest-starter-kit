package com.kotlinfoundation.koko.presentation.components.credit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.components.AppCardContainer
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.domain.model.credit.RecurringCredit
import com.kotlinfoundation.koko.util.extensions.asRelativeTimeString
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@Composable
fun RecurringCreditsStatusBox(recurringCredits: List<RecurringCredit>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
    ) {
        recurringCredits.forEach { recurringCredit ->
            RecurringCreditCardItem(recurringCredit)
        }
    }
}

@Composable
private fun RecurringCreditCardItem(recurringCredit: RecurringCredit) {
    AppCardContainer(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
        ) {
            Text(
                text = "Recurring Credits",
                style = AppTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.text.primary,
            )

            val creditLimit = recurringCredit.total
            val usedCredits = recurringCredit.used
            val remainingCredits = recurringCredit.remaining
            val progress = recurringCredit.progress
            val renewsInDays = recurringCredit.renewableDuration.inWholeDays

            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "$usedCredits / $creditLimit credits used",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.primary,
                        fontWeight = FontWeight.Medium,
                    )

                    Text(
                        text = "$remainingCredits remaining",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppTheme.colors.primary.copy(alpha = 0.2f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AppTheme.colors.primary),
                    )
                }

                val nextRenewalTime = recurringCredit.nextRenewalTime

                val nextRefillText = if (nextRenewalTime != null) {
                    val relativeTimeUntilRenew = nextRenewalTime.asRelativeTimeString().lowercase()
                    "Next refill $relativeTimeUntilRenew"
                } else {
                    "Refill time unknown"
                }

                Text(
                    text = "Credits refill every $renewsInDays days • $nextRefillText",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun RecurringCreditStatusBoxPreview() {
    val recurringCredits = listOf(
        RecurringCredit(
            id = "premium_weekly",
            total = 4,
            remaining = 2,
            renewableDuration = 7.days,
            nextRenewalTime = Clock.System.now().plus(2.days).toEpochMilliseconds(),
        ),
        RecurringCredit(
            id = "premium_monthly",
            total = 40,
            remaining = 22,
            renewableDuration = 30.days,
            nextRenewalTime = Clock.System.now().plus(10.days).toEpochMilliseconds(),
        ),
    )

    AppTheme {
        RecurringCreditsStatusBox(recurringCredits = recurringCredits)
    }
}
