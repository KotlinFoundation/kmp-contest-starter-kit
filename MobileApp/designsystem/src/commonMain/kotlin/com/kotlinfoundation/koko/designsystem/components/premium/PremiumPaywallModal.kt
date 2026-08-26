package com.kotlinfoundation.koko.designsystem.components.premium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlinfoundation.koko.designsystem.components.AppButton
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// TODO Update Premium Benefits according to app itself
internal fun defaultPremiumBenefits(): List<PremiumBenefit> = listOf(
    PremiumBenefit(
        icon = "🚀",
        title = "Unlimited Generations",
        description = "Create as much as you want, with no monthly cap.",
    ),
    PremiumBenefit(
        icon = "🤖",
        title = "Premium AI Models",
        description = "Access the highest quality models available.",
    ),
    PremiumBenefit(
        icon = "✨",
        title = "Priority Processing",
        description = "Skip the queue — your requests run first.",
    ),
    PremiumBenefit(
        icon = "💎",
        title = "No Watermark",
        description = "Clean, ready-to-share results every single time.",
    ),
)

// Premium benefits for soft paywall
data class PremiumBenefit(
    val icon: String,
    val title: String,
    val description: String,
)

@Composable
fun PremiumPaywallModal(
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
    titleText: String = "Get More With Premium",
    descriptionText: String = "Unlock unlimited generations, premium AI models, and faster processing.",
    benefits: List<PremiumBenefit> = defaultPremiumBenefits(),
    priceText: String = "$14.99",
    priceSuffixText: String = "/month",
    ctaBtnEnabled: Boolean = true,
    subtitleText: String = "7-day free trial • Cancel anytime",
    ctaBtnText: String = "Start Free Trial",
    dismissBtnText: String = "Maybe Later",
    bottomExtraContent: (@Composable () -> Unit)? = null,
) {
    val premiumBenefits = benefits
    val infiniteTransition = rememberInfiniteTransition(label = "paywall_effects")

    val crownBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec =
        infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "crown_bounce",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top section with dismiss button
        Box(modifier = Modifier.fillMaxWidth()) {
            SkipButton(modifier = Modifier.align(Alignment.TopEnd), text = dismissBtnText, onClick = onDismiss)
        }

        // Scrollable content section
        Column(
            modifier =
            Modifier
                .weight(1f)
                .padding(bottom = AppTheme.spacing.defaultSpacing)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = AppTheme.spacing.cardContentSpacing,
                    vertical = AppTheme.spacing.sectionSpacing,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
        ) {
            // Premium crown icon with bounce animation and scaling
            Text(
                text = "💸",
                fontSize = 64.sp,
                modifier = Modifier.scale((1f + crownBounce * 0.02f)),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacingSmall),
            ) {
                // Compelling headline
                Text(
                    text = titleText,
                    style = AppTheme.typography.h4,
                    color = AppTheme.colors.text.primary,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = descriptionText,
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.text.secondary,
                    textAlign = TextAlign.Center,
                )
            }

            // Premium benefits with staggered entrance
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
            ) {
                premiumBenefits.forEachIndexed { index, benefit ->
                    var isVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        delay(150.milliseconds * index)
                        isVisible = true
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter =
                        slideInVertically(
                            initialOffsetY = { 30 },
                            animationSpec = spring(dampingRatio = 0.8f),
                        ) + fadeIn(),
                    ) {
                        PremiumBenefitCard(benefit = benefit)
                    }
                }
            }
        }

        // Fixed bottom section with pricing and buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.verticalListItemSpacingSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Pricing with gradient background
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        brush =
                        Brush.horizontalGradient(
                            colors =
                            listOf(
                                AppTheme.colors.primary.copy(alpha = 0.1f),
                                AppTheme.colors.primary.copy(alpha = 0.2f),
                            ),
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ).padding(AppTheme.spacing.cardContentSpacing),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacingSmall),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = priceText,
                            style = AppTheme.typography.h3,
                            color = AppTheme.colors.primary,
                        )
                        Text(
                            text = priceSuffixText,
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.text.secondary,
                        )
                    }
                    Text(
                        text = subtitleText,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // CTA Button
            AppButton(
                onClick = onUpgrade,
                enabled = ctaBtnEnabled,
                modifier = Modifier.fillMaxWidth(),
                text = ctaBtnText,
            )
            if (bottomExtraContent != null) {
                bottomExtraContent()
            }
        }
    }
}

@Composable
private fun PremiumBenefitCard(
    benefit: PremiumBenefit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.primary.copy(alpha = 0.05f))
            .padding(AppTheme.spacing.cardContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.inputIconTextSpacing),
    ) {
        // Icon with shimmer effect for highlighted benefits
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = benefit.icon,
                fontSize = 24.sp,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = benefit.title,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.primary,
            )
            Text(
                text = benefit.description,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun SkipButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    TextButton(
        // This 12 dp extra padding comes from Material Design so we remove that
        modifier = modifier.offset(x = 12.dp),
        onClick = { onClick() },
    ) {
        Text(
            text = text,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text.secondary,
        )
    }
}
