@file:Suppress("invisible_reference", "invisible_member")

package com.kpstv.navigation.tab

import android.os.Bundle
import com.kpstv.navigation.ValueFragment

internal fun ValueFragment.getTabNavigationState(): Bundle? {
    return tabNavigationState
}

internal fun ValueFragment.setTabNavigationState(value: Bundle?) {
    tabNavigationState = value
}