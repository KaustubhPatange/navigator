package com.kpstv.bottom_navigation_sample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.kpstv.bottom_navigation_sample.databinding.FragmentSettingBinding
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.ValueFragment

class SettingFragment : ValueFragment(R.layout.fragment_setting), FragmentNavigator.Transmitter, FragmentNavigator.Navigation.Callbacks {
    private lateinit var navigator: FragmentNavigator

    override fun getNavigator(): FragmentNavigator = navigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSettingBinding.bind(view)
        navigator = Navigator.with(this, savedInstanceState)
            .setNavigator(FragmentNavigator::class)
            .initialize(binding.myContainer)

        // In multiple backstack navigation the fragment manager will saves all necessary information that
        // defines the earlier transaction based on fragment's lifecycle changes.
        // This is also reflected in Navigator's history so it is necessary to check if history is not empty
        // to prohibit the first navigate call which will create duplicate backstack records.
        if (navigator.getHistory().isEmpty() && savedInstanceState == null) {
            navigator.navigateTo(SettingFragment1::class)
        }
    }

    override fun onReselected() {
        Toast.makeText(requireContext(), "${this::class.simpleName} is reselected", Toast.LENGTH_SHORT).show()
    }
}

