package com.kpstv.home_internal2

import android.os.Bundle
import android.view.View
import com.kpstv.navigation.ValueFragment
import javax.inject.Inject

class HomeInternal2Fragment @Inject constructor() : ValueFragment(R.layout.fragment_home_internal2) {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    requireActivity().title = "Module: home-internal2"
  }
}