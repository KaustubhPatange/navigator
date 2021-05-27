package com.kpstv.navigation.base.navigation

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.base.navigation.internals.*
import com.kpstv.navigator.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MultipleBackStackTests {

    @get:Rule
    val activityRule = ActivityScenarioRule(TestMainActivity::class.java)

    private lateinit var baseNavigation: CustomCommonNavigationImpl
    private lateinit var tabNavigation: CustomCommonNavigationImpl

    private val tabNav = Custom2Navigation(
        fragments = mapOf(
            0 to FirstFragment::class,
            1 to NavigatorFragment::class
        ),
        topSelectedId = 1
    )
    private val bottomNav = Custom3Navigation(
        fragments = mapOf(
            0 to NavigatorFragment::class,
            1 to FirstFragment::class,
            2 to NavigatorFragment::class
        )
    )

    @Before
    fun init() {
        activityRule.with {
            getNavigator().navigateTo(NavigatorFragment::class)
            getNavigator().getFragmentManager().executePendingTransactions()

            val navFragment = (getNavigator().getCurrentFragment() as NavigatorFragment)

            baseNavigation = CustomCommonNavigationImpl(navFragment.getNavigator(), bottomNav.fragments, bottomNav)
            baseNavigation.onCreate(null)
            navFragment.apply base@ {
                getParentNavigator().getFragmentManager().registerFragmentLifecycleCallbacks(
                    FragmentNavigationLifecycle(navFragment, baseNavigation) { f, b ->
                        // Verify bundle
                        assert(!b.isEmpty && b["key_index"] is Int)
                    }, false
                )
                getNavigator().getFragmentManager().executePendingTransactions()

                (getNavigator().getCurrentFragment() as NavigatorFragment).apply {
                    tabNavigation = CustomCommonNavigationImpl(getNavigator(), tabNav.fragments, tabNav)
                    tabNavigation.onCreate(null)
                    getParentNavigator().getFragmentManager().registerFragmentLifecycleCallbacks(
                        FragmentNavigationLifecycle(this, tabNavigation) { f, b ->
                            // Verify bundle
                            assert(!b.isEmpty && b["key_index"] is Int)
                        }, false
                    )
                    childFragmentManager.executePendingTransactions()

                    assert(tabNavigation.firstId == 1)
                    (getNavigator().getCurrentFragment() as NavigatorFragment).apply {
                        getNavigator().navigateTo(ForthFragment::class)
                        getNavigator().navigateTo(ThirdFragment::class, FragmentNavigator.NavOptions(remember = true))
                        childFragmentManager.executePendingTransactions()

                        assert(getNavigator().getHistory().count() == 1)
                    }
                }

                baseNavigation.onSelectNavItem(2)
                getNavigator().getFragmentManager().executePendingTransactions()

                assert(baseNavigation.selectedId == 2)

                (getNavigator().getCurrentFragment() as NavigatorFragment).apply {
                    getNavigator().navigateTo(ThirdFragment::class)
                    getNavigator().getFragmentManager().executePendingTransactions()
                }
            }
        }
    }

    @Test
    fun CompleteMultipleStackTest() {
        activityRule.with {
            // Should not go back
            assert(!getNavigator().canGoBack())

            (getNavigator().getCurrentFragment() as NavigatorFragment).apply {
                (getNavigator().getCurrentFragment() as NavigatorFragment).apply {
                    getNavigator().navigateTo(ForthFragment::class, FragmentNavigator.NavOptions(remember = true))
                    getNavigator().navigateTo(FirstFragment::class, FragmentNavigator.NavOptions(remember = true))
                    childFragmentManager.executePendingTransactions()
                }
            }

            // It should go back
            assert(getNavigator().canGoBack())

            baseNavigation.onSelectNavItem(0)
            getNavigator().getFragmentManager().executePendingTransactions()

            // Tab selection should be 1
            assert(tabNavigation.selectedId == 1)
        }
        activityRule.scenario.recreate()
        activityRule.with act@ {
            (getNavigator().getCurrentFragment() as NavigatorFragment).apply {
                // verify bottom nav index to be 0
                val bottomState = getNavigator().getSaveInstanceState()
                assert(bottomState != null && bottomState["key_index"] == 0)

                baseNavigation = CustomCommonNavigationImpl(getNavigator(), bottomNav.fragments, bottomNav)
                baseNavigation.onCreate(bottomState)

                (getNavigator().getCurrentFragment() as NavigatorFragment).apply {
                    // index should be 1
                    val tabState = getNavigator().getSaveInstanceState()
                    assert(tabState != null && tabState["key_index"] == 1)
                }

                baseNavigation.onSelectNavItem(2)
                getNavigator().getFragmentManager().executePendingTransactions()

                // Should go back
                assert(getNavigator().canGoBack())

                (getNavigator().getCurrentFragment() as NavigatorFragment).apply {
                    // Check history count
                    assert(getNavigator().getHistory().count() == 2)
                    // Must be first fragment
                    assert(getNavigator().getCurrentFragment() is FirstFragment)
                }

                this@act.onBackPressed()
                (getNavigator().getCurrentFragment() as NavigatorFragment).childFragmentManager.executePendingTransactions()

                // Should still go back
                assert(getNavigator().canGoBack())

                (getNavigator().getCurrentFragment() as NavigatorFragment).apply {
                    // Check history count
                    assert(getNavigator().getHistory().count() == 1)
                    // Must be first fragment
                    assert(getNavigator().getCurrentFragment() is ForthFragment)
                }

                this@act.onBackPressed()
                (getNavigator().getCurrentFragment() as NavigatorFragment).childFragmentManager.executePendingTransactions()

                // Should not go back
                assert(!getNavigator().canGoBack())

                (getNavigator().getCurrentFragment() as NavigatorFragment).apply {
                    // Verify the fragment
                    assert(getNavigator().getCurrentFragment() is ThirdFragment)
                }

                baseNavigation.onSelectNavItem(0)
                getNavigator().getFragmentManager().executePendingTransactions()

                // Should go back & history must be 1
                assert(getNavigator().canGoBack())

                this@act.onBackPressed()
                (getNavigator().getCurrentFragment() as NavigatorFragment).childFragmentManager.executePendingTransactions()

                // Should not go back
                assert(!getNavigator().canGoBack())
            }
        }
    }
}