package com.kpstv.navigation.compose.sample.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HiltTestViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var data: String = "NOT_SET"
        private set

    init {
        savedStateHandle.get<String>("data")?.also { data = it }
        if (data == "NOT_SET") {
            data = "%.3f".format(Random.nextDouble())
        }
    }

    fun set(data: String) {
        savedStateHandle.set("data", data)
        this.data = data
    }
}