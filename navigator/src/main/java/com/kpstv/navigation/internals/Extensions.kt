package com.kpstv.navigation.internals

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.TransitionRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.transition.TransitionInflater
import com.kpstv.navigation.FragClazz
import com.kpstv.navigation.ValueFragment

internal fun View.doOnLaidOut(block: (View) -> Unit) {
    if (isLaidOut) {
        block.invoke(this)
    } else {
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener{
            override fun onPreDraw(): Boolean {
                if (isLaidOut) {
                    block.invoke(this@doOnLaidOut)
                    viewTreeObserver.removeOnPreDrawListener(this)
                }
                return true
            }
        })
    }
}

internal fun View.invisible() {
    visibility = View.INVISIBLE
}

internal fun View.show() {
    visibility = View.VISIBLE
}

internal fun<T> Iterable<T>.secondLast(): T? {
    val c = count()
    if (c > 1) {
        return this.elementAt(c - 2)
    }
    return null
}

internal fun Fragment.clearTransitions() {
    enterTransition = null
    exitTransition = null
    returnTransition = null
    reenterTransition = null
}

internal fun Context.inflateTransition(@TransitionRes id: Int) = TransitionInflater.from(this).inflateTransition(id)

internal fun FragmentManager.newFragment(context: Context, clazz: FragClazz): Fragment {
    return fragmentFactory.instantiate(context.classLoader, clazz.java.canonicalName)
}

internal fun Any.toIdentifier(): String {
    return this::class.qualifiedName?.replace(".", "_") ?: toString()
}