package com.kpstv.bottom_navigation_sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import com.kpstv.bottom_navigation_sample.databinding.FragmentHomeBinding
import com.kpstv.navigation.*
import kotlin.reflect.KClass

class HomeFragment : ValueFragment(R.layout.fragment_home), NavigatorTransmitter {
    private lateinit var navigator: Navigator
    private lateinit var tabController: TabNavigationController

    private var viewBinding: FragmentHomeBinding? = null

    override fun getNavigator(): Navigator = navigator

    override val forceBackPress: Boolean
        get() = viewBinding?.tabLayout?.selectedTabPosition != 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view).also { viewBinding = it }
        navigator = Navigator.with(this, savedInstanceState).initialize(binding.container)
        tabController = navigator.install(this, object: Navigator.TabNavigation() {
            override val tabLayoutId: Int = R.id.tabLayout
            override val tabNavigationFragments: List<KClass<out Fragment>> = listOf(
                FirstFragment::class,
                SecondFragment::class
            )
            override val fragmentNavigationTransition: Animation = Animation.Slide
            override val fragmentViewRetentionType: ViewRetention = ViewRetention.RETAIN
        })
    }

    override fun onBackPressed(): Boolean {
        val binding = viewBinding ?: return false
        if (binding.tabLayout.selectedTabPosition != 0) {
            tabController.select(0)
            return true
        }
        return super.onBackPressed()
    }
}

class FirstFragment : ValueFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ScrollView(requireContext()).apply {
            addView(
                TextView(requireContext()).apply {
                    layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                        setMargins(20)
                    }
                    setText(R.string.lorem_ipsum)
                }
            )
        }
    }
}

class SecondFragment : ValueFragment(R.layout.fragment_second)