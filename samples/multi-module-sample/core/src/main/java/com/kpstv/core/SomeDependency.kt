package com.kpstv.core

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SomeDependency @Inject constructor() {
  fun getData(value: Int) : String = "SomeDependency Data: $value"
}