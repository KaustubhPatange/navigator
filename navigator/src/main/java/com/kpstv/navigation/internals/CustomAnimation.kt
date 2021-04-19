package com.kpstv.navigation.internals

import android.os.Bundle
import android.transition.TransitionInflater
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.FragClazz
import com.kpstv.navigation.ValueFragment

internal class CustomAnimation(private val manager: FragmentManager, private val containerView: FrameLayout) {
    fun set(ft: FragmentTransaction, custom: AnimationDefinition.Custom, to: FragClazz) {
        val currentFragment = manager.findFragmentById(containerView.id) ?: return /* no-op */
        val type = containerView.context.resources.getResourceTypeName(custom.enter)
        if (type == "anim" || type == "animator") {
            // If enter & exit transition are set on Fragment then we must reset it to avoid
            // weird animation bug.
            currentFragment.clearTransitions()
            if (custom.popExit != 0 && custom.popEnter != 0 && custom.enter != 0 && custom.exit != 0)
                ft.setCustomAnimations(custom.enter, custom.exit, custom.popEnter, custom.popExit)
            else if (custom.enter != 0 && custom.exit != 0)
                ft.setCustomAnimations(custom.enter, custom.exit)
        } else if (type == "transition") {
            val tl = TransitionInflater.from(containerView.context)
            manager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                // Return & Re-Enter transition (if not specified) are same as Enter & Exit. We must specify
                // them explicitly to gain precedence over [FragmentTransaction.setCustomAnimations].
                override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
                    super.onFragmentPaused(fm, f)
                    if (f::class == currentFragment::class) {
                        if (custom.exit != 0) f.exitTransition = tl.inflateTransition(custom.exit)
                        if (custom.popEnter != 0)
                            f.reenterTransition = tl.inflateTransition(custom.popEnter)
                        else
                            f.reenterTransition = tl.inflateTransition(custom.exit)
                    }
                }
                override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                    super.onFragmentCreated(fm, f, savedInstanceState)
                    if (f::class == to) {
                        if (custom.enter != 0) f.enterTransition = tl.inflateTransition(custom.enter)
                        if (custom.popExit != 0)
                            f.returnTransition = tl.inflateTransition(custom.popExit)
                        else
                            f.returnTransition = tl.inflateTransition(custom.enter)
                        fm.unregisterFragmentLifecycleCallbacks(this)
                    }
                }
            }, false)
        }
    }
}

