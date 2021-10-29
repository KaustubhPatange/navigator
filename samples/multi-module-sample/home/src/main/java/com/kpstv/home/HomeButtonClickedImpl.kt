package com.kpstv.home

import com.kpstv.home_internal.HomeButtonClicked
import com.kpstv.home_internal2.HomeInternal2Fragment
import com.kpstv.navigation.FragmentNavigator
import javax.inject.Inject

class HomeButtonClickedImpl @Inject constructor(
  private val fragment: HomeFragment
) : HomeButtonClicked {
  override fun goToNext(navOptions: FragmentNavigator.NavOptions) {
    fragment.getNavigator().navigateTo(HomeInternal2Fragment::class, navOptions)
  }
}