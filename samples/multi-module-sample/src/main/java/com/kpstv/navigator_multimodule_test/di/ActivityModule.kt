package com.kpstv.navigator_multimodule_test.di

import com.kpstv.navigator_multimodule_test.navigation.welcome.WelcomeButtonClickImpl
import com.kpstv.welcome.WelcomeButtonClick
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class ActivityModule {
  @Binds
  abstract fun provideWelcomeButtonClick(welcomeButtonClick: WelcomeButtonClickImpl) : WelcomeButtonClick
}