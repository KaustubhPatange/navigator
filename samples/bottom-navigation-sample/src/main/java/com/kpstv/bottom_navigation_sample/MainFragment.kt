package com.kpstv.bottom_navigation_sample

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.kpstv.bottom_navigation_sample.databinding.FragmentMainBinding
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.NavigatorTransmitter
import com.kpstv.navigation.ValueFragment
import com.kpstv.navigation.install
import kotlin.reflect.KClass

class MainFragment : ValueFragment(R.layout.fragment_main), NavigatorTransmitter {
    private lateinit var navigator: Navigator
    private var viewBinding: FragmentMainBinding? = null

    override fun getNavigator(): Navigator = navigator

    override val forceBackPress: Boolean
        get() = viewBinding?.bottomNav?.selectedItemId != R.id.fragment_home ||
                viewBinding?.root?.isDrawerOpen(GravityCompat.START) == true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMainBinding.bind(view).also { viewBinding = it }
        navigator = Navigator(childFragmentManager, binding.container)

        navigator.install(this, object : Navigator.BottomNavigation(){
            override val bottomNavigationViewId: Int = R.id.bottom_nav
            override val bottomNavigationFragments: Map<Int, KClass<out Fragment>> =
                mapOf(
                    R.id.fragment_home to HomeFragment::class,
                    R.id.fragment_backup to BackupFragment::class,
                    R.id.fragment_settings to SettingFragment::class,
                )
        })

        binding.toolbar.setNavigationOnClickListener {
            binding.root.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed(): Boolean {
        val binding = viewBinding ?: return false
        if (binding.root.isDrawerOpen(GravityCompat.START)) {
            binding.root.closeDrawer(GravityCompat.START)
            return true
        }
        if (binding.bottomNav.selectedItemId != R.id.fragment_home) {
            binding.bottomNav.selectedItemId = R.id.fragment_home
            return true
        }
        return super.onBackPressed()
    }

    override fun onDestroyView() {
        viewBinding = null
        super.onDestroyView()
    }
}