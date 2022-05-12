package com.kpstv.navigation

import androidx.annotation.IdRes
import com.kpstv.navigation.internals.BottomNavigationImpl

class BottomNavigationController internal constructor(
   private val bn: BottomNavigationImpl
) {
   /**
    * Selects the current bottom navigation tab.
    *
    * @param id Id of the fragment
    * @param args Optional args to be passed. The destination fragment must be a subclass of [ValueFragment].
    */
   fun select(@IdRes id: Int, args: BaseArgs? = null) {
      bn.ignoreNavigationListeners {
         bn.onSelectNavItem(id, args)
         bn.selectPosition(id)
      }
   }
}