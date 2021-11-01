package com.kpstv.navigator_multimodule_test.di

import com.kpstv.home.di.HomeComponent
import com.kpstv.navigator_multimodule_test.MainActivity
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module(includes = [ActivityModule::class, FragmentModule::class])
interface MainActivityAggregatorModule

@InstallIn(ActivityComponent::class)
@EntryPoint
interface MainActivityComponent {

  fun homeComponentFactory(): HomeComponent.Factory

  fun inject(main: MainActivity)
}