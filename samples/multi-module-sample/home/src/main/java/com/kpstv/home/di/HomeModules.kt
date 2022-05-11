package com.kpstv.home.di

import androidx.fragment.app.Fragment
import com.kpstv.home.navigation.home_internal.HomeButtonClickedImpl
import com.kpstv.home.HomeDependency
import com.kpstv.home.HomeFragment
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

  @Provides @HomeQualifier
  fun homeFragment(fragment: Fragment) : HomeFragment {
    return fragment.requireActivity().supportFragmentManager.fragments.find { it is HomeFragment } as HomeFragment
  }
}

@Module
@InstallIn(FragmentComponent::class)
abstract class HomeModule {

  @Binds
  abstract fun provideHomeButtonClick(homeButtonClicked: HomeButtonClickedImpl) : HomeButtonClicked
}
