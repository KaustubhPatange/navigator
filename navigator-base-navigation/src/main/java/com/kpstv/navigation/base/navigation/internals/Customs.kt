package com.kpstv.navigation.base.navigation.internals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.NavigatorTransmitter

// Really missing the package-private feature in Kotlin :(

fun Navigator.getSaveInstanceState(): Bundle? {
    val field = Navigator::class.java.getDeclaredField("savedInstanceState").apply {
        isAccessible = true
    }
    return field.get(this) as? Bundle
}

fun Navigator.getOwner(): Any? {
    val field = Navigator::class.java.getDeclaredField("owner").apply {
        isAccessible = true
    }
    return field.get(this)
}

// TODO: Remove if not needed
fun Fragment.getNavigator(): Navigator {
    if (this !is NavigatorTransmitter)
        throw IllegalAccessException("The fragment must implement \"NavigatorTransmitter\" interface before setting up navigation.")
    return this.getNavigator()
}

// TODO: Remove if not needed
fun ComponentActivity.getNavigator(): Navigator {
    if (this !is NavigatorTransmitter)
        throw IllegalAccessException("The activity must implement \"NavigatorTransmitter\" interface before setting up navigation.")
    return this.getNavigator()
}