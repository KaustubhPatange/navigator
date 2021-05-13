package com.kpstv.navigator.test

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
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