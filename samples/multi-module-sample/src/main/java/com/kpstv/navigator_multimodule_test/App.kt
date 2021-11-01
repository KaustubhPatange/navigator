package com.kpstv.navigator_multimodule_test

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {
  @Inject lateinit var workerFactory: HiltWorkerFactory

  override fun getWorkManagerConfiguration(): Configuration {
    return Configuration.Builder().setWorkerFactory(workerFactory).build()
  }
}