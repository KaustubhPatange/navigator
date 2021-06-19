package com.kpstv.navigation.base.navigation.internals

import android.os.Bundle
import android.view.View
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment
import com.kpstv.navigator.test.FirstFragment
import com.kpstv.navigator.test.NavigatorFragment
import com.kpstv.navigator.test.R

class TestNavigationFragment : ValueFragment(R.layout.activity_main), FragmentNavigator.Transmitter {
    private lateinit var navigator: FragmentNavigator
    override fun getNavigator(): FragmentNavigator = navigator

    lateinit var baseNavigation: CustomCommonNavigationImpl

    override val forceBackPress: Boolean
        get() = baseNavigation.selectedId != com.kpstv.navigator.base.navigation.R.id.navigator_fragment_1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigator = FragmentNavigator.with(this, savedInstanceState)
            .initialize(view.findViewById(R.id.my_container))
        val custom = Custom3Navigation(
            fragments = mapOf(
                com.kpstv.navigator.base.navigation.R.id.navigator_fragment_1 to NavigatorFragment::class,
                com.kpstv.navigator.base.navigation.R.id.first_fragment to FirstFragment::class,
                com.kpstv.navigator.base.navigation.R.id.navigator_fragment_2 to NavigatorFragment::class
            ),
            topSelectedId = com.kpstv.navigator.base.navigation.R.id.navigator_fragment_1
        )
        baseNavigation = CustomCommonNavigationImpl(navigator, custom.fragments, custom)
        baseNavigation.onCreate(savedInstanceState)
        parentFragmentManager.registerFragmentLifecycleCallbacks(
            FragmentNavigationLifecycle(this, baseNavigation) { f, b ->
                assert(!b.isEmpty && b["key_index"] is Int)
            }, false
        )
    }

    override fun onBackPressed(): Boolean {
        if (baseNavigation.selectedId != com.kpstv.navigator.base.navigation.R.id.navigator_fragment_1) {
            baseNavigation.onSelectNavItem(com.kpstv.navigator.base.navigation.R.id.navigator_fragment_1)
            return true
        }
        return super.onBackPressed()
    }
}