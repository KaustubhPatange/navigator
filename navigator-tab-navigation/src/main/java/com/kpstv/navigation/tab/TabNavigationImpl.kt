package com.kpstv.navigation.tab

import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.base.navigation.internals.CommonNavigationImpl
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1

internal class TabNavigationImpl(
    navigator: FragmentNavigator,
    private val getNavView: () -> TabLayout,
    private val onNavSelectionChange: KFunction1<Int, Unit>,
    fragments: Map<Int, KClass<out Fragment>>,
    navigation: FragmentNavigator.Navigation
) : CommonNavigationImpl(
    navigator = navigator, navFragments = fragments, navigation = navigation, stateKeys = SaveStateKeys("$KEY_SELECTION_INDEX${getNavView().id}")
) {
    override fun setUpNavigationViewCallbacks(selectionId: Int) /* "selectedId" is "position" */ {
        selectPosition(selectionId)
        getNavView().addOnTabSelectedListener(tabListener)
    }

    private val tabListener = object: TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            onSelectNavItem(tab.position)
        }
        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    }

    override fun onNavigationSelectionChange(id: Int) /* "selectedId" is "position" */ {
        onNavSelectionChange.invoke(id)
    }

    internal fun selectPosition(position: Int) {
        getNavView().getTabAt(position)?.select()
    }

    internal fun ignoreTabListeners(block: () -> Unit) {
        getNavView().removeOnTabSelectedListener(tabListener)
        block.invoke()
        getNavView().addOnTabSelectedListener(tabListener)
    }

    companion object {
        private const val KEY_SELECTION_INDEX = "com.kpstv.navigation:tab:key_index"
    }
}