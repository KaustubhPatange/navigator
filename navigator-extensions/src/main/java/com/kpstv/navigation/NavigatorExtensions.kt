package com.kpstv.navigation

import android.os.Build
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.Fragment

/**
 * Determines if it's safe to `finish()` by ensuring the backstack to be completely
 * empty.
 */
fun FragmentNavigator.canFinish() : Boolean {
    if (canGoBack()) {
        goBack()
        return false
    }
    return true
}

/**
 * A hot fix to overcome Z-index issue of the views in the fragment container.
 */
fun FragmentNavigator.autoChildElevation() {
    getFragmentManager().addOnBackStackChangedListener {
        if (Build.VERSION.SDK_INT >= 21) {
            getContainerView().children.forEachIndexed { index, view ->
                view.translationZ = (index + 1).toFloat()
            }
        } else {
            val container = getContainerView()
            container.doOnPreDraw {
                container.bringChildToFront(getFragmentManager().fragments.last().view)
            }
        }
    }
}