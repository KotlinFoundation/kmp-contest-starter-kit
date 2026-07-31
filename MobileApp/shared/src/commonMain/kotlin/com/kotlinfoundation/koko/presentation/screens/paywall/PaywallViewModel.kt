package com.kotlinfoundation.koko.presentation.screens.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinfoundation.koko.data.repository.CreditRepository
import com.kotlinfoundation.koko.data.repository.SubscriptionRepository
import com.kotlinfoundation.koko.data.repository.UserRepository
import com.kotlinfoundation.koko.data.source.featureflag.FeatureFlagManager
import com.kotlinfoundation.koko.generated.resources.Res
import com.kotlinfoundation.koko.generated.resources.paywall_msg_credits_added
import com.kotlinfoundation.koko.generated.resources.paywall_msg_credits_not_added
import com.kotlinfoundation.koko.generated.resources.paywall_msg_sign_in_required
import com.kotlinfoundation.koko.root.AppGlobalUiState
import com.kotlinfoundation.koko.subscription.api.PurchaseError
import com.kotlinfoundation.koko.subscription.api.PurchaseEventsListener
import com.kotlinfoundation.koko.subscription.api.PurchasePackage
import com.kotlinfoundation.koko.subscription.api.PurchasePackageId
import com.kotlinfoundation.koko.subscription.api.SubscriptionProviderUser
import com.kotlinfoundation.koko.util.Constants
import com.kotlinfoundation.koko.util.Constants.CREDIT_PACK_PRODUCT_ID_PREFIX
import com.kotlinfoundation.koko.util.UiMessage
import com.kotlinfoundation.koko.util.extensions.isCreditPackProductId
import com.kotlinfoundation.koko.util.extensions.parseCreditAmountFromProductId
import com.kotlinfoundation.koko.util.logging.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val placementId: String?,
    private val subscriptionRepository: SubscriptionRepository,
    private val creditRepository: CreditRepository,
    private val userRepository: UserRepository,
    private val featureFlagManager: FeatureFlagManager,
    private val mapper: PaywallUiStateMapper = PaywallUiStateMapper(),
) : ViewModel() {
    private val mode: PaywallMode =
        if (placementId == Constants.PAYWALL_PLACEMENT_CREDITS_PACK) {
            PaywallMode.CREDIT_PACK
        } else {
            PaywallMode.SUBSCRIPTION
        }

    private var rawPackages: List<PurchasePackage> = emptyList()
    private var selectedPackageId: PurchasePackageId? = null

    private val _uiState = MutableStateFlow(
        PaywallUiState(
            mode = mode,
            currentPlacementId = placementId,
            isMock = subscriptionRepository.isMockProvider,
        ),
    )
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        if (featureFlagManager.getBoolean(FeatureFlagManager.Keys.SHOW_REMOTE_PAYWALL)) {
            // Native Adapty/RC paywall owns its own loading + package fetch.
            _uiState.update { it.copy(isLoading = false) }
        } else {
            fetchPackages()
        }
    }

    val remotePaywallPurchaseEventsListener: PurchaseEventsListener =
        object : PurchaseEventsListener {
            override fun onDismiss() {
                _uiState.update { it.copy(isDismissRequired = true) }
            }

            override fun onLoadingStateChanged(isLoading: Boolean) {
                _uiState.update { it.copy(isLoading = isLoading) }
            }

            override fun onUnknownError(error: Exception) {
                AppLogger.e("Unknown error occurred on paywall", error)
                _uiState.update {
                    it.copy(
                        errorMessage = UiMessage.Message(error.message),
                        isLoading = false,
                    )
                }
            }

            override fun onPurchaseSuccess(
                info: SubscriptionProviderUser,
                productIds: List<String>,
            ) {
                successfulPurchase(info, productIds)
            }

            override fun onRestoreSuccess(info: SubscriptionProviderUser) {
                successfulRestore(info)
            }

            override fun onPurchaseFailure(error: PurchaseError) {
                failedPurchase(error = Throwable(error.message))
            }

            override fun onRestoreFailure(error: PurchaseError) {
                failedRestore(error = Throwable(error.message))
            }
        }

    fun onMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onSignInActionHandled() = viewModelScope.launch {
        _uiState.update { it.copy(signInActionRequired = false) }
    }

    fun onPaywallDismissActionHandled() {
        subscriptionRepository.onPaywallDismissed()
        _uiState.update { it.copy(isDismissRequired = false) }
    }

    fun onUiEvent(event: PaywallUiEvent) {
        when (event) {
            PaywallUiEvent.OnClickBuy -> buyPackage()

            PaywallUiEvent.OnClickRestore -> restorePayment()

            is PaywallUiEvent.OnSelectPackage -> {
                selectedPackageId = event.packageId
                rebuildState()
            }
        }
    }

    private fun fetchPackages() = viewModelScope.launch {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                packages = emptyList(),
                buyButtonEnabled = false,
            )
        }
        subscriptionRepository.getPackageList(placementId = placementId)
            .onSuccess { packages -> handleLoaded(packages) }
            .onFailure { error -> handleError(error) }
    }

    private fun handleLoaded(packages: List<PurchasePackage>) {
        rawPackages = if (mode == PaywallMode.CREDIT_PACK) {
            packages.sortedBy { it.price.amount }
        } else {
            packages.sortedBy { it.period?.approximateDays ?: Int.MAX_VALUE }
        }
        selectedPackageId = mapper.pickDefaultSelection(rawPackages, mode)
        rebuildState()
    }

    private fun handleError(error: Throwable) {
        AppLogger.e("Error getting packages: $error")
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = UiMessage.Message(error.message),
                buyButtonEnabled = false,
            )
        }
    }

    private fun rebuildState() {
        val mapped = mapper.map(rawPackages, selectedPackageId, mode)
        val hasSelection = rawPackages.any { it.id == selectedPackageId }
        _uiState.update {
            it.copy(
                isLoading = false,
                packages = mapped.packages,
                buyButtonEnabled = hasSelection,
                ctaText = mapped.ctaText,
                aboveCtaText = mapped.aboveCtaText,
                belowCtaText = mapped.belowCtaText,
            )
        }
    }

    private fun restorePayment() = viewModelScope.launch {
        if (userCanDoPaymentAction().not()) {
            AppGlobalUiState.showUiMessage(UiMessage.Resource(Res.string.paywall_msg_sign_in_required))
            _uiState.update { it.copy(signInActionRequired = true) }
            return@launch
        }
        _uiState.update { it.copy(isLoading = true) }
        subscriptionRepository.restorePurchase()
            .onSuccess { purchaserInfo -> successfulRestore(purchaserInfo) }
            .onFailure { error -> failedRestore(error) }
    }

    private fun buyPackage() = viewModelScope.launch {
        // The mock provider simulates purchases with no backend, so it needs no signed-in user —
        // skip the sign-in gate so the demo flow works with zero config (no Firebase/auth).
        if (!subscriptionRepository.isMockProvider && userCanDoPaymentAction().not()) {
            AppGlobalUiState.showUiMessage(UiMessage.Resource(Res.string.paywall_msg_sign_in_required))
            _uiState.update { it.copy(signInActionRequired = true) }
            return@launch
        }
        val selected = rawPackages.firstOrNull { it.id == selectedPackageId } ?: return@launch
        _uiState.update { it.copy(buyButtonEnabled = false) }
        subscriptionRepository.purchase(selected.id)
            .onSuccess { purchaserInfo ->
                successfulPurchase(
                    subscriptionProviderUser = purchaserInfo,
                    productIds = listOf(selected.id.value),
                )
            }
            .onFailure { error -> failedPurchase(error) }
    }

    private fun successfulPurchase(
        subscriptionProviderUser: SubscriptionProviderUser,
        productIds: List<String>,
    ) = viewModelScope.launch {
        AppLogger.d("Successful payment, onPurchaseCompleted")
        _uiState.update { it.copy(isLoading = true) }
        val productId = productIds.firstOrNull()
        val isCreditPack = productId.isCreditPackProductId()

        if (isCreditPack && productId != null) {
            onSuccessfulCreditPack(productId)
            _uiState.update { it.copy(isDismissRequired = true, isLoading = false) }
            return@launch
        }

        val premiumSubscription = with(subscriptionRepository) {
            subscriptionProviderUser.asPremiumSubscription()
        }
        _uiState.update {
            it.copy(
                buyButtonEnabled = true,
                isLoading = false,
                successfulSubscription = premiumSubscription,
            )
        }
    }

    private fun successfulRestore(subscriptionProviderUser: SubscriptionProviderUser) = viewModelScope.launch {
        AppLogger.d("Successful restoring purchase: $subscriptionProviderUser")
        _uiState.update { it.copy(isLoading = true) }
        val premiumSubscription =
            with(subscriptionRepository) { subscriptionProviderUser.asPremiumSubscription() }
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                successfulSubscription = premiumSubscription,
            )
        }
    }

    private fun failedPurchase(error: Throwable) = viewModelScope.launch {
        AppLogger.e("There was an error with purchase: $error")
        _uiState.update {
            it.copy(
                buyButtonEnabled = true,
                errorMessage = UiMessage.Message(error.message),
            )
        }
    }

    private fun failedRestore(error: Throwable) = viewModelScope.launch {
        AppLogger.e("Error restoring purchases: $error")
        _uiState.update { state ->
            state.copy(
                errorMessage = UiMessage.Message(error.message),
                isLoading = false,
            )
        }
    }

    private suspend fun onSuccessfulCreditPack(productId: String) {
        AppLogger.d("Successful credit pack is purchased: $productId")
        val amountPart = productId.parseCreditAmountFromProductId()
        if (amountPart == null) {
            AppLogger.e(
                "Invalid credit pack product id: $productId. " +
                    "Must start with $CREDIT_PACK_PRODUCT_ID_PREFIX and contain a number. Example: credit_pack_50",
            )
            // Purchase succeeded but we can't determine the credit amount — fail loud so the
            // user (and support) know credits weren't granted, instead of silently swallowing it.
            AppGlobalUiState.showUiMessage(UiMessage.Resource(Res.string.paywall_msg_credits_not_added))
            return
        }
        AppLogger.d("Successful credit is added, amount: $amountPart")
        creditRepository.addCredits(amountPart)
        AppGlobalUiState.showUiMessage(UiMessage.Resource(Res.string.paywall_msg_credits_added, amountPart))
    }

    private suspend fun userCanDoPaymentAction(): Boolean {
        val currentUser = userRepository.currentUser.first().getOrNull()
        return currentUser != null
    }
}
