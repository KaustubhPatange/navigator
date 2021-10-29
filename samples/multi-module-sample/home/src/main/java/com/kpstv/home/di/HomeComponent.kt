package com.kpstv.home.di

import com.kpstv.home.HomeFragment
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent(modules = [HomeFragmentModule::class, HomeDependencyModule::class, HomeModule::class])
interface HomeComponent {

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance fragment: HomeFragment) : HomeComponent
  }

  fun inject(home: HomeFragment)
}

interface HomeComponentProvider {
  val homeComponent: HomeComponent
}