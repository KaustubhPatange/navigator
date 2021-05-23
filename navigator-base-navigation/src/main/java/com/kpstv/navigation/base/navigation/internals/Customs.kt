@file:Suppress("invisible_reference", "invisible_member")

package com.kpstv.navigation.base.navigation.internals

import android.os.Bundle
import com.kpstv.navigation.FragmentNavigator

fun FragmentNavigator.getSaveInstanceState(): Bundle? {
    return savedInstanceState
}

fun FragmentNavigator.getOwner(): Any {
    return this.owner
}