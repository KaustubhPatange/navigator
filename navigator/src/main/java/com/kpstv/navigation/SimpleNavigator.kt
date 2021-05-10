package com.kpstv.navigation

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kpstv.navigation.internals.BackStackRecord
import com.kpstv.navigation.internals.HistoryImpl
import com.kpstv.navigation.internals.newFragment

internal typealias DialogDismissListener = (DialogFragment) -> Unit

/**
 * A navigator available for [ValueFragment] limiting the original functionality.
 */
class SimpleNavigator internal constructor(private val context: Context, private val fm: FragmentManager, private val history: HistoryImpl) {
    constructor(context: Context, fm: FragmentManager) : this(context, fm, HistoryImpl(fm))

    private val dismissListeners = HashMap<DialogFragment, DialogDismissListener>()
    /**
     * Navigate to a [DialogFragment].
     *
     * @param clazz DialogFragment class to which it should navigate.
     * @param args Optional args to be passed.
     * @param onDismissListener Called when dialog is dismissed or canceled.
     */
    fun show(clazz: DialogFragClazz, args: BaseArgs? = null, onDismissListener: DialogDismissListener? = null) {
        val dialog = fm.newFragment(context, clazz) as DialogFragment
        val tagName = Navigator.getFragmentTagName(clazz)
        dialog.arguments = Navigator.createArguments(args)
        dialog.show(fm, tagName)

        if (onDismissListener != null) {
            dismissListeners[dialog] = onDismissListener
        }
        history.add(BackStackRecord(tagName, clazz))
    }

    /**
     * Dismiss the current [DialogFragment] if exist.
     */
    fun pop(): Boolean = history.pop()

    internal fun isLastFragment(fragment: DialogFragment): Boolean = history.isLastFragment(fragment)

    // save history state
    internal fun saveState(bundle: Bundle) = history.onSaveState(bundle)

    // restore history state
    internal fun restoreState(bundle: Bundle?) = history.onRestoreState(bundle)

    init {
        fm.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (f is DialogFragment && history.isLastFragment(f)) {
                    history.pop()
                }
                if (f is DialogFragment && dismissListeners.count() > 0) {
                    val listener = dismissListeners[f]
                    if (listener != null) {
                        listener.invoke(f)
                        dismissListeners.remove(f)
                    }
                }
                super.onFragmentDestroyed(fm, f)
            }
        }, false)
    }
}