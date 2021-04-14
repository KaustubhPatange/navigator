package com.kpstv.navigation.basic_sample

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

inline fun <reified VM : ViewModel> Fragment.viewModels(
    crossinline owner: () -> ViewModelStoreOwner = { this }
): Lazy<VM> = lazy { ViewModelProvider(owner.invoke()).get(VM::class.java) }