package com.kpstv.navigator.test

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule

fun<A : Activity> ActivityScenario<A>.with(block: A.() -> Unit) {
    this.onActivity { block.invoke(it) }
}

fun<A : Activity> ActivityScenarioRule<A>.with(block: A.() -> Unit) = scenario.with(block)