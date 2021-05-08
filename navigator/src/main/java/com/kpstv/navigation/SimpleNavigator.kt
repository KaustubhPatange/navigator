package com.kpstv.navigation

import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kpstv.navigation.internals.BackStackRecord
import com.kpstv.navigation.internals.newFragment

/**
 * A navigator available for [ValueFragment] limiting the original functionality.
 */
class SimpleNavigator internal constructor(private val context: Context, private val fm: FragmentManager) {
    private val backStack = ArrayDeque<BackStackRecord>()

    /**
     * Navigate to a [DialogFragment].
     *
     * @param clazz DialogFragment class to which it should navigate.
     * @param args Optional args to be passed.
     */
    fun show(clazz: DialogFragClazz, args: BaseArgs? = null) {
        val dialog = fm.newFragment(context, clazz) as DialogFragment
        val tagName = Navigator.getFragmentTagName(clazz)
        dialog.arguments = Navigator.createArguments(args)
        dialog.show(fm, tagName)

        backStack.add(BackStackRecord(tagName, clazz))
    }

    /**
     * Dismiss the current [DialogFragment] if exist.
     */
    fun pop(): Boolean {
        if (!backStack.isEmpty()) {
            val last = backStack.last().name
            val fragment = fm.findFragmentByTag(last)
            if (fragment is DialogFragment) {
                fragment.dismiss()
                return true
            }
        }
        return false
    }

    internal fun hasFragment(fragment: DialogFragment): Boolean {
        if (!backStack.isEmpty()) {
            return backStack.last().qualifiedName == fragment::class.qualifiedName
        }
        return false
    }

    init {
        fm.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (f is DialogFragment && backStack.any { it.qualifiedName == f::class.qualifiedName }) {
                    backStack.removeLastOrNull()
                }
                super.onFragmentDestroyed(fm, f)
            }
        }, false)
    }
}