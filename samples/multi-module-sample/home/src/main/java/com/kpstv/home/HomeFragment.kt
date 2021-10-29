package com.kpstv.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentContainerView
import com.kpstv.core.di.DaggerFragmentFactory
import com.kpstv.home.di.HomeComponent
import com.kpstv.home.di.HomeComponentProvider
import com.kpstv.home.di.HomeScope
import com.kpstv.home.fragments.HomeStartFragment
import com.kpstv.navigation.Destination
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment
import javax.inject.Inject

class HomeFragment @Inject constructor(
  homeComponentFactory: HomeComponent.Factory
) : ValueFragment(R.layout.fragment_home), FragmentNavigator.Transmitter, HomeComponentProvider {

  private lateinit var navigator: FragmentNavigator
  override fun getNavigator(): FragmentNavigator = navigator

  override val homeComponent = homeComponentFactory.create(this)

  // if constructor injected then the fragment multi-bindings will be provided from
  // activityComponent i.e only WelcomeFragment & HomeFragment but we also
  // want all from the HomeFragmentModule. Hence field injection to inject from HomeComponent.
  @HomeScope @Inject
  lateinit var fragmentFactory: DaggerFragmentFactory

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    homeComponent.inject(this)
    super.onViewCreated(view, savedInstanceState)
    childFragmentManager.fragmentFactory = fragmentFactory

    requireActivity().title = "Module: home"

    navigator = FragmentNavigator.with(this, savedInstanceState)
      .initialize(view as FragmentContainerView, Destination.of(HomeStartFragment::class))
  }
}