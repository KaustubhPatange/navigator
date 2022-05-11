package com.kpstv.home.navigation.home_internal

import com.kpstv.home.HomeFragment
import com.kpstv.home.di.HomeQualifier
import com.kpstv.home_internal.HomeButtonClicked
import com.kpstv.home_internal2.HomeInternal2Fragment
import com.kpstv.navigation.FragmentNavigator
import javax.inject.Inject

class HomeButtonClickedImpl @Inject constructor(
  @HomeQualifier private val fragment: HomeFragment
) : HomeButtonClicked {
  override fun goToNext(navOptions: FragmentNavigator.NavOptions) {
    fragment.getNavigator().navigateTo(HomeInternal2Fragment::class, navOptions)
  }
}