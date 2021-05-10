package com.kpstv.navigation.internals

import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.transition.TransitionInflater
import com.kpstv.navigation.*
import com.kpstv.navigation.FragClazz

internal fun FragmentTransaction.prepareForSharedTransition(fm: FragmentManager, containerView: FrameLayout, clazz: FragClazz, payload: AnimationDefinition.Shared) {
    payload.elements.keys.forEachIndexed { index, view ->
        addSharedElement(view, payload.elements.values.elementAt(index))
    }

    val currentFragment = fm.findFragmentById(containerView.id)
    currentFragment?.let {
        if (payload.currentExiting != -1) currentFragment.exitTransition =containerView.context.inflateTransition(payload.currentExiting)
    }

    fm.registerFragmentLifecycleCallbacks(
        SharedElementCallback(
            payload = payload,
            clazz = clazz,
        ), false)
}

internal class SharedElementCallback(
    private val payload: AnimationDefinition.Shared,
    private val clazz: FragClazz
) : FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        if (f::class == clazz) {
            if (payload.destinationEntering != -1) f.enterTransition = f.requireContext().inflateTransition(payload.destinationEntering)
            f.sharedElementEnterTransition = f.requireContext().inflateTransition(R.transition.navigator_change_transform)
            fm.unregisterFragmentLifecycleCallbacks(this)
        }
    }
}