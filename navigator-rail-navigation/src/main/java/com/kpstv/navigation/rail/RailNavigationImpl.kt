package com.kpstv.navigation.rail

import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigationrail.NavigationRailView
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.base.navigation.internals.CommonNavigationImpl
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1

internal class RailNavigationImpl(
    navigator: FragmentNavigator,
    private val getNavView: () -> NavigationRailView,
    private val onNavSelectionChange: KFunction1<Int, Unit>,
    fragments: Map<Int, KClass<out Fragment>>,
    navigation: FragmentNavigator.Navigation
) : CommonNavigationImpl(
    navigator = navigator, navFragments = fragments, navigation = navigation, stateKeys = SaveStateKeys("$KEY_SELECTION_INDEX${getNavView().id}")
) {
    override fun setUpNavigationViewCallbacks(selectionId: Int) {
        getNavView().selectedItemId = selectionId
        getNavView().setOnItemSelectedListener(navigationListener)
    }

    private val navigationListener = NavigationBarView.OnItemSelectedListener call@{ item ->
        return@call onSelectNavItem(item.itemId)
    }

    override fun onNavigationSelectionChange(id: Int) {
        onNavSelectionChange.invoke(id)
    }

    internal fun selectPosition(id: Int) {
        getNavView().selectedItemId = id
    }

    internal fun ignoreNavigationListeners(block: () -> Unit) {
        getNavView().setOnItemSelectedListener(null)
        block.invoke()
        getNavView().setOnItemSelectedListener(navigationListener)
    }

    companion object {
        private const val KEY_SELECTION_INDEX = "com.kpstv.navigation:rail:key_index"
    }
}