package com.kpstv.navigation.compose.sample.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HiltTestViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val data = MutableLiveData("NOT_SET")

    init {
        savedStateHandle.get<String>("data")?.also { data.value = it }
        if (data.value == "NOT_SET") {
            set("%.3f".format(Random.nextDouble()))
        }
    }

    fun set(data: String) {
        savedStateHandle.set("data", data)
        this.data.value = data
    }
}