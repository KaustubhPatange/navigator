package com.kpstv.navigation.tab

import android.os.Bundle
import com.kpstv.navigation.ValueFragment

internal fun ValueFragment.getTabNavigationState(): Bundle? {
    val field = ValueFragment::class.java.getDeclaredField("tabNavigationState").apply {
        isAccessible = true
    }
    return field.get(this) as? Bundle
}

internal fun ValueFragment.setTabNavigationState(value: Bundle?) {
    ValueFragment::class.java.getDeclaredField("tabNavigationState").apply {
        isAccessible = true
        set(this@setTabNavigationState, value)
    }
}