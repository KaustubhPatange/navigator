@file:Suppress("unused")

package com.kpstv.navigation

import androidx.fragment.app.Fragment

/**
 * Checks if the fragment has any arguments passed during [FragmentNavigator.navigateTo] call.
 *
 * @see BaseArgs
 */
inline fun<reified T : BaseArgs> Fragment.hasKeyArgs(): Boolean {
    return arguments?.containsKey(ValueFragment.createArgKey(T::class.qualifiedName!!)) ?: false
}

/**
 * Parse the typed arguments from the bundle. It is best practice to check [hasKeyArgs]
 * & then proceed with this call.
 *
 * @throws NullPointerException When it does not exist.
 * @see BaseArgs
 */
inline fun <reified T : BaseArgs> Fragment.getKeyArgs(): T {
    return arguments?.getParcelable<T>(ValueFragment.createArgKey(T::class.qualifiedName!!)) as T
}

/**
 * Remove the typed arguments from the bundle.
 *
 * @see BaseArgs
 */
inline fun <reified T: BaseArgs> Fragment.clearArgs() {
    arguments?.remove(ValueFragment.createArgKey(T::class.qualifiedName!!))
}