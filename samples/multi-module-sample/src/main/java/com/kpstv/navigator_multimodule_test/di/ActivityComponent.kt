package com.kpstv.navigator_multimodule_test.di

import androidx.fragment.app.FragmentActivity
import com.kpstv.home.di.HomeComponent
import com.kpstv.navigator_multimodule_test.MainActivity
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent(modules = [ActivityModule::class, FragmentModule::class])
interface ActivityComponent {

  fun homeFragmentComponent() : HomeComponent.Factory

  @Subcomponent.Factory
  interface Factory {
    fun activity(@BindsInstance activity: FragmentActivity) : ActivityComponent
  }

  fun inject(main: MainActivity)
}