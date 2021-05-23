package com.kpstv.bottom_navigation_sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.ValueFragment

open class AbstractFragment : ValueFragment(R.layout.fragment_abstract), FragmentNavigator.Navigation.Callbacks {

    fun setupButton(view: View, buttonText: String, onClick: () -> Unit) {
        view.findViewById<Button>(R.id.btn_navigate).apply {
            text = buttonText
            setOnClickListener { onClick.invoke() }
            visibility = View.VISIBLE
        }
    }

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

class BackupFragment : AbstractFragment()
class SettingFragment1 : AbstractFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButton(view, "Go to Second Fragment") {
            getParentNavigator().navigateTo(SettingFragment2::class, FragmentNavigator.NavOptions(remember = true))
        }
    }
}
class SettingFragment2 : AbstractFragment()