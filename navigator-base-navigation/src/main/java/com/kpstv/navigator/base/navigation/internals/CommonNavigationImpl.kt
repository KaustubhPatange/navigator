package com.kpstv.navigator.base.navigation.internals

import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commitNow
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.ValueFragment
import com.kpstv.navigation.internals.ViewStateFragment
import com.kpstv.navigator.base.navigation.R
import kotlin.reflect.KClass

@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class CommonNavigationImpl(
    private val fm: FragmentManager,
    private val containerView: FrameLayout,
    private val navFragments: Map<Int, KClass<out Fragment>>,
    private val navigation: Navigator.Navigation
) : CommonLifecycleCallbacks {

    abstract fun setUpNavigationViewCallbacks(selectionId: Int)
    abstract fun onNavigationSelectionChange(id: Int)

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
                navFragments.values.forEach { frag ->
                    val tagFragment = fm.findFragmentByTag(frag.simpleName + FRAGMENT_SUFFIX)?.also { fragments.add(it) }
                    if (tagFragment == null) {
                        val fragment = frag.java.getConstructor().newInstance().also { fragments.add(it) }
                        add(containerView.id, fragment, frag.simpleName + FRAGMENT_SUFFIX)
                    }
                }
            }

            when(navigation.fragmentViewRetentionType) {
                Navigator.ViewRetention.RECREATE ->  fm.commitNow { fragments.forEach { detach(it) } }
                Navigator.ViewRetention.RETAIN -> fm.commitNow { fragments.forEach { hide(it) } }
            }
        } else {
            navFragments.values.forEach { frag ->
                val fragment = fm.findFragmentByTag(frag.simpleName + FRAGMENT_SUFFIX)!!
                fragments.add(fragment)
            }
            selectedIndex = savedInstanceState.getInt(KEY_SELECTION_INDEX, 0)
            topSelectionId = navFragments.keys.elementAt(selectedIndex)
        }

        setUpNavigationViewCallbacks(topSelectionId)

        setFragment(selectedFragment)
    }

    fun onSelectNavItem(id: Int, args: BaseArgs? = null) : Boolean {
        val fragment = getFragmentFromId(id)!!
        if (fragment is ValueFragment) {
            if (args == null) {
                fragment.arguments?.remove(ValueFragment.ARGUMENTS)
            } else {
                fragment.arguments = Bundle().apply {
                    putParcelable(ValueFragment.ARGUMENTS, args)
                }
            }
        }
        if (selectedFragment === fragment) {
            if (fragment is Navigator.Navigation.Callbacks && fragment.isVisible) {
                fragment.onReselected()
            }
        } else {
            setFragment(fragment, true)
        }
        return true
    }


    private fun setFragment(whichFragment: Fragment, runIfHasAnimations: Boolean = false) {
        val current = Navigator.getCurrentVisibleFragment(fm, containerView)

        if (navigation.fragmentViewRetentionType == Navigator.ViewRetention.RETAIN) {
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
            if (fragment == whichFragment) {
                transaction = when(navigation.fragmentViewRetentionType) {
                    Navigator.ViewRetention.RECREATE -> transaction.attach(fragment)
                    Navigator.ViewRetention.RETAIN -> transaction.show(fragment)
                }
                selectedIndex = index

                if (fragment is Navigator.Navigation.Callbacks) {
                    fragment.onSelected()
                }
            } else {
                transaction = when(navigation.fragmentViewRetentionType) {
                    Navigator.ViewRetention.RECREATE -> transaction.detach(fragment)
                    Navigator.ViewRetention.RETAIN -> transaction.hide(fragment)
                }
            }
        }
        transaction.commit()

        onNavigationSelectionChange(getSelectedNavFragmentId())
    }

    private fun setAnimations(ft: FragmentTransaction, fromIndex: Int, toIndex: Int) {
        when (navigation.fragmentNavigationTransition) {
            is Navigator.Navigation.Animation.None -> {}
            is Navigator.Navigation.Animation.Slide -> {
                if (fromIndex < toIndex)
                    ft.setCustomAnimations(R.anim.navigator_slide_in_right, R.anim.navigator_slide_out_left)
                if (fromIndex > toIndex)
                    ft.setCustomAnimations(R.anim.navigator_slide_in_left, R.anim.navigator_slide_out_right)
            }
            else -> ft.setCustomAnimations(navigation.fragmentNavigationTransition.enter, navigation.fragmentNavigationTransition.exit)
        }
    }

    private fun getSelectedNavFragmentId(): Int {
        return navFragments
            .filter { it.value.qualifiedName == selectedFragment.javaClass.name }
            .map { it.key }.first()
    }

    private fun getFragmentFromId(@IdRes id: Int): Fragment? {
        val tag = navFragments[id]!!.java.simpleName + FRAGMENT_SUFFIX
        return fm.findFragmentByTag(tag)
    }

    private fun getPrimarySelectionFragmentId(): Int = navFragments.keys.indexOf(navigation.selectedFragmentId)

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_SELECTION_INDEX, selectedIndex)
    }

    companion object {
        private const val FRAGMENT_SUFFIX = "_absBottomNav"
        private const val KEY_SELECTION_INDEX = "keySelectedIndex"
    }
}