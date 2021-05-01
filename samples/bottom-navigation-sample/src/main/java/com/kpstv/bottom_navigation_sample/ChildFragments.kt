package com.kpstv.bottom_navigation_sample

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.ValueFragment

open class AbstractFragment : ValueFragment(R.layout.fragment_abstract), Navigator.BottomNavigation.Callbacks {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.title).text = this::class.simpleName
    }

    override fun onReselected() {
        Toast.makeText(requireContext(), "${this::class.simpleName} is reselected", Toast.LENGTH_SHORT).show()
    }
}

class HomeFragment : AbstractFragment()
class BackupFragment : AbstractFragment()
class SettingFragment : AbstractFragment()
