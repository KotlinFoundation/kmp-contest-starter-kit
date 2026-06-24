@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.kotlinfoundation.kmpstarterkit.presentation.screens.onboarding

import com.kotlinfoundation.kmpstarterkit.data.source.preferences.FakeUserPreferences
import com.kotlinfoundation.kmpstarterkit.data.source.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnBoardingUiStateHolderTest {
    private val mainDispatcher = StandardTestDispatcher()
    private lateinit var userPreferences: FakeUserPreferences

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        userPreferences = FakeUserPreferences()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `first launch shows onboarding`() = runTest(mainDispatcher) {
        val holder = OnBoardingUiStateHolder(userPreferences)
        advanceUntilIdle()

        val state = holder.uiState.value
        assertFalse(state.onBoardIsShown)
        assertFalse(state.isLoading)
    }

    @Test
    fun `returning user skips onboarding`() = runTest(mainDispatcher) {
        userPreferences.putBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN, true)

        val holder = OnBoardingUiStateHolder(userPreferences)
        advanceUntilIdle()

        assertTrue(holder.uiState.value.onBoardIsShown)
    }

    @Test
    fun `clicking start persists the flag and completes onboarding`() = runTest(mainDispatcher) {
        val holder = OnBoardingUiStateHolder(userPreferences)
        advanceUntilIdle()

        holder.onUiEvent(OnBoardingUiEvent.OnClickStart)
        advanceUntilIdle()

        assertTrue(holder.uiState.value.onBoardIsShown)
        assertTrue(userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN))
    }

    @Test
    fun `premium CTA raises the paywall flag and paywall handling completes onboarding`() = runTest(mainDispatcher) {
        val holder = OnBoardingUiStateHolder(userPreferences)
        advanceUntilIdle()

        holder.onUiEvent(OnBoardingUiEvent.OnClickGetPremiumAccess)
        advanceUntilIdle()
        assertTrue(holder.uiState.value.isPremiumRequired)

        holder.onPaywallEventHandled()
        advanceUntilIdle()

        val state = holder.uiState.value
        assertFalse(state.isPremiumRequired)
        assertTrue(state.onBoardIsShown)
        assertEquals(true, userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN))
    }
}
