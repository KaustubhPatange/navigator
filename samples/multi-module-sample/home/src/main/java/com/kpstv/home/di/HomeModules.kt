package com.kpstv.home.di

import com.kpstv.home.navigation.home_internal.HomeButtonClickedImpl
import com.kpstv.home.HomeDependency
import com.kpstv.home_internal.HomeButtonClicked
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class HomeDependencyModule {
  @Provides
  fun homeDependency(): HomeDependency = HomeDependency()
}

@Module
@InstallIn(FragmentComponent::class)
abstract class HomeModule {

  @Binds
  abstract fun provideHomeButtonClick(homeButtonClicked: HomeButtonClickedImpl) : HomeButtonClicked
}
