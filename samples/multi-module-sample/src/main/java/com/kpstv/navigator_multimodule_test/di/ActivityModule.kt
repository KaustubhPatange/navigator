package com.kpstv.navigator_multimodule_test.di

import com.kpstv.home.di.HomeScope
import com.kpstv.home.HomeFragment
import com.kpstv.navigator_multimodule_test.navigation.welcome.WelcomeButtonClickImpl
import com.kpstv.welcome.WelcomeButtonClick
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class ActivityModule {
  @Binds
  abstract fun provideWelcomeButtonClick(welcomeButtonClick: WelcomeButtonClickImpl) : WelcomeButtonClick
}

@Module
@InstallIn(ActivityComponent::class)
class HomeFragmentModule {
  @Provides @HomeScope
  fun homeFragment() = HomeFragment()
}
