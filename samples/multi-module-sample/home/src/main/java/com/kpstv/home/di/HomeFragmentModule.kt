package com.kpstv.home.di

import androidx.fragment.app.Fragment
import com.kpstv.core.di.FragmentKey
import com.kpstv.home.fragments.HomeStartFragment
import com.kpstv.home_internal.HomeInternalFragment
import com.kpstv.home_internal2.HomeInternal2Fragment
import dagger.Binds
import dagger.Module
import dagger.hilt.migration.DisableInstallInCheck
import dagger.multibindings.IntoMap

@Module
@DisableInstallInCheck
abstract class HomeFragmentModule {
  @Binds
  @IntoMap
  @FragmentKey(HomeStartFragment::class)
  abstract fun homeStartFragment(homeStartFragment: HomeStartFragment): Fragment

  @Binds
  @IntoMap
  @FragmentKey(HomeInternalFragment::class)
  abstract fun homeInternalFragment(homeInternalFragment: HomeInternalFragment): Fragment

  @Binds
  @IntoMap
  @FragmentKey(HomeInternal2Fragment::class)
  abstract fun homeInternal2Fragment(homeInternal2Fragment: HomeInternal2Fragment): Fragment
}