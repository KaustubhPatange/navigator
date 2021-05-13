package com.kpstv.navigation

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kpstv.navigator.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BasicMultiStackTests {
    @get:Rule
    val activityRule = ActivityScenarioRule(TestMainActivity::class.java)

    @Before
    fun init() {
        activityRule.scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun SimpleCompleteMultiStackTest() {
        activityRule.with main@{
            navigator.navigateTo(FirstFragment::class)
            navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
            navigator.navigateTo(NavigatorFragment::class, Navigator.NavOptions(remember = true))
            navigator.getFragmentManager().executePendingTransactions()

            (navigator.getCurrentFragment() as NavigatorFragment).apply first@{
                getNavigator().navigateTo(FirstFragment::class)
                getNavigator().navigateTo(ThirdFragment::class, Navigator.NavOptions(remember = true))
                getNavigator().navigateTo(NavigatorFragment::class, Navigator.NavOptions(remember = true))
                getNavigator().getFragmentManager().executePendingTransactions()

                (getNavigator().getCurrentFragment() as NavigatorFragment).apply second@{
                    getNavigator().navigateTo(ThirdFragment::class)
                    getNavigator().navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
                    getNavigator().getFragmentManager().executePendingTransactions()

                    getNavigator().show(FirstSheet::class)
                    getNavigator().getFragmentManager().executePendingTransactions()

                    // Check if sheet is showing
                    assert(getNavigator().getCurrentFragment().matchClass(FirstSheet::class))
                }
            }
        }
        // Simulate configuration change or process death
        activityRule.scenario.recreate()
        activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        activityRule.scenario.with main@{
            // Check for the first Navigator fragment.
            assert(navigator.getCurrentFragment().matchClass(NavigatorFragment::class))

            (navigator.getCurrentFragment() as NavigatorFragment).apply first@{
                (getNavigator().getCurrentFragment() as NavigatorFragment).apply second@{
                    // Check if first sheet is showing
                    assert(getNavigator().getCurrentFragment().matchClass(FirstSheet::class))

                    this@main.onBackPressed()
                    getNavigator().getFragmentManager().executePendingTransactions()

                    // Check if fragment is second
                    assert(getNavigator().getCurrentFragment().matchClass(SecondFragment::class))

                    this@main.onBackPressed()
                    getNavigator().getFragmentManager().executePendingTransactions()

                    // Check if fragment is third & is the last fragment
                    assert(getNavigator().getCurrentFragment().matchClass(ThirdFragment::class))
                    assert(getNavigator().getHistory().isEmpty())
                }
                this@main.onBackPressed()
                getNavigator().getFragmentManager().executePendingTransactions()

                // Must be third fragment
                assert(getNavigator().getCurrentFragment().matchClass(ThirdFragment::class))

                this@main.onBackPressed()
                getNavigator().getFragmentManager().executePendingTransactions()

                // Check if fragment is first & is the last fragment
                assert(getNavigator().getCurrentFragment().matchClass(FirstFragment::class))
                assert(getNavigator().getHistory().isEmpty())
            }
            onBackPressed()
            navigator.getFragmentManager().executePendingTransactions()

            // Check if fragment is Second
            assert(navigator.getCurrentFragment().matchClass(SecondFragment::class))

            onBackPressed()
            navigator.getFragmentManager().executePendingTransactions()

            // Check if fragment is first & is the last fragment
            assert(navigator.getCurrentFragment().matchClass(FirstFragment::class))
            assert(navigator.getHistory().isEmpty())
        }
    }
}