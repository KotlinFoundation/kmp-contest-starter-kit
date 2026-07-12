package com.kotlinfoundation.koko.designsystem.components.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.lifetime_premium_subscription_renewal_text
import com.kotlinfoundation.koko.designsystem.generated.resources.premium_subscription_non_recurring_text
import com.kotlinfoundation.koko.designsystem.generated.resources.premium_subscription_renewal_text
import com.kotlinfoundation.koko.designsystem.generated.resources.text_here
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import com.kotlinfoundation.koko.designsystem.util.appendLinkIfNotEmpty
import org.jetbrains.compose.resources.stringResource

@Composable
fun ManageSubscriptionText(
    subscriptionUrl: String,
    isLifetime: Boolean = false,
    expirationDate: String? = null,
    isRecurring: Boolean = true,
    // When set, tapping the "here" link runs this instead of opening [subscriptionUrl] — used by the
    // mock provider to cancel in-app (there's no real store to manage).
    onManageClick: (() -> Unit)? = null,
) {
    val annotatedString = when {
        isLifetime -> buildAnnotatedString {
            append(stringResource(UiRes.string.lifetime_premium_subscription_renewal_text))
        }

        expirationDate != null -> {
            val textRes =
                if (isRecurring) UiRes.string.premium_subscription_renewal_text else UiRes.string.premium_subscription_non_recurring_text
            val text = stringResource(textRes, expirationDate)
            val hereText = stringResource(UiRes.string.text_here)
            val clickableTextStyle = SpanStyle(
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
                color = AppTheme.colors.primary,
            )
            val nextBillingDateTextStyle = SpanStyle(
                fontWeight = FontWeight.SemiBold,
            )

            buildAnnotatedString {
                append(text.substringBefore(expirationDate))
                withStyle(style = nextBillingDateTextStyle) {
                    append(expirationDate)
                }
                val remainingText = text.substringAfter(expirationDate)
                append(remainingText.substringBefore(hereText))
                if (onManageClick != null) {
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "manage",
                            styles = TextLinkStyles(style = clickableTextStyle),
                        ) { onManageClick() },
                    ) { append(hereText) }
                } else {
                    withStyle(style = clickableTextStyle) {
                        appendLinkIfNotEmpty(subscriptionUrl, hereText)
                    }
                }
                append(remainingText.substringAfter(hereText))
            }
        }

        else -> buildAnnotatedString { }
    }
    Text(
        text = annotatedString,
        style = AppTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = AppTheme.colors.text.secondary,
    )
}

@Composable
@Preview
internal fun ManageSubscriptionTextPreview() {
    PreviewHelper {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing)) {
            val isLifetime = true
            val isRecurring = true
            val expirationDate = "01 Jan 2026"
            Text("Lifetime: $isLifetime, isRecurring: $isRecurring, expirationDate: $expirationDate")
            ManageSubscriptionText(
                subscriptionUrl = "",
                isLifetime = isLifetime,
                isRecurring = isRecurring,
                expirationDate = expirationDate,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing)) {
            val isLifetime = false
            val isRecurring = true
            val expirationDate = "01 Jan 2026"
            Text("Lifetime: $isLifetime, isRecurring: $isRecurring, expirationDate: $expirationDate")
            ManageSubscriptionText(
                subscriptionUrl = "",
                isLifetime = isLifetime,
                isRecurring = isRecurring,
                expirationDate = expirationDate,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing)) {
            val isLifetime = true
            val isRecurring = false
            val expirationDate = "01 Jan 2026"
            Text("Lifetime: $isLifetime, isRecurring: $isRecurring, expirationDate: $expirationDate")
            ManageSubscriptionText(
                subscriptionUrl = "",
                isLifetime = isLifetime,
                isRecurring = isRecurring,
                expirationDate = expirationDate,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing)) {
            val isLifetime = false
            val isRecurring = false
            val expirationDate = "01 Jan 2026"
            Text("Lifetime: $isLifetime, isRecurring: $isRecurring, expirationDate: $expirationDate")
            ManageSubscriptionText(
                subscriptionUrl = "",
                isLifetime = isLifetime,
                isRecurring = isRecurring,
                expirationDate = expirationDate,
            )
        }
    }
}
