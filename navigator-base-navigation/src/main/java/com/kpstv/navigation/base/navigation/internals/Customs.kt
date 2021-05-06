package com.kpstv.navigation.base.navigation.internals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.NavigatorTransmitter
import com.kpstv.navigation.ValueFragment

fun Fragment.getSaveInstanceState(): Bundle? {
    val field = Fragment::class.java.getDeclaredField("mSavedFragmentState").apply {
        isAccessible = true
    }
    return field.get(this) as? Bundle
}

fun Fragment.getNavigator(): Navigator {
    if (this !is NavigatorTransmitter)
        throw IllegalAccessException("The fragment must implement \"NavigatorTransmitter\" interface before setting up navigation.")
    return this.getNavigator()
}

fun ComponentActivity.getNavigator(): Navigator {
    if (this !is NavigatorTransmitter)
        throw IllegalAccessException("The activity must implement \"NavigatorTransmitter\" interface before setting up navigation.")
    return this.getNavigator()
}