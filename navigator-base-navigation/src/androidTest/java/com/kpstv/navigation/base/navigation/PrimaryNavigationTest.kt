package com.kpstv.navigation.base.navigation

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.base.navigation.internals.TestNavigationFragment
import com.kpstv.navigator.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.kpstv.navigator.base.navigation.R

@LargeTest
@RunWith(AndroidJUnit4::class)
class PrimaryNavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(TestMainActivity::class.java)

    private val doNotAddToBackstackOptions = FragmentNavigator.NavOptions(remember = false)

    @Before
    fun init() {
        activityRule.with {
            getNavigator().navigateTo(TestNavigationFragment::class, doNotAddToBackstackOptions)
            getNavigator().getFragmentManager().executePendingTransactions()
        }
        activityRule.scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun TestIfBackPressNavigatesToPrimaryNavigationProperly() {
        activityRule.with {
            (getNavigator().get<TestNavigationFragment>()).apply {
                assert(baseNavigation.firstId == R.id.navigator_fragment_1)

                (getNavigator().get<NavigatorFragment>()).apply {
                    getNavigator().navigateTo(ThirdFragment::class, doNotAddToBackstackOptions)
                    getNavigator().navigateTo(ForthFragment::class, FragmentNavigator.NavOptions(remember = true))
                    getNavigator().getFragmentManager().executePendingTransactions()

                    // Verify history
                    assert(getNavigator().getHistory().count() == 1)
                }

                baseNavigation.onSelectNavItem(R.id.navigator_fragment_2)
                getNavigator().getFragmentManager().executePendingTransactions()

                (getNavigator().get<NavigatorFragment>()).apply {
                    getNavigator().navigateTo(FirstFragment::class, doNotAddToBackstackOptions)
                    getNavigator().navigateTo(SecondFragment::class, FragmentNavigator.NavOptions(remember = true))
                    getNavigator().getFragmentManager().executePendingTransactions()

                    // Verify history
                    assert(getNavigator().getHistory().count() == 1)
                }

                // Selection must be 2
                assert(baseNavigation.selectedId == R.id.navigator_fragment_2)
            }
        }
        activityRule.scenario.recreate()
        activityRule.with act@{
            (getNavigator().get<TestNavigationFragment>()).apply {
                getNavigator().getFragmentManager().executePendingTransactions()

                // Selection after configuration change must be 2
                assert(baseNavigation.selectedId == R.id.navigator_fragment_2)
            }

            // Should go back
            assert(getNavigator().canGoBack())

            this.onBackPressed()
            getNavigator().getFragmentManager().executePendingTransactions()

            // Verify if fragment exist in current chain
            assert(getNavigator().verifyRecursive(FirstFragment::class))

            // Should go back (must be invoked from forceBackPressed)
            assert(getNavigator().canGoBack())
            assert(getNavigator().get<TestNavigationFragment>().forceBackPress)

            this.onBackPressed()
            getNavigator().get<TestNavigationFragment>().executeTransactionsRecursive()

            // Index must be back to 0
            assert(getNavigator().get<TestNavigationFragment>().baseNavigation.selectedId == R.id.navigator_fragment_1)

            // Verify if fragment exist in current chain
            assert(getNavigator().verifyRecursive(ForthFragment::class))

            // Should still go back (because of the current child fragments)
            assert(getNavigator().canGoBack())

            this.onBackPressed()
            getNavigator().getFragmentManager().executePendingTransactions()

            // Verify if fragment exist in current chain
            assert(getNavigator().verifyRecursive(NavigatorFragment::class))
            assert(getNavigator().verifyRecursive(ThirdFragment::class))

            // Should not go back
            assert(!getNavigator().canGoBack())
        }
    }
}