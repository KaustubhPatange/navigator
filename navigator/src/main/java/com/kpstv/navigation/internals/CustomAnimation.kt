package com.kpstv.navigation.internals

import android.os.Bundle
import android.transition.TransitionInflater
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.FragClazz
import com.kpstv.navigation.FragmentNavigator

internal class CustomAnimation(private val manager: FragmentManager, private val containerView: FrameLayout) {
    fun set(ft: FragmentTransaction, custom: AnimationDefinition.Custom, to: FragClazz) {
        val currentFragment = FragmentNavigator.getCurrentVisibleFragment(manager, containerView) ?: return /* no-op */
        val type = containerView.context.resources.getResourceTypeName(custom.destinationEntering)
        if (type == "anim" || type == "animator") {
            // If enter & exit transition are set on Fragment then we must reset it to avoid
            // weird animation bug.
            currentFragment.clearTransitions()
            if (custom.destinationExiting != 0 && custom.currentReturning != 0 && custom.destinationEntering != 0 && custom.currentExiting != 0)
                ft.setCustomAnimations(custom.destinationEntering, custom.currentExiting, custom.currentReturning, custom.destinationExiting)
            else if (custom.destinationEntering != 0 && custom.currentExiting != 0)
                ft.setCustomAnimations(custom.destinationEntering, custom.currentExiting)
        } else if (type == "transition") {
            val tl = TransitionInflater.from(containerView.context)
            manager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                // Return & Re-Enter transition (if not specified) are same as Enter & Exit. We must specify
                // them explicitly to gain precedence over [FragmentTransaction.setCustomAnimations].
                override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
                    super.onFragmentPaused(fm, f)
                    if (f::class == currentFragment::class) {
                        if (custom.currentExiting != 0) f.exitTransition = tl.inflateTransition(custom.currentExiting)
                        if (custom.currentReturning != 0)
                            f.reenterTransition = tl.inflateTransition(custom.currentReturning)
                        else
                            f.reenterTransition = tl.inflateTransition(custom.currentExiting)
                    }
                }
                override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                    super.onFragmentCreated(fm, f, savedInstanceState)
                    if (f::class == to) {
                        if (custom.destinationEntering != 0) f.enterTransition = tl.inflateTransition(custom.destinationEntering)
                        if (custom.destinationExiting != 0)
                            f.returnTransition = tl.inflateTransition(custom.destinationExiting)
                        else
                            f.returnTransition = tl.inflateTransition(custom.destinationEntering)
                        fm.unregisterFragmentLifecycleCallbacks(this)
                    }
                }
            }, false)
        }
    }
}

