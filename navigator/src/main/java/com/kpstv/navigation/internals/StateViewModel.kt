package com.kpstv.navigation.internals

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal class StateViewModel(private val saveState: Bundle) : ViewModel() {
    fun putHistory(identifier: String, bundle: Bundle) {
        saveState.putBundle(identifier, bundle)
    }
    fun getHistory(identifier: String): Bundle? {
        return saveState.getBundle(identifier)
    }
    // Force the use of default factory.
    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(StateViewModel::class.java)) {
                StateViewModel(Bundle()) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}