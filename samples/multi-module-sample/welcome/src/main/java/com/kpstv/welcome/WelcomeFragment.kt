package com.kpstv.welcome

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.kpstv.core.SomeDependency
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeFragment : ValueFragment(R.layout.fragment_welcome) {

  @Inject lateinit var someDependency: SomeDependency
  @Inject lateinit var welcomeButtonClick: WelcomeButtonClick

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    requireActivity().title = "Module: welome"

    val textView = view.findViewById<TextView>(R.id.textView)
    textView.text = "${someDependency.getData(100)}"

    val button = view.findViewById<Button>(R.id.btn)
    button.setOnClickListener {
      welcomeButtonClick.goToNext(
        FragmentNavigator.NavOptions(remember = true)
      )
    }
  }
}