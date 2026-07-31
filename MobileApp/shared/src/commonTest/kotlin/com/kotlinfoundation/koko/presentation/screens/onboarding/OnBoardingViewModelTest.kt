@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.kotlinfoundation.koko.presentation.screens.onboarding

import com.kotlinfoundation.koko.data.source.preferences.FakeUserPreferences
import com.kotlinfoundation.koko.data.source.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnBoardingViewModelTest {
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
        val viewModel = OnBoardingViewModel(userPreferences)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isOnBoardingFinished)
        assertFalse(state.isLoading)
    }

    @Test
    fun `returning user finishes as an existing user`() = runTest(mainDispatcher) {
        userPreferences.putBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN, true)

        val viewModel = OnBoardingViewModel(userPreferences)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isOnBoardingFinished)
        // Not a new user, so navigation skips the paywall and goes straight to main.
        assertFalse(state.isNewUser)
    }

    @Test
    fun `clicking start finishes onboarding as a new user and persists it`() = runTest(mainDispatcher) {
        val viewModel = OnBoardingViewModel(userPreferences)
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.OnClickStart)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isOnBoardingFinished)
        // isNewUser lets navigation decide paywall vs. main; onboarding stays paywall-unaware.
        assertTrue(state.isNewUser)
        assertTrue(userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN))
    }

    @Test
    fun `onFinishHandled resets the finished flag`() = runTest(mainDispatcher) {
        val viewModel = OnBoardingViewModel(userPreferences)
        advanceUntilIdle()

        viewModel.onUiEvent(OnBoardingUiEvent.OnClickStart)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isOnBoardingFinished)

        viewModel.onFinishHandled()
        assertFalse(viewModel.uiState.value.isOnBoardingFinished)
    }
}
