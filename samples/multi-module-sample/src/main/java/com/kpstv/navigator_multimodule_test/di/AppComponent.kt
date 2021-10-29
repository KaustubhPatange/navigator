package com.kpstv.navigator_multimodule_test.di

import android.app.Application
import com.kpstv.navigator_multimodule_test.App
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module(includes = [])
interface AppAggregatorModule

@InstallIn(SingletonComponent::class)
@EntryPoint
interface AppComponent {

  fun activityComponent() : ActivityComponent.Factory

  fun inject(app: App)
}


interface AppComponentProvider {
  val appComponent: AppComponent
}