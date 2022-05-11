package com.kpstv.navigator_multimodule_test.navigation.welcome

import androidx.fragment.app.FragmentActivity
import com.kpstv.home.HomeFragment
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.welcome.WelcomeButtonClick
import javax.inject.Inject

class WelcomeButtonClickImpl @Inject constructor(
  private val activity: FragmentActivity
) : WelcomeButtonClick {
  override fun goToNext(navOptions: FragmentNavigator.NavOptions) {
    if (activity is FragmentNavigator.Transmitter) {
      activity.getNavigator().navigateTo(HomeFragment::class, navOptions)
    } else throw IllegalArgumentException("Could not navigate")
  }
}