package com.kpstv.home.di

import androidx.fragment.app.FragmentFactory
import com.kpstv.core.di.DaggerFragmentFactory
import com.kpstv.home.HomeButtonClickedImpl
import com.kpstv.home.HomeDependency
import com.kpstv.home_internal.HomeButtonClicked
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck

@Module
@DisableInstallInCheck
abstract class HomeModule {
  @Binds
  abstract fun homeButtonClicked(homeButtonClicked: HomeButtonClickedImpl): HomeButtonClicked

  @Binds
  @HomeScope
  abstract fun fragmentFactory(daggerFragmentFactory: DaggerFragmentFactory): FragmentFactory
}

@Module
@DisableInstallInCheck
class HomeDependencyModule {
  @Provides
  fun homeDependency(): HomeDependency = HomeDependency()
}