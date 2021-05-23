package com.kpstv.navigation.internals

import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.base.navigation.internals.CommonNavigationImpl
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1

internal class BottomNavigationImpl(
    navigator: FragmentNavigator,
    internal val navView: BottomNavigationView,
    private val onNavSelectionChange: KFunction1<Int, Unit>,
    fragments: Map<Int, KClass<out Fragment>>,
    navigation: FragmentNavigator.Navigation
) : CommonNavigationImpl(
    navigator = navigator, navFragments = fragments, navigation = navigation, stateKeys = SaveStateKeys("$KEY_SELECTION_INDEX${navView.id}")
) {
    override fun setUpNavigationViewCallbacks(selectionId: Int) {
        navView.selectedItemId = selectionId
        navView.setOnNavigationItemSelectedListener(navigationListener)
    }

    override fun onNavigationSelectionChange(id: Int) {
        onNavSelectionChange.invoke(id)
    }

    private val navigationListener = BottomNavigationView.OnNavigationItemSelectedListener call@{ item ->
        return@call onSelectNavItem(item.itemId)
    }

    internal fun ignoreNavigationListeners(block: () -> Unit) {
        navView.setOnNavigationItemSelectedListener(null)
        block.invoke()
        navView.setOnNavigationItemSelectedListener(navigationListener)
    }

    companion object {
        private const val KEY_SELECTION_INDEX = "com.kpstv.navigation:bottom:key_index"
    }
}