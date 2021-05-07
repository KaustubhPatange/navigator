package com.kpstv.navigation

interface History {
    /**
     * Clear the backstack history up to the fragment.
     */
    fun clearUpTo(clazz: FragClazz, inclusive: Boolean = true): Boolean

    /**
     * Clear the backstack history up to the backstack name.
     */
    fun clearUpTo(name: String, inclusive: Boolean = true): Boolean

    /**
     * Clears all the backstack history.
     */
    fun clearAll(): Boolean

    /**
     * Pops the last fragment
     */
    fun pop(): Boolean
}