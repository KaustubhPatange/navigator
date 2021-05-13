package com.kpstv.bottom_navigation_sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.kpstv.bottom_navigation_sample.databinding.FragmentMainBinding
import com.kpstv.navigation.*
import kotlin.reflect.KClass

class MainFragment : ValueFragment(R.layout.fragment_main), NavigatorTransmitter {
    private lateinit var navigator: Navigator
    private lateinit var bottomController: BottomNavigationController
    private var viewBinding: FragmentMainBinding? = null

    override fun getNavigator(): Navigator = navigator

    override val forceBackPress: Boolean
        get() = viewBinding?.bottomNav?.selectedItemId != R.id.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMainBinding.bind(view).also { viewBinding = it }
        navigator = Navigator.with(this, savedInstanceState).initialize(binding.container)

        bottomController = navigator.install(object : Navigator.BottomNavigation(){
            override val bottomNavigationViewId: Int = R.id.bottom_nav
            override val bottomNavigationFragments: Map<Int, KClass<out Fragment>> =
                mapOf(
                    R.id.fragment_home to HomeFragment::class,
                    R.id.fragment_backup to BackupFragment::class,
                    R.id.fragment_settings to SettingFragment::class,
                )
            override val fragmentNavigationTransition = Animation.Fade
            override val fragmentViewRetentionType: ViewRetention = ViewRetention.RECREATE // or RETAIN to retain the views
        })
    }

    override fun onBackPressed(): Boolean {
        val binding = viewBinding ?: return false
        if (binding.bottomNav.selectedItemId != R.id.fragment_home) {
            bottomController.select(R.id.fragment_home)
            return true
        }
        return super.onBackPressed()
    }

    override fun onDestroyView() {
        viewBinding = null
        super.onDestroyView()
    }
}