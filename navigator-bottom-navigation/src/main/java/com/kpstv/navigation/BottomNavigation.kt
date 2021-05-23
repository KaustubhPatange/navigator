package com.kpstv.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kpstv.navigation.base.navigation.internals.*
import com.kpstv.navigation.internals.*

/**
 * Set up navigation for [BottomNavigationView] in [FragmentActivity] or [Fragment].
 *
 * This will automatically handle navigation & its state that can also
 * survive process death as well.
 *
 * Child fragments can implement [FragmentNavigator.Navigation.Callbacks] to get notified
 * when they are selected & re-selected again.
 */
fun FragmentNavigator.install(obj: FragmentNavigator.BottomNavigation): BottomNavigationController {
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
    obj: FragmentNavigator.BottomNavigation
): BottomNavigationController {
    val fragmentSavedState = savedStateInstance
        ?: if (fragment is ValueFragment) fragment.getBottomNavigationState() else null

    val view = fragment.requireView()

    val impl = BottomNavigationImpl(
        navigator = navigator,
        fragments = obj.bottomNavigationFragments,
        navView = view.findViewById(obj.bottomNavigationViewId),
        onNavSelectionChange = obj::onBottomNavigationSelectionChanged,
        navigation = obj
    )

    impl.onCreate(fragmentSavedState)

    fragment.parentFragmentManager.registerFragmentLifecycleCallbacks(
        FragmentNavigationLifecycle(fragment, impl) { f, bundle ->
            f.setBottomNavigationState(bundle)
        }, false
    )

    return BottomNavigationController(impl)
}

internal fun install(
    navigator: FragmentNavigator,
    activity: FragmentActivity,
    savedStateInstance: Bundle? = null,
    obj: FragmentNavigator.BottomNavigation
): BottomNavigationController {
    val impl = BottomNavigationImpl(
        navigator = navigator,
        navView = activity.findViewById(obj.bottomNavigationViewId),
        fragments = obj.bottomNavigationFragments,
        onNavSelectionChange = obj::onBottomNavigationSelectionChanged,
        navigation = obj
    )

    impl.onCreate(savedStateInstance)

    activity.application.registerActivityLifecycleCallbacks(
        ActivityNavigationLifecycle(activity, impl)
    )

    return BottomNavigationController(impl)
}