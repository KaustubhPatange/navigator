package com.kpstv.navigation

import androidx.fragment.app.DialogFragment

/**
 * @see ValueFragment.hasKeyArgs
 */
fun DialogFragment.hasKeyArgs(): Boolean {
    return arguments?.containsKey(ValueFragment.ARGUMENTS) ?: false
}

/**
 * @see ValueFragment.getKeyArgs
 */
fun<T : BaseArgs> DialogFragment.getKeyArgs(): T {
    return arguments?.getParcelable<T>(ValueFragment.ARGUMENTS) as T
}