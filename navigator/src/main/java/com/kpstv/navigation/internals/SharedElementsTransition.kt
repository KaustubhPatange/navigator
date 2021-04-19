package com.kpstv.navigation.internals

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.transition.TransitionInflater
import com.kpstv.navigation.*
import com.kpstv.navigation.FragClazz

internal fun FragmentTransaction.prepareForSharedTransition(fm: FragmentManager, clazz: FragClazz, payload: AnimationDefinition.Shared) {
    payload.elements.keys.forEachIndexed { index, view ->
        addSharedElement(view, payload.elements.values.elementAt(index))
    }

    fm.registerFragmentLifecycleCallbacks(
        SharedElementCallback(
            clazz = clazz,
        ), false)
}

internal class SharedElementCallback(
    private val clazz: FragClazz
) : FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        if (f::class == clazz) {
            f.sharedElementEnterTransition = TransitionInflater.from(f.requireContext())
                .inflateTransition(R.transition.navigator_change_transform)
            fm.unregisterFragmentLifecycleCallbacks(this)
        }
    }
}