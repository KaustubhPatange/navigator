package com.kpstv.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kpstv.navigation.internals.*
import com.kpstv.navigation.internals.BottomNavigationImpl
import com.kpstv.navigation.internals.getSaveInstanceState
import com.kpstv.navigator.base.navigation.internals.ActivityBottomNavigationLifecycle
import com.kpstv.navigator.base.navigation.internals.FragmentBottomNavigationLifecycle

/**
 * Set up navigation for [BottomNavigationView] in [Fragment].
 *
 * This will automatically handle navigation & its state that can also
 * survive process death as well.
 *
 * Child fragments can implement [Navigator.Navigation.Callbacks] to get notified
 * when they are selected & re-selected again.
 */
fun Navigator.install(fragment: Fragment, obj: Navigator.BottomNavigation): BottomNavigationController {
    val fragmentSavedState = fragment.getSaveInstanceState() ?:
    if (fragment is ValueFragment) fragment.getBottomNavigationState() else null

    val view = fragment.requireView()

    val impl = BottomNavigationImpl(
        fm = getFragmentManager(),
        containerView = getContainerView(),
        fragments = obj.bottomNavigationFragments,
        navView = view.findViewById(obj.bottomNavigationViewId),
        onNavSelectionChange = obj::onBottomNavigationSelectionChanged,
        navigation = obj
    )

    impl.onCreate(fragmentSavedState)

    fragment.parentFragmentManager.registerFragmentLifecycleCallbacks(
        FragmentBottomNavigationLifecycle(fragment, impl), false
    )

    return BottomNavigationController(impl)
}

/**
 * Set up navigation for [BottomNavigationView] in [FragmentActivity].
 *
 * This will automatically handle navigation & its state that can also
 * survive process death as well.
 *
 * Child fragments can implement [Navigator.BottomNavigation.Callbacks] to get notified
 * when they are selected & re-selected again.
 */
fun Navigator.install(
    activity: FragmentActivity,
    savedStateInstance: Bundle? = null,
    obj: Navigator.BottomNavigation
): BottomNavigationController {
    val impl = BottomNavigationImpl(
        fm = getFragmentManager(),
        containerView = getContainerView(),
        navView = activity.findViewById(obj.bottomNavigationViewId),
        fragments = obj.bottomNavigationFragments,
        onNavSelectionChange = obj::onBottomNavigationSelectionChanged,
        navigation = obj
    )

    impl.onCreate(savedStateInstance)

    activity.application.registerActivityLifecycleCallbacks(
        ActivityBottomNavigationLifecycle(activity, impl)
    )

    return BottomNavigationController(impl)
}