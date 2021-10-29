package com.kpstv.navigator_multimodule_test

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kpstv.navigator_multimodule_test.di.AppComponent
import com.kpstv.navigator_multimodule_test.di.AppComponentProvider
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), AppComponentProvider, Configuration.Provider {
  override val appComponent by lazy { EntryPointAccessors.fromApplication(this, AppComponent::class.java) }

  @Inject lateinit var workerFactory: HiltWorkerFactory

  override fun getWorkManagerConfiguration(): Configuration {
    return Configuration.Builder().setWorkerFactory(workerFactory).build()
  }

  override fun onCreate() {
    appComponent.inject(this)
    super.onCreate()
  }
}