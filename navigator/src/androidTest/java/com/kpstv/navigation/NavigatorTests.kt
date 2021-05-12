package com.kpstv.navigation

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kpstv.navigation.internals.HistoryImpl
import com.kpstv.navigator.test.*
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigatorTests {


    @get:Rule
    val activity = ActivityScenarioRule(TestMainActivity::class.java)

    @Before
    fun init() {
        activity.scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun NavigateToFirstFragmentAndVerify() {
        activity.with {
            navigator.navigateTo(FirstFragment::class)
            navigator.getFragmentManager().executePendingTransactions()
            val fragment = navigator.getCurrentFragment()
            assert(fragment!!::class == FirstFragment::class)
        }
    }

    @Test
    fun SomeTestsForBackstack() {
        activity.with {
            navigator.navigateTo(FirstFragment::class)
            navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
            navigator.getFragmentManager().executePendingTransactions()

            // Check if it can go back.
            assert(navigator.canGoBack())

            // Check if the fragment is added to backstack.
            assert(navigator.getFragmentManager().backStackEntryCount > 0)

            // Check if the backstack has unique name.
            val name = "SecondFragment_navigator$0"
            assert(navigator.getFragmentManager().getBackStackEntryAt(0).name == name)

            // Check a new unique backstack name for SecondFragment is not same as the above.
            val new = (navigator.getHistory() as HistoryImpl).getUniqueBackStackName(SecondFragment::class)
            assert(name != new)

            // Go back & see if the current fragment is FirstFragment.
            navigator.goBack()
            navigator.getFragmentManager().executePendingTransactions()
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)

            // It must not back.
            assert(!navigator.canGoBack())
        }
    }

    private fun preSetupForHistoryTest(activity: TestMainActivity) = with(activity) {
        navigator.navigateTo(FirstFragment::class)
        navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
        navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
        navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
        navigator.navigateTo(ThirdFragment::class, Navigator.NavOptions(remember = true))
        navigator.getFragmentManager().executePendingTransactions()
    }
    @Test
    fun SomeRandomHistoryTests() {
        activity.with {
            preSetupForHistoryTest(this)

            // Check the backstack count
            assert(navigator.getFragmentManager().backStackEntryCount == 4)

            // Get the last backstack name
            assert(navigator.getHistory().getBackStackName(SecondFragment::class) == "SecondFragment_navigator$2")

            // Get the top backstack name
            assert(navigator.getHistory().getTopBackStackName(SecondFragment::class) == "SecondFragment_navigator$0")

            // Get all backstack name
            var list = navigator.getHistory().getAllBackStackName(SecondFragment::class)
            assert(list.count() == 3)

            // Clear up to SecondFragment all & inclusive
            navigator.getHistory().clearUpTo(SecondFragment::class, all = true)
            navigator.getFragmentManager().executePendingTransactions()
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)

            preSetupForHistoryTest(this)

            // Clear up to SecondFragment inclusive without all
            navigator.getHistory().clearUpTo(SecondFragment::class)
            navigator.getFragmentManager().executePendingTransactions()
            list = navigator.getHistory().getAllBackStackName(SecondFragment::class)
            assert(list.count() == 2)
            assert(list.last() == "SecondFragment_navigator$1")

            // Clear up to SecondFragment not inclusive & all
            navigator.getHistory().clearUpTo(SecondFragment::class, inclusive = false, all = true)
            navigator.getFragmentManager().executePendingTransactions()
            list = navigator.getHistory().getAllBackStackName(SecondFragment::class)
            assert(list.count() == 1)
            assert(list[0] == "SecondFragment_navigator$0")

            // Check if pop works
            navigator.getHistory().pop()
            navigator.getFragmentManager().executePendingTransactions()
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)

            // Cannot go back
            assert(!navigator.canGoBack())

            preSetupForHistoryTest(this)

            // Check clear history
            navigator.getHistory().clearAll()
            navigator.getFragmentManager().executePendingTransactions()
            assert(!navigator.canGoBack())
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)

            // Check if back press works
            navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
            navigator.getFragmentManager().executePendingTransactions()
            onBackPressed()
            navigator.getFragmentManager().executePendingTransactions()

            assert(!navigator.canGoBack())
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)
        }
    }

    @Test
    fun TypedArgumentTests() {
        activity.with {
            navigator.navigateTo(FirstFragment::class)

            val args = TestArgs.create()
            navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(args = args))

            navigator.getFragmentManager().executePendingTransactions()
            val currentFragment = navigator.getCurrentFragment() as ValueFragment

            // Check if key args are present.
            assert(currentFragment.hasKeyArgs())

            // Check if both arguments has a value equality.
            val fragArgs = currentFragment.getKeyArgs<TestArgs>()
            assert(args == fragArgs)
        }
    }

    @Test
    fun TestDialogFragments() {
        activity.with {
            preSetupForHistoryTest(this)
            val currentFragment = navigator.getCurrentFragment() as ValueFragment

            val args = TestArgs.create()
            currentFragment.getSimpleNavigator().show(FirstSheet::class, args)
            currentFragment.childFragmentManager.executePendingTransactions()

            // Check if dialog is showing.
            val sheetFragment = currentFragment.childFragmentManager.findFragmentByTag("FirstSheet_navigator$0") as DialogFragment
            assert(sheetFragment::class == FirstSheet::class)

            // Check for arguments equality.
            val sheetArgs = sheetFragment.getKeyArgs<TestArgs>()
            assert(args == sheetArgs)

            // Show multiple instance of this fragment.
            currentFragment.getSimpleNavigator().show(FirstSheet::class)
            currentFragment.getSimpleNavigator().show(FirstSheet::class)
            currentFragment.getSimpleNavigator().show(FirstSheet::class)
            currentFragment.childFragmentManager.executePendingTransactions()

            // Now dismiss 4 times
            var dismissed = true
            dismissed = dismissed and currentFragment.getSimpleNavigator().pop()
            dismissed = dismissed and currentFragment.getSimpleNavigator().pop()
            dismissed = dismissed and currentFragment.getSimpleNavigator().pop()
            dismissed = dismissed and currentFragment.getSimpleNavigator().pop()
            currentFragment.childFragmentManager.executePendingTransactions()

            assert(dismissed)

            // Test dialog listener
            var called = false
            currentFragment.getSimpleNavigator().show(SecondSheet::class) {
                called = true
            }
            currentFragment.childFragmentManager.executePendingTransactions()
            currentFragment.getSimpleNavigator().pop()
            currentFragment.childFragmentManager.executePendingTransactions()

            assert(called)
        }
    }

    @Test
    fun TestSaveStateInstance() {
        val currentBundle = Bundle()
        activity.with {
            preSetupForHistoryTest(this)
            navigator.show(FirstSheet::class)
            navigator.show(SecondSheet::class)

            navigator.onSaveInstance(currentBundle)
        }
        activity.scenario.recreate()
        activity.with {
            // The bundle should not be null
            assert(navigator.savedInstanceState != null)

            // Check if bundle contents are same
            assert(currentBundle[HistoryImpl.SAVED_STATE] == navigator.savedInstanceState!![HistoryImpl.SAVED_STATE])

            navigator.getFragmentManager().executePendingTransactions()

            // Check current fragment is ThirdFragment.
            assert(navigator.getCurrentFragment()!!::class == ThirdFragment::class)
        }
    }

    @Test
    fun HistoryOptionsTests() {
        activity.with {
            preSetupForHistoryTest(this)

            // Check for single instance
            navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(historyOptions = HistoryOptions.SingleTopInstance))
            navigator.getFragmentManager().executePendingTransactions()
            assert(navigator.getHistory().getAllBackStackName(SecondFragment::class).count() == 1)

            navigator.navigateTo(SecondFragment::class)
            navigator.navigateTo(ThirdFragment::class)

            // Clear history & navigate
            navigator.navigateTo(FirstFragment::class, Navigator.NavOptions(historyOptions = HistoryOptions.ClearHistory))
            navigator.getFragmentManager().executePendingTransactions()
            assert(!navigator.canGoBack())

            // Pop to fragment
            preSetupForHistoryTest(this)
            navigator.navigateTo(ThirdFragment::class, Navigator.NavOptions(remember = true, historyOptions = HistoryOptions.PopToFragment(SecondFragment::class, all = true)))
            navigator.getFragmentManager().executePendingTransactions()
            assert(navigator.getFragmentManager().backStackEntryCount == 1)
        }
    }

    @Test
    fun AddTransactionTest() {
        activity.with {
            navigator.navigateTo(FirstFragment::class)
            navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true, transaction = Navigator.TransactionType.ADD))
            navigator.navigateTo(ThirdFragment::class, Navigator.NavOptions(remember = true, transaction = Navigator.TransactionType.ADD))
            navigator.getFragmentManager().executePendingTransactions()

            // Check if container has 3 views
            assert(navigator.getContainerView().childCount == 3)
        }
    }
}