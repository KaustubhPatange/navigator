package com.kpstv.core.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import dagger.Reusable
import javax.inject.Inject
import javax.inject.Provider

class DaggerFragmentFactory @Inject constructor(
  private val fragments: Map<Class<out Fragment>, @JvmSuppressWildcards Provider<Fragment>>
) : FragmentFactory() {

  override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
    val fragmentClass: Class<out Fragment> = loadFragmentClass(classLoader, className)
    return fragments[fragmentClass]?.get() ?: super.instantiate(classLoader, className)
  }

}