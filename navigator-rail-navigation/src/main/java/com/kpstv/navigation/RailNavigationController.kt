package com.kpstv.navigation

import androidx.annotation.IdRes
import com.kpstv.navigation.rail.RailNavigationImpl

class RailNavigationController internal constructor(
    private val rn: RailNavigationImpl
) {
    /**
     * Selects the current bottom navigation tab.
     *
     * @param id Id of the fragment
     * @param args Optional args to be passed. The destination fragment must be a subclass of [ValueFragment].
     */
    fun select(@IdRes id: Int, args: BaseArgs? = null) {
        rn.ignoreNavigationListeners {
            rn.onSelectNavItem(id, args)
            rn.selectPosition(id)
        }
    }
}