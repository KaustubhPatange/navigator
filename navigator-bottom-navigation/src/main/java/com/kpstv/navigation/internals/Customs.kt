@file:Suppress("invisible_reference", "invisible_member")

package com.kpstv.navigation.internals

import android.os.Bundle
import com.kpstv.navigation.ValueFragment

internal fun ValueFragment.getBottomNavigationState(): Bundle? {
    return bottomNavigationState
}

internal fun ValueFragment.setBottomNavigationState(value: Bundle?) {
    bottomNavigationState = value
}