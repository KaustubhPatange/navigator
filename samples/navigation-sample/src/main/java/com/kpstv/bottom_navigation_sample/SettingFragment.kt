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

    private var isCreated: Boolean = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // In case of bottom & tab navigation view is detached & fragment is stopped but not destroyed.
        // Hence all the class declared fields will exist & must not be created again.
        // Normally it's not problem to create them but in case of multiple backstack Navigator must not be created again.
        if (!isCreated) {
            val binding = FragmentSettingBinding.bind(view)

            navigator = Navigator.with(this, savedInstanceState)
                .initialize(binding.myContainer)

            if (savedInstanceState == null) {
                navigator.navigateTo(SettingFragment1::class)
            }
        }

        isCreated = true
    }

    override fun onReselected() {
        Toast.makeText(requireContext(), "${this::class.simpleName} is reselected", Toast.LENGTH_SHORT).show()
    }
}

