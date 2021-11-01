package com.kpstv.navigator_multimodule_test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.core.di.DaggerFragmentFactory
import com.kpstv.navigation.Destination
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.canFinish
import com.kpstv.navigator_multimodule_test.databinding.ActivityMainBinding
import com.kpstv.navigator_multimodule_test.di.MainActivityComponent
import com.kpstv.welcome.WelcomeFragment
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), FragmentNavigator.Transmitter {
  private lateinit var navigator: FragmentNavigator
  override fun getNavigator(): FragmentNavigator = navigator

  private lateinit var binding: ActivityMainBinding

  @Inject lateinit var daggerFragmentFactory: DaggerFragmentFactory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbar)

    supportFragmentManager.fragmentFactory = daggerFragmentFactory

    navigator = FragmentNavigator.with(this, savedInstanceState)
      .initialize(binding.fragContainer, Destination.of(WelcomeFragment::class))

  }

  override fun onBackPressed() {
    if (navigator.canFinish()) super.onBackPressed()
  }
}