package com.kotlinfoundation.koko.presentation.navigation

/** Public navigation API over [NavigationState]: push, switch tab, replace, and go back. */
class Navigator(val state: NavigationState) {

    /**
     * top-level routes switch tabs, everything else is pushed onto the current back stack.
     */
    fun navigate(route: ScreenRoute) {
        if (route is TopLevelScreenRoute) switchToTab(route) else add(route)
    }

    fun add(route: ScreenRoute) {
        state.currentBackstack.add(route)
    }

    /** Clear the current back stack and place [route] at the top. */
    fun set(route: ScreenRoute) {
        state.currentBackstack.clear()
        if (route is TopLevelScreenRoute) {
            state.topLevelRoute = route
        } else {
            state.currentBackstack.add(route)
        }
    }

    /** Pop the top of the current back stack and push [route] in its place. */
    fun replace(route: ScreenRoute) {
        state.currentBackstack.removeLastOrNull()
        state.currentBackstack.add(route)
    }

    /** Switch the active top-level tab. Tapping the active tab is a no-op. */
    fun switchToTab(route: TopLevelScreenRoute) {
        if (route == state.topLevelRoute) return
        state.topLevelRoute = route
    }

    fun goBack() {
        val stack = state.currentBackstack
        when {
            stack.size > 1 -> stack.removeLastOrNull()

            state.topLevelRoute != state.primaryTopLevelRoute -> {
                state.topLevelRoute = state.primaryTopLevelRoute
            }
        }
    }
}
