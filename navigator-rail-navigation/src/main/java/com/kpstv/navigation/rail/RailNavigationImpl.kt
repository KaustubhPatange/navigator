package com.kpstv.navigation.rail

import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigationrail.NavigationRailView
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.base.navigation.internals.CommonNavigationImpl
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1

class RailNavigationImpl(
    navigator: FragmentNavigator,
    internal val navView: NavigationRailView,
    private val onNavSelectionChange: KFunction1<Int, Unit>,
    fragments: Map<Int, KClass<out Fragment>>,
    navigation: FragmentNavigator.Navigation
) : CommonNavigationImpl(
    navigator = navigator, navFragments = fragments, navigation = navigation, stateKeys = SaveStateKeys("$KEY_SELECTION_INDEX${navView.id}")
) {
    override fun setUpNavigationViewCallbacks(selectionId: Int) {
        navView.selectedItemId = selectionId
        navView.setOnItemSelectedListener(navigationListener)
    }

    private val navigationListener = NavigationBarView.OnItemSelectedListener call@{ item ->
        return@call onSelectNavItem(item.itemId)
    }

    override fun onNavigationSelectionChange(id: Int) {
        onNavSelectionChange.invoke(id)
    }

    internal fun ignoreNavigationListeners(block: () -> Unit) {
        navView.setOnItemSelectedListener(null)
        block.invoke()
        navView.setOnItemSelectedListener(navigationListener)
    }

    companion object {
        private const val KEY_SELECTION_INDEX = "com.kpstv.navigation:rail:key_index"
    }
}