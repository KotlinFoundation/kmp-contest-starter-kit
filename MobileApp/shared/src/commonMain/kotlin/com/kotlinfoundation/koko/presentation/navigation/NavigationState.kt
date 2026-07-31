package com.kotlinfoundation.koko.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer

/**
 * Creates a [NavigationState] whose back stacks survive config changes / process death
 * (`rememberSerializable` over the polymorphic [ScreenRoute] hierarchy). Each top-level route
 * gets its own back stack; non-tab start routes use the default stack.
 */
@Composable
fun rememberNavigationState(
    startRoute: ScreenRoute,
    topLevelRoutes: Set<TopLevelScreenRoute>,
    primaryTopLevelRoute: TopLevelScreenRoute,
): NavigationState {
    val topLevelBackStacks: Map<TopLevelScreenRoute, SnapshotStateList<ScreenRoute>> = buildMap {
        topLevelRoutes.forEach { route ->
            put(
                route,
                rememberSerializable(serializer = SnapshotStateListSerializer<ScreenRoute>()) {
                    mutableStateListOf(route)
                },
            )
        }
    }

    val defaultBackstack =
        rememberSerializable(serializer = SnapshotStateListSerializer<ScreenRoute>()) {
            if (startRoute !is TopLevelScreenRoute) {
                mutableStateListOf(startRoute)
            } else {
                mutableStateListOf()
            }
        }

    val currentBackstack =
        rememberSerializable(serializer = SnapshotStateListSerializer<ScreenRoute>()) {
            val initial = if (startRoute is TopLevelScreenRoute) {
                requireNotNull(topLevelBackStacks[startRoute]) {
                    "startRoute $startRoute is a TopLevelScreenRoute but is not registered in topLevelRoutes"
                }
            } else {
                defaultBackstack
            }
            initial.toMutableStateList()
        }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            primaryTopLevelRoute = primaryTopLevelRoute,
            topLevelBackStacks = topLevelBackStacks,
            defaultBackstack = defaultBackstack,
            currentBackstack = currentBackstack,
        )
    }
}

/**
 * Holds the navigation back stacks as plain observable state. [currentBackstack] is what's
 * rendered; switching tabs swaps it with the active tab's stored stack (see [topLevelRoute]).
 * Mutated through [Navigator] — screens don't touch this directly.
 */
class NavigationState(
    val topLevelBackStacks: Map<TopLevelScreenRoute, SnapshotStateList<ScreenRoute>>,
    val defaultBackstack: SnapshotStateList<ScreenRoute>,
    val primaryTopLevelRoute: TopLevelScreenRoute,
    val currentBackstack: SnapshotStateList<ScreenRoute>,
) {

    var topLevelRoute: TopLevelScreenRoute?
        get() = currentBackstack.firstOrNull() as? TopLevelScreenRoute
        set(value) {
            val oldRoute = topLevelRoute
            val oldStorage =
                if (oldRoute != null) topLevelBackStacks[oldRoute] ?: defaultBackstack else defaultBackstack
            oldStorage.clear()
            oldStorage.addAll(currentBackstack)
            val newStorage = if (value != null) {
                requireNotNull(topLevelBackStacks[value]) {
                    "Top-level route $value must be registered in topLevelRoutes"
                }
            } else {
                defaultBackstack
            }
            currentBackstack.clear()
            currentBackstack.addAll(newStorage)
        }

    @Composable
    fun toDecoratedEntries(entryProvider: (ScreenRoute) -> NavEntry<ScreenRoute>): List<NavEntry<ScreenRoute>> {
        val saveable = rememberSaveableStateHolderNavEntryDecorator<ScreenRoute>()
        val viewModel = rememberViewModelStoreNavEntryDecorator<ScreenRoute>()
        val decorators: List<NavEntryDecorator<ScreenRoute>> = remember(saveable, viewModel) {
            listOf(saveable, viewModel)
        }

        val topLevelEntries = topLevelBackStacks.mapValues { (route, stack) ->
            rememberDecoratedNavEntries(
                backStack = if (route == topLevelRoute) currentBackstack else stack,
                entryDecorators = decorators,
                entryProvider = entryProvider,
            )
        }.withDefault { emptyList() }

        val defaultEntries = rememberDecoratedNavEntries(
            backStack = if (topLevelRoute == null) currentBackstack else defaultBackstack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )

        return when (val topRoute = topLevelRoute) {
            null -> defaultEntries

            primaryTopLevelRoute -> topLevelEntries.getValue(primaryTopLevelRoute)

            else -> topLevelEntries.getValue(primaryTopLevelRoute) + topLevelEntries.getValue(
                topRoute,
            )
        }
    }
}
