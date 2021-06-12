package com.kpstv.navigation.base.navigation.internals

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commitNow
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment
import com.kpstv.navigation.internals.ViewStateFragment
import com.kpstv.navigator.base.navigation.R
import kotlin.reflect.KClass

@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class CommonNavigationImpl(
    private val navigator: FragmentNavigator,
    private val navFragments: Map<Int, KClass<out Fragment>>,
    private val navigation: FragmentNavigator.Navigation,
    private val stateKeys: SaveStateKeys
) : CommonLifecycleCallbacks {

    abstract fun setUpNavigationViewCallbacks(selectionId: Int)
    abstract fun onNavigationSelectionChange(id: Int)

    private val fm = navigator.getFragmentManager()
    private val containerView = navigator.getContainerView()

    private var fragments = arrayListOf<Fragment>()
    private var selectedIndex = if (navigation.selectedFragmentId != -1)
        getPrimarySelectionFragmentId()
    else 0
    private val selectedFragment get() = fragments[selectedIndex]

    private var topSelectionId = if (navigation.selectedFragmentId != -1)
        navigation.selectedFragmentId
    else navFragments.keys.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            fm.commitNow {
                navFragments.forEach { (id, frag) ->
                    val tagFragment = fm.findFragmentByTag(getFragmentTagFromId(id))?.also { fragments.add(it) }
                    if (tagFragment == null) {
                        val fragment = frag.java.getConstructor().newInstance().also { fragments.add(it) }
                        add(containerView.id, fragment, getFragmentTagFromId(id))
                    }
                }
            }
            when(navigation.fragmentViewRetentionType) {
                FragmentNavigator.Navigation.ViewRetention.RECREATE ->  fm.commitNow { fragments.forEach { detach(it) } }
                FragmentNavigator.Navigation.ViewRetention.RETAIN -> fm.commitNow { fragments.forEach { hide(it) } }
            }
        } else {
            navFragments.keys.forEach{ id ->
                val fragment = fm.findFragmentByTag(getFragmentTagFromId(id))!!
                fragments.add(fragment)
            }
            selectedIndex = savedInstanceState.getInt(stateKeys.keyIndex, 0)
            topSelectionId = navFragments.keys.elementAt(selectedIndex)
        }

        setUpNavigationViewCallbacks(topSelectionId)

        setFragment(selectedFragment)
    }

    fun onSelectNavItem(id: Int, args: BaseArgs? = null) : Boolean {
        runChecks()
        val fragment = getFragmentFromId(id)!!
        if (fragment is ValueFragment) {
            if (args == null) {
                fragment.arguments?.clear()
            } else {
                fragment.arguments = Bundle().apply {
                    putParcelable(ValueFragment.createArgKey(args), args)
                }
            }
        }
        if (selectedFragment === fragment) {
            if (fragment is FragmentNavigator.Navigation.Callbacks && fragment.isVisible) {
                fragment.onReselected()
            }
        } else {
            setFragment(fragment, true)
        }
        return true
    }


    private fun setFragment(whichFragment: Fragment, runIfHasAnimations: Boolean = false) {
        val current = navigator.getCurrentFragment()

        if (navigation.fragmentViewRetentionType == FragmentNavigator.Navigation.ViewRetention.RETAIN) {
            if (whichFragment is ViewStateFragment) whichFragment.onViewStateChanged(ViewStateFragment.ViewState.FOREGROUND)
            if (current is ViewStateFragment) current.onViewStateChanged(ViewStateFragment.ViewState.BACKGROUND)
        }

        internalSetFragment(current, whichFragment, runIfHasAnimations)
    }

    private fun internalSetFragment(current: Fragment?, whichFragment: Fragment, runIfHasAnimations: Boolean = false) {
        var transaction = fm.beginTransaction()
        if (runIfHasAnimations && current != null) {
            setAnimations(transaction, fromIndex = fragments.indexOf(current), toIndex = fragments.indexOf(whichFragment))
        }
        fragments.forEachIndexed { index, fragment ->
            if (fragment === whichFragment) {
                transaction = when(navigation.fragmentViewRetentionType) {
                    FragmentNavigator.Navigation.ViewRetention.RECREATE -> transaction.attach(fragment)
                    FragmentNavigator.Navigation.ViewRetention.RETAIN -> transaction.show(fragment)
                }
                selectedIndex = index

                if (fragment is FragmentNavigator.Navigation.Callbacks) {
                    fragment.onSelected()
                }
            } else {
                transaction = when(navigation.fragmentViewRetentionType) {
                    FragmentNavigator.Navigation.ViewRetention.RECREATE -> transaction.detach(fragment)
                    FragmentNavigator.Navigation.ViewRetention.RETAIN -> transaction.hide(fragment)
                }
            }
        }
        transaction.commit()

        onNavigationSelectionChange(navFragments.keys.elementAt(selectedIndex))
    }

    private fun setAnimations(ft: FragmentTransaction, fromIndex: Int, toIndex: Int) {
        when (navigation.fragmentNavigationTransition) {
            is FragmentNavigator.Navigation.Animation.None -> {}
            is FragmentNavigator.Navigation.Animation.SlideHorizontally -> {
                if (fromIndex < toIndex)
                    ft.setCustomAnimations(R.anim.navigator_slide_in_right, R.anim.navigator_slide_out_left)
                if (fromIndex > toIndex)
                    ft.setCustomAnimations(R.anim.navigator_slide_in_left, R.anim.navigator_slide_out_right)
            }
            is FragmentNavigator.Navigation.Animation.SlideVertically -> {
                if (fromIndex < toIndex)
                    ft.setCustomAnimations(R.anim.navigator_slide_in_bottom, R.anim.navigator_slide_out_top)
                if (fromIndex > toIndex)
                    ft.setCustomAnimations(R.anim.navigator_slide_in_top, R.anim.navigator_slide_out_bottom)
            }
            else -> ft.setCustomAnimations(navigation.fragmentNavigationTransition.enter, navigation.fragmentNavigationTransition.exit)
        }
    }

    private fun getPrimarySelectionFragmentId(): Int = navFragments.keys.indexOf(navigation.selectedFragmentId)

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(stateKeys.keyIndex, selectedIndex)
    }

    private fun getFragmentFromId(@IdRes id: Int): Fragment? {
        val tag = getFragmentTagFromId(id)
        return fm.findFragmentByTag(tag)
    }

    private fun getFragmentTagFromId(id: Int) : String {
        return "${navFragments[id]!!.qualifiedName}_${id}_$FRAGMENT_SUFFIX"
    }

    private fun runChecks() {
        check(fragments.isNotEmpty()) { "Fragment list are empty. Did you forgot to call onCreate()?" }
    }

    data class SaveStateKeys(val keyIndex: String)

    companion object {
        private const val FRAGMENT_SUFFIX = "base_navigation"
    }
}