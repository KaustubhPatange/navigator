package com.kpstv.navigation.base.navigation

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.ValueFragment
import com.kpstv.navigation.base.navigation.internals.*
import com.kpstv.navigation.internals.ViewStateFragment
import com.kpstv.navigator.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BaseNavigationTests {

    @get:Rule
    val activityRule = ActivityScenarioRule(TestMainActivity::class.java)

    @Before
    fun init() {
        activityRule.with {
            // Check owner
            assert(navigator.getOwner() is FragmentActivity)
        }
        activityRule.scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun CompleteNavigationWithStateSavingTest() {
        activityRule.with {
            val commonNavigationImpl = preSetup(this, CustomNavigation())

            commonNavigationImpl.onCreate(null)
            supportFragmentManager.executePendingTransactions()

            // Check if top selection id is 0 & fragment is First
            assert(commonNavigationImpl.firstId == 0)
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)

            // Check if selection is changed & fragment is Second
            commonNavigationImpl.onSelectNavItem(1)
            supportFragmentManager.executePendingTransactions()
            assert(commonNavigationImpl.selectedId == 1)
            assert(navigator.getCurrentFragment()!!::class == SecondFragment::class)

            val args = TestArgs.create()
            commonNavigationImpl.onSelectNavItem(2, args)
            supportFragmentManager.executePendingTransactions()

            // Check if args are the same.
            val fragArgs = (navigator.getCurrentFragment() as ThirdFragment).getKeyArgs<TestArgs>()
            assert(fragArgs == args)
        }
        activityRule.scenario.recreate()
        activityRule.with {
            val commonNavigationImpl = preSetup(this, CustomNavigation())
            val savedState = navigator.getSaveInstanceState()

            // Saved state must not be null
            assert(savedState != null)

            commonNavigationImpl.onCreate(savedState)
            supportFragmentManager.executePendingTransactions()

            // Check if top selection Id is 1 & fragment is Second
            assert(commonNavigationImpl.firstId == 2)
            val fragment = navigator.getCurrentFragment() as ValueFragment
            assert(fragment::class == ThirdFragment::class)

            // Check if the last arguments are stored
            assert(fragment.hasKeyArgs<TestArgs>())

            commonNavigationImpl.onSelectNavItem(0)
            commonNavigationImpl.onSelectNavItem(2)
            supportFragmentManager.executePendingTransactions()

            // Check if args are removed
            assert(!fragment.hasKeyArgs<TestArgs>())

            // Check view state
            assert((fragment as ThirdFragment).viewState == ViewStateFragment.ViewState.FOREGROUND)
        }
    }

    @Test
    fun RetainFragmentOnSelectionChangeTest() {
        activityRule.with {
            val commonNavigationImpl = preSetup(this, CustomNavigation(2, Navigator.Navigation.ViewRetention.RETAIN))

            commonNavigationImpl.onCreate(null)
            supportFragmentManager.executePendingTransactions()

            // Check if top selection is 2 & fragment is Third
            assert(commonNavigationImpl.firstId == 2)
            assert(navigator.getCurrentFragment()!!::class == ThirdFragment::class)

            // Check if there are 3 views in the container
            assert(navigator.getContainerView().childCount == 3)

            commonNavigationImpl.onSelectNavItem(0)
            supportFragmentManager.executePendingTransactions()

            // Check selection is 0 & fragment is First
            assert(commonNavigationImpl.selectedId == 0)
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)

            // Check view state changes of all three fragments
            assert((supportFragmentManager.fragments[0] as FirstFragment).viewState == ViewStateFragment.ViewState.FOREGROUND)
            assert((supportFragmentManager.fragments[1] as SecondFragment).viewState == ViewStateFragment.ViewState.BACKGROUND)
            assert((supportFragmentManager.fragments[2] as ThirdFragment).viewState == ViewStateFragment.ViewState.BACKGROUND)

            commonNavigationImpl.onSelectNavItem(1)
            supportFragmentManager.executePendingTransactions()

            // Verify view state again
            assert((supportFragmentManager.fragments[0] as FirstFragment).viewState == ViewStateFragment.ViewState.BACKGROUND)
            assert((supportFragmentManager.fragments[1] as SecondFragment).viewState == ViewStateFragment.ViewState.FOREGROUND)
            assert((supportFragmentManager.fragments[2] as ThirdFragment).viewState == ViewStateFragment.ViewState.BACKGROUND)
        }
    }

    private fun preSetup(activity: TestMainActivity, navigation: CustomNavigation): CustomCommonNavigationImpl{
        val commonNavigationImpl = CustomCommonNavigationImpl(activity.navigator, navigation.fragments, navigation)
        activity.application.registerActivityLifecycleCallbacks(ActivityNavigationLifecycle(activity, commonNavigationImpl))
        return commonNavigationImpl
    }
}