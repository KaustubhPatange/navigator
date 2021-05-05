package com.kpstv.navigation.base.navigation.internals

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.kpstv.navigation.ValueFragment

fun Fragment.getSaveInstanceState(): Bundle? {
    val field = Fragment::class.java.getDeclaredField("mSavedFragmentState").apply {
        isAccessible = true
    }
    return field.get(this) as? Bundle
}