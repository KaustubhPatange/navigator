package com.kpstv.navigation.base.navigation.internals

import androidx.fragment.app.Fragment
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigator.test.FirstFragment
import com.kpstv.navigator.test.SecondFragment
import com.kpstv.navigator.test.ThirdFragment
import kotlin.reflect.KClass

class CustomNavigation(topSelectedId: Int = 0, option: ViewRetention = ViewRetention.RECREATE) : FragmentNavigator.Navigation() {
    override val selectedFragmentId: Int = topSelectedId
    override val fragmentViewRetentionType: ViewRetention = option
    val fragments = mapOf<Int, KClass<out Fragment>>(
        0 to FirstFragment::class,
        1 to SecondFragment::class,
        2 to ThirdFragment::class
    )
}

class CustomCommonNavigationImpl(
    navigator: FragmentNavigator,
    fragments: Map<Int, KClass<out Fragment>>,
    navigation: FragmentNavigator.Navigation
) : CommonNavigationImpl(navigator, fragments, navigation) {
    var firstId: Int = -1
    var selectedId: Int = -1
    override fun setUpNavigationViewCallbacks(selectionId: Int) {
        this.firstId = selectionId
    }
    override fun onNavigationSelectionChange(id: Int) {
        this.selectedId = id
    }
}