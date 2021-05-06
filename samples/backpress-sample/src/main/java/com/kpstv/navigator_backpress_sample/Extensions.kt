package com.kpstv.navigator_backpress_sample

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import androidx.viewbinding.ViewBinding

fun Fragment.colorFrom(@ColorRes id: Int): Int {
    return ContextCompat.getColor(requireContext(), id)
}

@Suppress("DEPRECATION")
fun Activity.makeFullScreen() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
}

fun View.applyBottomInsets() {
    setOnApplyWindowInsetsListener { _, insets ->
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            updateMargins(bottom = insets.systemWindowInsetBottom)
        }
        insets
    }
}

fun FragmentActivity.registerFragmentLifecycleForLogging(block: (Fragment, String) -> Unit) {
    supportFragmentManager.registerFragmentLifecycleCallbacks(
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
                super.onFragmentPreAttached(fm, f, context)
                block.invoke(f, "onPreAttached")
            }

            override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                super.onFragmentAttached(fm, f, context)
                block.invoke(f, "onAttached")
            }

            override fun onFragmentPreCreated(
                fm: FragmentManager,
                f: Fragment,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentPreCreated(fm, f, savedInstanceState)
                block.invoke(f, "onPreCreated")
            }

            override fun onFragmentCreated(
                fm: FragmentManager,
                f: Fragment,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentCreated(fm, f, savedInstanceState)
                block.invoke(f, "onCreated")
            }

            override fun onFragmentActivityCreated(
                fm: FragmentManager,
                f: Fragment,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentActivityCreated(fm, f, savedInstanceState)
                block.invoke(f, "onActivityCreated")
            }

            override fun onFragmentViewCreated(
                fm: FragmentManager,
                f: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState)
                block.invoke(f, "onViewCreated")
            }

            override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                super.onFragmentStarted(fm, f)
                block.invoke(f, "onStarted")
            }

            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                super.onFragmentResumed(fm, f)
                block.invoke(f, "onResumed")
            }

            override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
                super.onFragmentPaused(fm, f)
                block.invoke(f, "onPaused")
            }

            override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
                super.onFragmentStopped(fm, f)
                block.invoke(f, "onStopped")
            }

            override fun onFragmentSaveInstanceState(
                fm: FragmentManager,
                f: Fragment,
                outState: Bundle
            ) {
                super.onFragmentSaveInstanceState(fm, f, outState)
                block.invoke(f, "onSaveInstanceState")
            }

            override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentViewDestroyed(fm, f)
                block.invoke(f, "onViewDestroyed")
            }

            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentDestroyed(fm, f)
                block.invoke(f, "onDestroyed")
            }

            override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                super.onFragmentDetached(fm, f)
                block.invoke(f, "onDetached")
            }
        },
        true
    )
}