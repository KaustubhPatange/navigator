package com.kpstv.bottom_navigation_sample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.kpstv.bottom_navigation_sample.databinding.FragmentSettingBinding
import com.kpstv.navigation.Destination
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment

class SettingFragment : ValueFragment(R.layout.fragment_setting), FragmentNavigator.Transmitter, FragmentNavigator.Navigation.Callbacks {
    private lateinit var navigator: FragmentNavigator

    override fun getNavigator(): FragmentNavigator = navigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSettingBinding.bind(view)
        navigator = FragmentNavigator.with(this, savedInstanceState)
            .initialize(binding.myContainer, Destination.of(SettingFragment1::class))
    }

    override fun onReselected() {
        Toast.makeText(requireContext(), "${this::class.simpleName} is reselected", Toast.LENGTH_SHORT).show()
    }
}

