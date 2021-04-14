package com.kpstv.navigator_backpress_sample

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
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