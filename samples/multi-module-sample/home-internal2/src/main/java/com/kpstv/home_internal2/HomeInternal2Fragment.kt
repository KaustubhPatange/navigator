package com.kpstv.home_internal2

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.kpstv.navigation.ValueFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeInternal2Fragment : ValueFragment(R.layout.fragment_home_internal2) {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // don't do this, this is just for differentiating nested modules
    requireParentFragment().requireView().findViewById<Toolbar>(R.id.toolbar).title = "Module: home-internal2"
  }
}