package com.kpstv.navigation.internals

import android.view.View
import android.view.ViewTreeObserver

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