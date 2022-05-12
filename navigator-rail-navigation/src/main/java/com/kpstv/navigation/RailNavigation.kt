package com.kpstv.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.navigationrail.NavigationRailView
import com.kpstv.navigation.base.navigation.internals.ActivityNavigationLifecycle
import com.kpstv.navigation.base.navigation.internals.FragmentNavigationLifecycle
import com.kpstv.navigation.base.navigation.internals.getOwner
import com.kpstv.navigation.base.navigation.internals.getSaveInstanceState
import com.kpstv.navigation.rail.RailNavigationImpl
import com.kpstv.navigation.rail.getRailNavigationState
import com.kpstv.navigation.rail.setRailNavigationState

/**
 * Set up navigation for [NavigationRailView] in [FragmentActivity] or [Fragment].
 *
 * This will automatically handle navigation & its state that can also
 * survive process death as well.
 *
 * Child fragments can implement [FragmentNavigator.Navigation.Callbacks] to get notified
 * when they are selected & re-selected again.
 */
fun FragmentNavigator.install(obj: FragmentNavigator.RailNavigation): RailNavigationController {
    val owner = getOwner()
    val savedStateInstance = getSaveInstanceState()
    return when (owner) {
        is FragmentActivity -> install(this, owner, savedStateInstance, obj) // Activity
        is Fragment -> install(this, owner, savedStateInstance, obj) // Fragment
        else -> throw IllegalStateException("Couldn't determine the owner type, this is the problem of something stupid I did. Kindly report to me!")
    }
}

internal fun install(
    navigator: FragmentNavigator,
    fragment: Fragment,
    savedStateInstance: Bundle?,
    obj: FragmentNavigator.RailNavigation
): RailNavigationController {
    val fragmentSavedState = savedStateInstance
        ?: if (fragment is ValueFragment) fragment.getRailNavigationState() else null

    val impl = RailNavigationImpl(
        navigator = navigator,
        fragments = obj.railNavigationFragments,
        getNavView = { fragment.requireView().findViewById(obj.railNavigationViewId) },
        onNavSelectionChange = obj::onRailNavigationSelectionChanged,
        navigation = obj
    )

    impl.onCreate(fragmentSavedState)

    fragment.parentFragmentManager.registerFragmentLifecycleCallbacks(
        FragmentNavigationLifecycle(fragment, impl) { f, bundle ->
            f.setRailNavigationState(bundle)
        }, false
    )

    return RailNavigationController(impl)
}

internal fun install(
    navigator: FragmentNavigator,
    activity: FragmentActivity,
    savedStateInstance: Bundle? = null,
    obj: FragmentNavigator.RailNavigation
): RailNavigationController {
    val impl = RailNavigationImpl(
        navigator = navigator,
        getNavView = activity.findViewById(obj.railNavigationViewId),
        fragments = obj.railNavigationFragments,
        onNavSelectionChange = obj::onRailNavigationSelectionChanged,
        navigation = obj
    )

    impl.onCreate(savedStateInstance)

    activity.application.registerActivityLifecycleCallbacks(
        ActivityNavigationLifecycle(activity, impl)
    )

    return RailNavigationController(impl)
}