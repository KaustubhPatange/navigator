@file:Suppress("invisible_reference", "invisible_member")

package com.kpstv.navigation.rail

import android.os.Bundle
import com.kpstv.navigation.ValueFragment

internal fun ValueFragment.getRailNavigationState(): Bundle? {
    return bottomNavigationState
}

internal fun ValueFragment.setRailNavigationState(value: Bundle?) {
    bottomNavigationState = value
}