package com.kpstv.home.fragments

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class HomeStartViewModel @Inject constructor() : ViewModel() {

  val state: Flow<HomeStartState> = flow {
    emit(HomeStartState.Loading)
    delay(1000)
    emit(HomeStartState.Completed)
  }
}

sealed class HomeStartState(val text: String, val enabled: Boolean) {
  object Loading : HomeStartState("Loading in 1s...", false)
  object Completed : HomeStartState("Completed", true)
}