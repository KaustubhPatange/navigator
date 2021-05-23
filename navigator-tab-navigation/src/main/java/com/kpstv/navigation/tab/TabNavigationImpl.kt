package com.kpstv.navigation.tab

import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.base.navigation.internals.CommonNavigationImpl
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1

internal class TabNavigationImpl(
    navigator: FragmentNavigator,
    internal val navView: TabLayout,
    private val onNavSelectionChange: KFunction1<Int, Unit>,
    fragments: Map<Int, KClass<out Fragment>>,
    navigation: FragmentNavigator.Navigation
) : CommonNavigationImpl(
    navigator = navigator, navFragments = fragments, navigation = navigation
) {
    override fun setUpNavigationViewCallbacks(selectionId: Int) /* "selectedId" is "position" */ {
        selectPosition(selectionId)
        navView.addOnTabSelectedListener(tabListener)
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
        navView.getTabAt(position)?.select()
    }

    internal fun ignoreTabListeners(block: () -> Unit) {
        navView.removeOnTabSelectedListener(tabListener)
        block.invoke()
        navView.addOnTabSelectedListener(tabListener)
    }
}