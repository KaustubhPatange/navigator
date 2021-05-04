package com.kpstv.bottom_navigation_sample

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.ValueFragment

open class AbstractFragment : ValueFragment(R.layout.fragment_abstract), Navigator.Navigation.Callbacks {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.title).text = this::class.simpleName
    }

    override fun onReselected() {
        Toast.makeText(requireContext(), "${this::class.simpleName} is reselected", Toast.LENGTH_SHORT).show()
    }

    override fun onViewStateChanged(viewState: ViewState) {
        Log.e(this::class.simpleName, "viewState: $viewState")
        super.onViewStateChanged(viewState)
    }

    override fun onDestroyView() {
        Log.e(this::class.simpleName, "onDestroyView()")
        super.onDestroyView()
    }
}

class HomeFragment : AbstractFragment()
class BackupFragment : AbstractFragment()
class SettingFragment : AbstractFragment()
