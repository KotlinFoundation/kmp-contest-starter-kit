package com.kotlinfoundation.kmpstarterkit.util

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.currentKoinScope
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope

/** Base class for all UiStateHolders (the project's ViewModel equivalent). */
abstract class UiStateHolder : ViewModel()

/** The holder's lifecycle-bound coroutine scope (cancelled when the screen/entry is gone). */
val UiStateHolder.uiStateHolderScope: CoroutineScope get() = viewModelScope

/** Obtains a Koin-scoped UiStateHolder for the current NavEntry. Used by AppNavigation entries. */
@Composable
inline fun <reified T : ViewModel> uiStateHolder(
    qualifier: Qualifier? = null,
    scope: Scope = currentKoinScope(),
    noinline parameters: ParametersDefinition? = null,
): T = koinViewModel(qualifier = qualifier, scope = scope, parameters = parameters)
