package com.kpstv.navigator_multimodule_test.di

import androidx.fragment.app.Fragment
import com.kpstv.core.di.FragmentKey
import com.kpstv.home.HomeFragment
import com.kpstv.welcome.WelcomeFragment
import dagger.Binds
import dagger.Module
import dagger.hilt.migration.DisableInstallInCheck
import dagger.multibindings.IntoMap

@Module
@DisableInstallInCheck
abstract class FragmentModule {
  @Binds
  @IntoMap
  @FragmentKey(WelcomeFragment::class)
  abstract fun welcomeFragment(welcomeFragment: WelcomeFragment) : Fragment

  @Binds
  @IntoMap
  @FragmentKey(HomeFragment::class)
  abstract fun homeFragment(homeFragment: HomeFragment) : Fragment
}