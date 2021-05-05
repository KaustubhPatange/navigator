package com.kpstv.navigation

import androidx.annotation.IdRes
import com.kpstv.navigation.tab.TabNavigationImpl

class TabNavigationController internal constructor(
    private val tn: TabNavigationImpl
) {
    /**
     * Selects the current bottom navigation tab.
     *
     * @param position Position of the fragment in the list.
     * @param args Optional args to be passed. The destination fragment must be a subclass of [ValueFragment].
     */
    fun select(@IdRes position: Int, args: BaseArgs? = null) {
        tn.ignoreTabListeners {
            tn.onSelectNavItem(position, args)
            tn.selectPosition(position)
        }
    }
}