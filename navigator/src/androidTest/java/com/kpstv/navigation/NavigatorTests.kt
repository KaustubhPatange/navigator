package com.kpstv.navigation

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kpstv.navigation.base.*
import com.kpstv.navigation.internals.HistoryImpl
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigatorTests {

    private lateinit var activityScenario: ActivityScenario<TestMainActivity>
    private lateinit var navigator: Navigator

    @Before
    fun init() {
        activityScenario = ActivityScenario.launch(TestMainActivity::class.java)
        activityScenario.onActivity { activity ->
            navigator = activity.navigator
        }
        activityScenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun NavigateToFirstFragmentAndVerify() {
        activityScenario.onActivity { activity ->
            navigator.navigateTo(FirstFragment::class)
            activity.supportFragmentManager.executePendingTransactions()
            val fragment = navigator.getCurrentFragment()
            assert(fragment!!::class == FirstFragment::class)
        }
    }

    @Test
    fun SomeTestsForBackstack() {
        activityScenario.onActivity { activity ->
            navigator.navigateTo(FirstFragment::class)
            navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
            activity.supportFragmentManager.executePendingTransactions()

            // Check if it can go back.
            assert(navigator.canGoBack())

            // Check if the fragment is added to backstack.
            assert(activity.supportFragmentManager.backStackEntryCount > 0)

            // Check if the backstack has unique name.
            val name = "SecondFragment_navigator$0"
            assert(activity.supportFragmentManager.getBackStackEntryAt(0).name == name)

            // Check a new unique backstack name for SecondFragment is not same as the above.
            val new = (navigator.getHistory() as HistoryImpl).getUniqueBackStackName(SecondFragment::class)
            assert(name != new)

            // Go back & see if the current fragment is FirstFragment.
            navigator.goBack()
            activity.supportFragmentManager.executePendingTransactions()
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)

            // It must not back.
            assert(!navigator.canGoBack())
        }
    }

    private fun preSetupForHistoryTest(activity: TestMainActivity) {
        navigator.navigateTo(FirstFragment::class)
        navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
        navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
        navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
        navigator.navigateTo(ThirdFragment::class, Navigator.NavOptions(remember = true))
        activity.supportFragmentManager.executePendingTransactions()
    }
    @Test
    fun SomeRandomHistoryTests() {
        activityScenario.onActivity { activity ->
            preSetupForHistoryTest(activity)

            // Check the backstack count
            assert(activity.supportFragmentManager.backStackEntryCount == 4)

            // Get the last backstack name
            assert(navigator.getHistory().getBackStackName(SecondFragment::class) == "SecondFragment_navigator$2")

            // Get the top backstack name
            assert(navigator.getHistory().getTopBackStackName(SecondFragment::class) == "SecondFragment_navigator$0")

            // Get all backstack name
            var list = navigator.getHistory().getAllBackStackName(SecondFragment::class)
            assert(list.count() == 3)

            // Clear up to SecondFragment all & inclusive
            navigator.getHistory().clearUpTo(SecondFragment::class, all = true)
            activity.supportFragmentManager.executePendingTransactions()
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)

            preSetupForHistoryTest(activity)

            // Clear up to SecondFragment inclusive without all
            navigator.getHistory().clearUpTo(SecondFragment::class)
            activity.supportFragmentManager.executePendingTransactions()
            list = navigator.getHistory().getAllBackStackName(SecondFragment::class)
            assert(list.count() == 2)
            assert(list.last() == "SecondFragment_navigator$1")

            // Clear up to SecondFragment not inclusive & all
            navigator.getHistory().clearUpTo(SecondFragment::class, inclusive = false, all = true)
            activity.supportFragmentManager.executePendingTransactions()
            list = navigator.getHistory().getAllBackStackName(SecondFragment::class)
            assert(list.count() == 1)
            assert(list[0] == "SecondFragment_navigator$0")

            // Check if pop works
            navigator.getHistory().pop()
            activity.supportFragmentManager.executePendingTransactions()
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)

            // Cannot go back
            assert(!navigator.canGoBack())

            preSetupForHistoryTest(activity)

            // Check clear history
            navigator.getHistory().clearAll()
            activity.supportFragmentManager.executePendingTransactions()
            assert(!navigator.canGoBack())
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)

            // Check if back press works
            navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(remember = true))
            activity.supportFragmentManager.executePendingTransactions()
            activity.onBackPressed()
            activity.supportFragmentManager.executePendingTransactions()

            assert(!navigator.canGoBack())
            assert(navigator.getCurrentFragment()!!::class == FirstFragment::class)
        }
    }

    @Test
    fun TypedArgumentTests() {
        activityScenario.onActivity { activity ->
            navigator.navigateTo(FirstFragment::class)

            val args = TestArgs.create()
            navigator.navigateTo(SecondFragment::class, Navigator.NavOptions(args = args))

            activity.supportFragmentManager.executePendingTransactions()
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
        activityScenario.onActivity { activity ->
            preSetupForHistoryTest(activity)
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
        }
    }

    @Test
    fun TestSaveStateInstance() {
        val currentBundle = Bundle()
        activityScenario.onActivity { activity ->
            preSetupForHistoryTest(activity)
            navigator.onSaveInstance(currentBundle)
        }
        activityScenario.recreate()
        activityScenario.onActivity { activity ->
            // The bundle should not be null
            assert(activity.navigator.savedInstanceState != null)

            // Check if bundle contents are same
            val const = "com.kpstv.navigation:navigator:history_saved_state"
            assert(currentBundle[const] == activity.navigator.savedInstanceState!![const])

            activity.supportFragmentManager.executePendingTransactions()

            // Check current fragment is ThirdFragment.
            assert(activity.navigator.getCurrentFragment()!!::class == ThirdFragment::class)
        }
    }
}