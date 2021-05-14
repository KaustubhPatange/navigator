@file:Suppress("invisible_reference", "invisible_member")

package com.kpstv.navigation.base.navigation.internals

import android.os.Bundle
import com.kpstv.navigation.Navigator

fun Navigator.getSaveInstanceState(): Bundle? {
    return savedInstanceState
}

fun Navigator.getOwner(): Any {
    return this.owner
}