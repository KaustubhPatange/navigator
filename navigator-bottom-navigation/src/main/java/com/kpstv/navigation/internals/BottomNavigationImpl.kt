package com.kpstv.navigation.internals

import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commitNow
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.ValueFragment
import com.kpstv.navigation.bottom.R
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BottomNavigationImpl(
    private val fm: FragmentManager,
    private val containerView: FrameLayout,
    internal val navView: BottomNavigationView,
    private val navFragments: Map<Int, KClass<out Fragment>>,
    private val selectedNavId: Int,
    private val onNavSelectionChange: KFunction1<Int, Unit>,
    private val transition: Navigator.BottomNavigation.Animation,
) : CommonLifecycleCallbacks {

    private var fragments = arrayListOf<Fragment>()
    private var selectedIndex = if (selectedNavId != -1)
        getPrimarySelectionFragmentId()
    else 0
    private val selectedFragment get() = fragments[selectedIndex]

    private var topSelectionId = if (selectedNavId != -1)
        selectedNavId
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
            fm.commitNow {
                fragments.forEach { detach(it) }
            }
        } else {
            navFragments.values.forEach { frag ->
                val fragment = fm.findFragmentByTag(frag.simpleName + FRAGMENT_SUFFIX)!!
                fragments.add(fragment)
            }
            selectedIndex = savedInstanceState.getInt(KEY_SELECTION_INDEX, 0)
            topSelectionId = navFragments.keys.elementAt(selectedIndex)
        }

        navView.selectedItemId = topSelectionId
        navView.setOnNavigationItemSelectedListener(navigationListener)

        setFragment(selectedFragment)
    }

    private val navigationListener = BottomNavigationView.OnNavigationItemSelectedListener call@{ item ->
        return@call onSelectNavItem(item.itemId)
    }

    internal fun onSelectNavItem(id: Int, args: BaseArgs? = null) : Boolean {
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
            if (fragment is Navigator.BottomNavigation.Callbacks && fragment.isVisible) {
                fragment.onReselected()
            }
        } else {
            setFragment(fragment, true)
        }
        return true
    }

    private fun setFragment(whichFragment: Fragment, runIfHasAnimations: Boolean = false) {
        var transaction = fm.beginTransaction()
        val current = fm.findFragmentById(containerView.id)
        if (runIfHasAnimations && current != null) {
            setAnimations(transaction, fromIndex = fragments.indexOf(current), toIndex = fragments.indexOf(whichFragment))
        }
        fragments.forEachIndexed { index, fragment ->
            if (fragment == whichFragment) {
                transaction = transaction.attach(fragment)
                selectedIndex = index

                if (fragment is Navigator.BottomNavigation.Callbacks) {
                    fragment.onSelected()
                }
            } else {
                transaction = transaction.detach(fragment)
            }
        }
        transaction.commit()

        onNavSelectionChange.invoke(getSelectedNavFragmentId())
    }

    private fun setAnimations(ft: FragmentTransaction, fromIndex: Int, toIndex: Int) {
        when (transition) {
            is Navigator.BottomNavigation.Animation.None -> {}
            is Navigator.BottomNavigation.Animation.Slide -> {
                if (fromIndex < toIndex)
                    ft.setCustomAnimations(R.anim.navigator_slide_in_right, R.anim.navigator_slide_out_left)
                if (fromIndex > toIndex)
                    ft.setCustomAnimations(R.anim.navigator_slide_in_left, R.anim.navigator_slide_out_right)
            }
            else -> ft.setCustomAnimations(transition.enter, transition.exit)
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

    private fun getPrimarySelectionFragmentId(): Int = navFragments.keys.indexOf(selectedNavId)

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_SELECTION_INDEX, selectedIndex)
    }

    internal fun ignoreNavigationListeners(block: () -> Unit) {
        navView.setOnNavigationItemSelectedListener(null)
        block.invoke()
        navView.setOnNavigationItemSelectedListener(navigationListener)
    }

    companion object {
        private const val FRAGMENT_SUFFIX = "_absBottomNav"
        private const val KEY_SELECTION_INDEX = "keySelectedIndex"
    }
}