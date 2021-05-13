package com.kpstv.navigation

import androidx.fragment.app.DialogFragment

/**
 * @see ValueFragment.hasKeyArgs
 */
inline fun<reified T : BaseArgs> DialogFragment.hasKeyArgs(): Boolean {
    return arguments?.containsKey(ValueFragment.createArgKey(T::class.qualifiedName!!)) ?: false
}

/**
 * @see ValueFragment.getKeyArgs
 */
inline fun<reified T : BaseArgs> DialogFragment.getKeyArgs(): T {
    return arguments?.getParcelable<T>(ValueFragment.createArgKey(T::class.qualifiedName!!)) as T
}