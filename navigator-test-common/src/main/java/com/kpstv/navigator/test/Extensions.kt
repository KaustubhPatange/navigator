package com.kpstv.navigator.test

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment
import kotlin.reflect.KClass

fun<A : Activity> ActivityScenario<A>.with(block: A.() -> Unit) {
    this.onActivity { block.invoke(it) }
}

fun<A : Activity> ActivityScenarioRule<A>.with(block: A.() -> Unit) = scenario.with(block)

fun Any?.matchClass(clazz: KClass<out Any>): Boolean {
    if (this != null) {
        return this::class == clazz
    }
    return false
}

inline fun<reified T: ValueFragment> FragmentNavigator.get(): T {
    return getCurrentFragment() as T
}

tailrec fun<T: Fragment> FragmentNavigator.verifyRecursive(obj: KClass<T>): Boolean {
    if (getCurrentFragment().matchClass(obj)) return true
    val current = getCurrentFragment()
    if (current is FragmentNavigator.Transmitter) return current.getNavigator().verifyRecursive(obj)
    return false
}

fun Fragment.executeTransactionsRecursive() {
    if (this is FragmentNavigator.Transmitter) {
        val frag = this.getNavigator().getCurrentFragment() ?: return
        frag.executeTransactionsRecursive()
    }
    this.childFragmentManager.executePendingTransactions()
}