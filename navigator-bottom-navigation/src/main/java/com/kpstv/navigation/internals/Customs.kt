package com.kpstv.navigation.internals

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.kpstv.navigation.ValueFragment

internal fun ValueFragment.getBottomNavigationState(): Bundle? {
    val field = ValueFragment::class.java.getDeclaredField("bottomNavigationState").apply {
        isAccessible = true
    }
    return field.get(this) as? Bundle
}

internal fun ValueFragment.setBottomNavigationState(value: Bundle?) {
    ValueFragment::class.java.getDeclaredField("bottomNavigationState").apply {
        isAccessible = true
        set(this@setBottomNavigationState, value)
    }
}