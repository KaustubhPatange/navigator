package com.kpstv.home

import android.util.Log
import javax.inject.Inject

class HomeDependency @Inject constructor() {
  fun call() {
    Log.e("HomeDependency", "Function called")
  }
}
