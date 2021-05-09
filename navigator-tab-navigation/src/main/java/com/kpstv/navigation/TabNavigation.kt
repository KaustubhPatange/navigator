package com.kpstv.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kpstv.navigation.base.navigation.internals.ActivityNavigationLifecycle
import com.kpstv.navigation.base.navigation.internals.FragmentNavigationLifecycle
import com.kpstv.navigation.base.navigation.internals.getOwner
import com.kpstv.navigation.base.navigation.internals.getSaveInstanceState
import com.kpstv.navigation.tab.TabNavigationImpl
import com.kpstv.navigation.tab.getTabNavigationState
import com.kpstv.navigation.tab.setTabNavigationState
import kotlin.reflect.KClass

/**
 * Set up navigation for [BottomNavigationView] in [Fragment].
 *
 * This will automatically handle navigation & its state that can also
 * survive process death as well.
 *
 * Child fragments can implement [Navigator.Navigation.Callbacks] to get notified
 * when they are selected & re-selected again.
 */
fun Navigator.install(obj: Navigator.TabNavigation): TabNavigationController {
    val owner = getOwner()
    val savedStateInstance = getSaveInstanceState()
    return when(owner) {
        is FragmentActivity -> install(this, owner, savedStateInstance, obj)
        is Fragment -> install(this, owner, savedStateInstance, obj)
        else -> throw IllegalStateException("Couldn't determine the owner type, this is the problem of something stupid I did. Kindly report to me!")
    }
}


internal fun install(
    navigator: Navigator,
    fragment: Fragment,
    savedStateInstance: Bundle?,
    obj: Navigator.TabNavigation
): TabNavigationController {
    val fragmentSavedState = savedStateInstance
        ?: if (fragment is ValueFragment) fragment.getTabNavigationState() else null

    val view = fragment.requireView()

    val impl = TabNavigationImpl(
        navigator = navigator,
        fragments = listToMapOfFragments(obj.tabNavigationFragments),
        navView = view.findViewById(obj.tabLayoutId),
        onNavSelectionChange = obj::onTabNavigationSelectionChanged,
        navigation = obj
    )

    impl.onCreate(fragmentSavedState)

    fragment.parentFragmentManager.registerFragmentLifecycleCallbacks(
        FragmentNavigationLifecycle(fragment, impl) { f, bundle ->
            f.setTabNavigationState(bundle)
        }, false
    )

    return TabNavigationController(impl)
}

internal fun install(
    navigator: Navigator,
    activity: FragmentActivity,
    savedStateInstance: Bundle? = null,
    obj: Navigator.TabNavigation
): TabNavigationController {
    val impl = TabNavigationImpl(
        navigator = navigator,
        fragments = listToMapOfFragments(obj.tabNavigationFragments),
        navView = activity.findViewById(obj.tabLayoutId),
        onNavSelectionChange = obj::onTabNavigationSelectionChanged,
        navigation = obj
    )
    impl.onCreate(savedStateInstance)

    activity.application.registerActivityLifecycleCallbacks(
        ActivityNavigationLifecycle(activity, impl)
    )

    return TabNavigationController(impl)
}

private fun listToMapOfFragments(list: List<KClass<out Fragment>>) : Map<Int, KClass<out Fragment>> {
    return list.associateBy { list.indexOf(it) }
}