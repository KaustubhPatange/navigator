package com.kpstv.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * An abstraction layer over [FragmentManager.mBackStack] to efficiently manage backstack.
 */
interface History {
    /**
     * Clear the backstack history up to the fragment.
     *
     * @param clazz [Fragment]'s class up to where history should be clear.
     * @param inclusive Include this fragment as well.
     * @param all There may be multiple instance of this fragment on the backstack this will remove all the instance of them if set to true, otherwise the last added one from the backstack will be removed.
     */
    fun clearUpTo(clazz: FragClazz, inclusive: Boolean = true, all: Boolean = false): Boolean

    /**
     * Clear the backstack history up to the backstack name.
     *
     * @param name Name of the entry in the backstack.
     * @param inclusive Include this fragment as well.
     */
    fun clearUpTo(name: String, inclusive: Boolean = true): Boolean

    /**
     * Clears all the backstack history.
     */
    fun clearAll(): Boolean

    /**
     * Removes the last fragment.
     */
    fun pop(): Boolean

    /**
     * Returns if backstack record is empty.
     */
    fun isEmpty(): Boolean

    /**
     * Returns the count of total items in backstack.
     */
    fun count(): Int

    /**
     * Returns the backstack name of the [fragClazz] if it's present in the history.
     *
     * If there are multiple instance of the [fragClazz] then it will return the last added one.
     */
    fun getBackStackName(fragClazz: FragClazz): String?

    /**
     * Returns the backstack name of the [fragClazz] if it's present in the history.
     *
     * If there are multiple instance of the [fragClazz] then it will return the first added one.
     */
    fun getTopBackStackName(fragClazz: FragClazz): String?

    /**
     * Returns a list of backstack name of all the instance of [fragClazz] present in the history.
     *
     * If there are no instance of this fragment then it returns an empty list.
     */
    fun getAllBackStackName(fragClazz: FragClazz): List<String>
}

/**
 * Some set of options to manage backstack history in a fragment transaction through [FragmentNavigator.navigateTo] call.
 *
 * @see <a href="https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials#navigation-with-single-top-instance-or-popupto">Navigation with single top instance or popUpTo</a>
 */
sealed class HistoryOptions {
    object None : HistoryOptions()

    /**
     * Clear all the previous remembered fragment transactions. Equivalent to clearing all backstack records.
     */
    object ClearHistory: HistoryOptions()

    /**
     * Maintains only one instance of this [Fragment] in the current [FragmentManager].
     *
     * If that instance exist it will be replaced with this new one after clearing the history till there.
     */
    object SingleTopInstance: HistoryOptions()

    /**
     * Clear backstack history up to the [clazz]. This also removes all the necessary fragment involved in that fragment transaction.
     *
     * - Example: Consider a history in order `[first, second, second, third, forth]` where we are navigating
     * to `fifth`.
     * - A `PopToFragment(second)` call will make history `[first, fifth]`.
     * - A `PopToFragment(second, all = false)` call will make history `[first, second, fifth]`.
     *
     * @param clazz [Fragment]'s class that'll be used to identify the entry in backstack.
     * @param all There may be multiple instance of this [Fragment], this specifies if we should remove them all?
     */
    data class PopToFragment(val clazz: FragClazz, val all: Boolean = true) : HistoryOptions()

    /**
     * Clear history up to the [name] of the entry in backstack. This also removes all the necessary fragment involved in that fragment transaction.
     *
     * - Example: Consider a history in order `[first, second, third, forth]`. So a `PopToFragment(second)` call
     * will make history `[first, fifth]` where `fifth` is the fragment to which we are navigating.
     *
     * @param name The name associated with the entry in backstack.
     * @param inclusive Should that fragment be removed as well. Equivalent to [FragmentManager.POP_BACK_STACK_INCLUSIVE].
     *
     * @see History.getBackStackName
     * @see History.getTopBackStackName
     * @see History.getAllBackStackName
     */
    data class PopToBackStack(val name: String, val inclusive: Boolean = true) : HistoryOptions()
}