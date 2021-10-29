package com.kpstv.core

import com.kpstv.navigation.FragmentNavigator

interface Navigate {
  fun goToNext(navOptions: FragmentNavigator.NavOptions = FragmentNavigator.NavOptions())
}