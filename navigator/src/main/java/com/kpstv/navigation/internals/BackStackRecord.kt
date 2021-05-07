package com.kpstv.navigation.internals

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

internal class BackStackRecord(
    private val name: String,
    private val lifecycleOwner: LifecycleOwner
) {

}