package com.kpstv.navigation.tab

import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.tabs.TabLayout
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.base.navigation.internals.CommonNavigationImpl
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1

internal class TabNavigationImpl(
    fm: FragmentManager,
    containerView: FrameLayout,
    internal val navView: TabLayout,
    private val onNavSelectionChange: KFunction1<Int, Unit>,
    private val fragments: Map<Int, KClass<out Fragment>>,
    navigation: Navigator.Navigation
) : CommonNavigationImpl(
    fm = fm, containerView = containerView, navFragments = fragments, navigation = navigation
) {
    override fun setUpNavigationViewCallbacks(position: Int) /* "selectedId" is "position" */ {
        selectPosition(position)
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

    override fun onNavigationSelectionChange(position: Int) /* "selectedId" is "position" */ {
        onNavSelectionChange.invoke(position)
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