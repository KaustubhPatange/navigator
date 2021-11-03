package com.kpstv.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentContainerView
import com.kpstv.home.fragments.HomeStartFragment
import com.kpstv.navigation.Destination
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : ValueFragment(R.layout.fragment_home), FragmentNavigator.Transmitter {

  private lateinit var navigator: FragmentNavigator
  override fun getNavigator(): FragmentNavigator = navigator

  @Inject
  lateinit var homeDependency : HomeDependency

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    requireActivity().title = "Module: home"

    homeDependency.call("home")

    navigator = FragmentNavigator.with(this, savedInstanceState)
      .initialize(view.findViewById(R.id.frag_container), Destination.of(HomeStartFragment::class))
  }
}