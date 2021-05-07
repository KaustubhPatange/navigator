package com.kpstv.navigation.internals

import androidx.fragment.app.FragmentManager
import com.kpstv.navigation.FragClazz
import com.kpstv.navigation.History

internal class HistoryImpl internal constructor(private val fm: FragmentManager) : History {
    private val backStack = ArrayDeque<BackStackRecord>()

    internal fun add(record: BackStackRecord) {
        backStack.removeAll { r -> r == record }
        backStack.add(record)
    }

    override fun pop(): Boolean {
        if (!backStack.isEmpty()) {
            backStack.removeLast()
            fm.popBackStackImmediate()
            return true
        }
        return false
    }

    override fun clearUpTo(clazz: FragClazz, inclusive: Boolean): Boolean {
        if (!backStack.isEmpty()) {
            // find last because same instance of fragments might be present with different backstack name.
            val record = backStack.findLast { it.qualifiedName == clazz.qualifiedName } ?: return false
            val index = backStack.indexOf(record) + if (!inclusive) 0 else +1
            if (index > backStack.count()) return false
            for(i in backStack.count() - 1 downTo index) backStack.removeLast()

            popInternal(record.name, inclusive)
            return true
        }
        return false
    }

    override fun clearUpTo(name: String, inclusive: Boolean): Boolean {
        if (!backStack.isEmpty()) {
            val index = backStack.indexOfFirst { it.name == name }
            if (index != -1) {
                for(i in backStack.count() -1 downTo index) backStack.removeLast()
                popInternal(name, inclusive)
                return true
            }
        }
        return false
    }

    override fun clearAll(): Boolean {
        if (!backStack.isEmpty()) {
            backStack.clear()
            val to = fm.getBackStackEntryAt(0).name
            popInternal(to!!, true)
            return true
        }
        return false
    }

    private fun popInternal(name: String, inclusive: Boolean) {
        val include = if (inclusive) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0
        fm.popBackStack(name, include)
    }
}