package com.kpstv.home

import android.util.Log
import javax.inject.Inject

class HomeDependency @Inject constructor() {
  fun call(tag: String) {
    Log.e("HomeDependency:$tag", "Function called")
  }
}
