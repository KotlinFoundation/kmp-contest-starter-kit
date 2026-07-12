package com.kotlinfoundation.koko.presentation.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NavigatorTest {

    private fun newState(
        start: ScreenRoute = HomeScreenRoute,
        topLevel: Set<TopLevelScreenRoute> = setOf(
            HomeScreenRoute,
            GalleryScreenRoute,
            AccountScreenRoute,
        ),
        primary: TopLevelScreenRoute = HomeScreenRoute,
    ): NavigationState {
        val topLevelBackStacks: Map<TopLevelScreenRoute, SnapshotStateList<ScreenRoute>> =
            topLevel.associateWith { route -> mutableStateListOf<ScreenRoute>(route) }
        val defaultBackstack: SnapshotStateList<ScreenRoute> =
            if (start is TopLevelScreenRoute) mutableStateListOf() else mutableStateListOf(start)
        val currentBackstack: SnapshotStateList<ScreenRoute> = mutableStateListOf<ScreenRoute>().apply {
            if (start is TopLevelScreenRoute) addAll(topLevelBackStacks.getValue(start)) else addAll(defaultBackstack)
        }
        return NavigationState(
            topLevelBackStacks = topLevelBackStacks,
            defaultBackstack = defaultBackstack,
            primaryTopLevelRoute = primary,
            currentBackstack = currentBackstack,
        )
    }

    @Test
    fun `add pushes onto the current back stack`() {
        val nav = Navigator(newState())
        nav.add(ProfileScreenRoute)
        assertEquals(listOf(HomeScreenRoute, ProfileScreenRoute), nav.state.currentBackstack.toList())
    }

    @Test
    fun `navigate pushes a non-top-level route`() {
        val nav = Navigator(newState())
        nav.navigate(ProfileScreenRoute)
        assertEquals(ProfileScreenRoute, nav.state.currentBackstack.last())
    }

    @Test
    fun `navigate to a top-level route switches tab`() {
        val nav = Navigator(newState())
        nav.navigate(GalleryScreenRoute)
        assertEquals(GalleryScreenRoute, nav.state.topLevelRoute)
    }

    @Test
    fun `each tab keeps its own back stack`() {
        val nav = Navigator(newState())
        nav.add(ProfileScreenRoute) // Home tab: [Home, Profile]
        nav.switchToTab(GalleryScreenRoute) // Gallery tab: [Gallery]
        assertEquals(listOf(GalleryScreenRoute), nav.state.currentBackstack.toList())

        nav.switchToTab(HomeScreenRoute) // Home restored with its pushed entry
        assertEquals(listOf(HomeScreenRoute, ProfileScreenRoute), nav.state.currentBackstack.toList())
    }

    @Test
    fun `switching to the active tab is a no-op`() {
        val nav = Navigator(newState())
        nav.add(ProfileScreenRoute)
        nav.switchToTab(HomeScreenRoute)
        assertEquals(listOf(HomeScreenRoute, ProfileScreenRoute), nav.state.currentBackstack.toList())
    }

    @Test
    fun `goBack pops when more than one entry`() {
        val nav = Navigator(newState())
        nav.add(ProfileScreenRoute)
        nav.goBack()
        assertEquals(listOf(HomeScreenRoute), nav.state.currentBackstack.toList())
    }

    @Test
    fun `goBack from a non-primary tab returns to the primary tab`() {
        val nav = Navigator(newState())
        nav.switchToTab(GalleryScreenRoute)
        nav.goBack()
        assertEquals(HomeScreenRoute, nav.state.topLevelRoute)
    }

    @Test
    fun `goBack on the primary tab root is a no-op`() {
        val nav = Navigator(newState())
        nav.goBack()
        assertEquals(listOf(HomeScreenRoute), nav.state.currentBackstack.toList())
        assertEquals(HomeScreenRoute, nav.state.topLevelRoute)
    }

    @Test
    fun `set clears the back stack and places the route on top`() {
        val nav = Navigator(newState())
        nav.add(ProfileScreenRoute)
        nav.set(SubscriptionsScreenRoute)
        assertEquals(listOf(SubscriptionsScreenRoute), nav.state.currentBackstack.toList())
    }

    @Test
    fun `replace swaps the top entry`() {
        val nav = Navigator(newState())
        nav.add(ProfileScreenRoute)
        nav.replace(SubscriptionsScreenRoute)
        assertEquals(listOf(HomeScreenRoute, SubscriptionsScreenRoute), nav.state.currentBackstack.toList())
    }

    @Test
    fun `switching to an unregistered top-level route fails fast`() {
        // AccountScreenRoute is deliberately left out of the registered tab set.
        val nav = Navigator(newState(topLevel = setOf(HomeScreenRoute, GalleryScreenRoute)))
        assertFailsWith<IllegalArgumentException> {
            nav.switchToTab(AccountScreenRoute)
        }
    }
}
