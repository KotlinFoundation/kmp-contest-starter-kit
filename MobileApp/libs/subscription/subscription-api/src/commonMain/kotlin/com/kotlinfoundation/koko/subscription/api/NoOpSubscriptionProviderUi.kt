package com.kotlinfoundation.koko.subscription.api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object NoOpSubscriptionProviderUi : SubscriptionProviderUi {
    @Composable
    override fun RemotePaywall(
        placementId: String?,
        listener: PurchaseEventsListener,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Paywall UI is not supported yet on this platform")
            Button(
                onClick = { listener.onDismiss() },
            ) {
                Text("Go Back")
            }
        }
    }
}
