package com.kpstv.bottom_navigation_sample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.kpstv.bottom_navigation_sample.databinding.FragmentSettingBinding
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.NavigatorTransmitter
import com.kpstv.navigation.ValueFragment

class SettingFragment : ValueFragment(R.layout.fragment_setting), NavigatorTransmitter, Navigator.Navigation.Callbacks {
    private lateinit var navigator: Navigator

    override fun getNavigator(): Navigator = navigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSettingBinding.bind(view)
        navigator = Navigator.with(this, savedInstanceState)
            .initialize(binding.myContainer)

        // In multiple backstack navigation the fragment manager will save all necessary information that
        // defines the earlier transaction.
        // This is also reflected in Navigator's history so it is necessary to check if history is not empty
        // to prohibit the first navigate call which will create duplicate backstack record.
        if (navigator.getHistory().isEmpty() && savedInstanceState == null) {
            navigator.navigateTo(SettingFragment1::class)
        }
    }

    override fun onReselected() {
        Toast.makeText(requireContext(), "${this::class.simpleName} is reselected", Toast.LENGTH_SHORT).show()
    }
}

