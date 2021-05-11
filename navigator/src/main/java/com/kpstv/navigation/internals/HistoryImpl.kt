package com.kpstv.navigation.internals

import android.os.Bundle
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kpstv.navigation.FragClazz
import com.kpstv.navigation.History
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList

internal class HistoryImpl internal constructor(private val fm: FragmentManager) : History {
    private val backStack = ArrayDeque<BackStackRecord>()

    internal fun add(record: BackStackRecord) {
        // no same backstack name
        // TODO: This is already ensured by getUniqueBackStackName, see if you need this
        backStack.removeAll { r -> r == record }
        backStack.add(record)
    }

    override fun pop(): Boolean {
        if (!backStack.isEmpty()) {
            val last = backStack.last().name
            val fragment = fm.findFragmentByTag(last)
            if (fragment is DialogFragment) {
                // Remove if not already canceled or dismissed.
                if (!fragment.isStateSaved) fragment.dismiss()
            } else {
                fm.popBackStackImmediate()
            }
            backStack.removeLast()
            return true
        }
        return false
    }

    override fun clearUpTo(clazz: FragClazz, inclusive: Boolean, all: Boolean): Boolean {
        if (!backStack.isEmpty()) {
            val record = if (all) {
                backStack.find { it.qualifiedName == clazz.qualifiedName }
            } else {
                backStack.findLast { it.qualifiedName == clazz.qualifiedName }
            } ?: return false
            return clearUpTo(record.name, inclusive)
        }
        return false
    }

    override fun clearUpTo(name: String, inclusive: Boolean): Boolean {
        if (!backStack.isEmpty()) {
            val index = backStack.indexOfFirst { it.name == name }
            if (index != -1) {
                for(i in backStack.count() -1 downTo if (inclusive) index else index+1) backStack.removeLast()
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

    override fun getBackStackName(fragClazz: FragClazz): String? {
        if (!backStack.isEmpty()) {
            return backStack.findLast { it.qualifiedName == fragClazz.qualifiedName }?.name
        }
        return null
    }

    override fun getTopBackStackName(fragClazz: FragClazz): String? {
        if (!backStack.isEmpty()) {
            return backStack.find { it.qualifiedName == fragClazz.qualifiedName }?.name
        }
        return null
    }

    override fun getAllBackStackName(fragClazz: FragClazz): List<String> {
        if (!backStack.isEmpty()) {
            return backStack.filter { it.qualifiedName == fragClazz.qualifiedName }
                            .map { it.name }
        }
        return emptyList()
    }

    internal fun isLastFragment(fragment: Fragment): Boolean {
        if (!backStack.isEmpty()) {
            return backStack.last().qualifiedName == fragment::class.qualifiedName
        }
        return false
    }

    internal fun getUniqueBackStackName(clazz: FragClazz): String {
        val name = "${clazz.simpleName}${BACKSTACK_SUFFIX}"
        var id = 0
        while (true) {
            val current = "$name$$id"
            if (!hasBackStackName(current)) return current
            id++
        }
    }

    private fun hasBackStackName(name: String): Boolean {
        if (!backStack.isEmpty()) {
            return backStack.any { it.name == name }
        }
        return false
    }

    private fun popInternal(name: String, inclusive: Boolean) {
        val include = if (inclusive) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0
        fm.popBackStack(name, include)
    }

    internal fun onRestoreState(bundle: Bundle?) {
        val list = bundle?.getStringArrayList(SAVED_STATE) ?: return
        list.forEach { data ->
            val split = data.split('|')
            backStack.add(BackStackRecord(split[0], split[1]))
        }
    }

    internal fun onSaveState(bundle: Bundle) {
        val arrayList = ArrayList<String>()
        backStack.forEach { arrayList.add("${it.name}|${it.qualifiedName}") }
        bundle.putStringArrayList(SAVED_STATE, arrayList)
    }

    companion object {
        private const val BACKSTACK_SUFFIX = "_navigator"
        @VisibleForTesting
        internal const val SAVED_STATE = "com.kpstv.navigation:navigator:history_saved_state"
    }
}