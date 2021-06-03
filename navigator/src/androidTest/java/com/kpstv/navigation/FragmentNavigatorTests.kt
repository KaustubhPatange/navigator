package com.kpstv.navigation

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kpstv.navigation.internals.HistoryImpl
import com.kpstv.navigator.test.*
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class FragmentNavigatorTests {

    @get:Rule
    val activity = ActivityScenarioRule(TestMainActivity::class.java)

    @Before
    fun init() {
        activity.scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun NavigateToFirstFragmentAndVerify() {
        activity.with {
            getNavigator().navigateTo(FirstFragment::class)
            getNavigator().getFragmentManager().executePendingTransactions()
            val fragment = getNavigator().getCurrentFragment()
            assert(fragment!!::class == FirstFragment::class)
        }
    }

    @Test
    fun SomeTestsForBackstack() {
        activity.with {
            getNavigator().navigateTo(FirstFragment::class)
            getNavigator().navigateTo(SecondFragment::class, FragmentNavigator.NavOptions(remember = true))
            getNavigator().getFragmentManager().executePendingTransactions()

            // Check if it can go back.
            assert(getNavigator().canGoBack())

            // Check if the fragment is added to backstack.
            assert(getNavigator().getFragmentManager().backStackEntryCount > 0)

            // Check if the backstack has unique name.
            val name = "SecondFragment_navigator$0"
            assert(getNavigator().getFragmentManager().getBackStackEntryAt(0).name == name)

            // Check a new unique backstack name for SecondFragment is not same as the above.
            val new = (getNavigator().getHistory() as HistoryImpl).getUniqueBackStackName(SecondFragment::class)
            assert(name != new)

            // Go back & see if the current fragment is FirstFragment.
            getNavigator().goBack()
            getNavigator().getFragmentManager().executePendingTransactions()
            assert(getNavigator().getCurrentFragment()!!::class == FirstFragment::class)

            // It must not back.
            assert(!getNavigator().canGoBack())
        }
    }

    private fun preSetupForHistoryTest(activity: TestMainActivity) = with(activity) {
        getNavigator().navigateTo(FirstFragment::class)
        getNavigator().navigateTo(SecondFragment::class, FragmentNavigator.NavOptions(remember = true))
        getNavigator().navigateTo(SecondFragment::class, FragmentNavigator.NavOptions(remember = true))
        getNavigator().navigateTo(SecondFragment::class, FragmentNavigator.NavOptions(remember = true))
        getNavigator().navigateTo(ThirdFragment::class, FragmentNavigator.NavOptions(remember = true))
        getNavigator().getFragmentManager().executePendingTransactions()
    }
    @Test
    fun SomeRandomHistoryTests() {
        activity.with {
            preSetupForHistoryTest(this)

            // Check the backstack count
            assert(getNavigator().getFragmentManager().backStackEntryCount == 4)

            // Get the last backstack name
            assert(getNavigator().getHistory().getBackStackName(SecondFragment::class) == "SecondFragment_navigator$2")

            // Get the top backstack name
            assert(getNavigator().getHistory().getTopBackStackName(SecondFragment::class) == "SecondFragment_navigator$0")

            // Get all backstack name
            var list = getNavigator().getHistory().getAllBackStackName(SecondFragment::class)
            assert(list.count() == 3)

            // Clear up to SecondFragment all & inclusive
            getNavigator().getHistory().clearUpTo(SecondFragment::class, all = true)
            getNavigator().getFragmentManager().executePendingTransactions()
            assert(getNavigator().getCurrentFragment()!!::class == FirstFragment::class)

            preSetupForHistoryTest(this)

            // Clear up to SecondFragment inclusive without all
            getNavigator().getHistory().clearUpTo(SecondFragment::class)
            getNavigator().getFragmentManager().executePendingTransactions()
            list = getNavigator().getHistory().getAllBackStackName(SecondFragment::class)
            assert(list.count() == 2)
            assert(list.last() == "SecondFragment_navigator$1")

            // Clear up to SecondFragment not inclusive & all
            getNavigator().getHistory().clearUpTo(SecondFragment::class, inclusive = false, all = true)
            getNavigator().getFragmentManager().executePendingTransactions()
            list = getNavigator().getHistory().getAllBackStackName(SecondFragment::class)
            assert(list.count() == 1)
            assert(list[0] == "SecondFragment_navigator$0")

            // Check if pop works
            getNavigator().getHistory().pop()
            getNavigator().getFragmentManager().executePendingTransactions()
            assert(getNavigator().getCurrentFragment()!!::class == FirstFragment::class)

            // Cannot go back
            assert(!getNavigator().canGoBack())

            preSetupForHistoryTest(this)

            // Check clear history
            getNavigator().getHistory().clearAll()
            getNavigator().getFragmentManager().executePendingTransactions()
            assert(!getNavigator().canGoBack())
            assert(getNavigator().getCurrentFragment()!!::class == FirstFragment::class)

            // Check if back press works
            getNavigator().navigateTo(SecondFragment::class, FragmentNavigator.NavOptions(remember = true))
            getNavigator().getFragmentManager().executePendingTransactions()
            onBackPressed()
            getNavigator().getFragmentManager().executePendingTransactions()

            assert(!getNavigator().canGoBack())
            assert(getNavigator().getCurrentFragment()!!::class == FirstFragment::class)
        }
    }

    @Test
    fun TypedArgumentTests() {
        activity.with {
            getNavigator().navigateTo(FirstFragment::class)

            val args = TestArgs.create()
            getNavigator().navigateTo(SecondFragment::class, FragmentNavigator.NavOptions(args = args))

            getNavigator().getFragmentManager().executePendingTransactions()
            val currentFragment = getNavigator().getCurrentFragment() as ValueFragment

            // Check if key args are present.
            assert(currentFragment.hasKeyArgs<TestArgs>())

            // Check if both arguments has a value equality.
            val fragArgs = currentFragment.getKeyArgs<TestArgs>()
            assert(args == fragArgs)
        }
    }

    @Test
    fun TestDialogFragments() {
        activity.with {
            preSetupForHistoryTest(this)
            val currentFragment = getNavigator().getCurrentFragment() as ValueFragment

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
            dismissed = dismissed and currentFragment.getSimpleNavigator().dismiss()
            dismissed = dismissed and currentFragment.getSimpleNavigator().dismiss()
            dismissed = dismissed and currentFragment.getSimpleNavigator().dismiss()
            dismissed = dismissed and currentFragment.getSimpleNavigator().dismiss()
            currentFragment.childFragmentManager.executePendingTransactions()

            assert(dismissed)

            // Test dialog listener
            var called = false
            currentFragment.getSimpleNavigator().show(SecondSheet::class) {
                called = true
            }
            currentFragment.childFragmentManager.executePendingTransactions()
            currentFragment.getSimpleNavigator().dismiss()
            currentFragment.childFragmentManager.executePendingTransactions()

            assert(called)
        }
    }

    @Test
    fun TestSaveStateInstance() {
        activity.with {
            preSetupForHistoryTest(this)
            getNavigator().show(FirstSheet::class)
            getNavigator().show(SecondSheet::class)

            getNavigator().getFragmentManager().executePendingTransactions()

            val currentBundle = Bundle()
            getNavigator().onSaveInstance(currentBundle)

            assert(!currentBundle.isEmpty)
        }
        activity.scenario.recreate()
        activity.with {
            supportFragmentManager.executePendingTransactions()

            // The bundle should not be null
            assert(getNavigator().savedInstanceState != null)

            // Check if navigator is successfully restored
            assert(!getNavigator().getHistory().isEmpty())

            // Contents must be 4
            assert(getNavigator().getHistory().count() == 4)

            // Check if sheets are showing
            assert(getNavigator().getCurrentFragment().matchClass(SecondSheet::class))
            onBackPressed()
            getNavigator().getFragmentManager().executePendingTransactions()
            assert(getNavigator().getCurrentFragment().matchClass(FirstSheet::class))
            getNavigator().getFragmentManager().executePendingTransactions()

            onBackPressed()
            getNavigator().getFragmentManager().executePendingTransactions()

            // Check current fragment is Third.
            assert(getNavigator().getCurrentFragment().matchClass(ThirdFragment::class))
        }
    }

    @Test
    fun HistoryOptionsTests() {
        activity.with {
            preSetupForHistoryTest(this)

            // Check for single instance
            getNavigator().navigateTo(SecondFragment::class, FragmentNavigator.NavOptions(historyOptions = HistoryOptions.SingleTopInstance))
            getNavigator().getFragmentManager().executePendingTransactions()
            assert(getNavigator().getHistory().getAllBackStackName(SecondFragment::class).count() == 1)

            getNavigator().navigateTo(SecondFragment::class)
            getNavigator().navigateTo(ThirdFragment::class)

            // Clear history & navigate
            getNavigator().navigateTo(FirstFragment::class, FragmentNavigator.NavOptions(historyOptions = HistoryOptions.ClearHistory))
            getNavigator().getFragmentManager().executePendingTransactions()
            assert(!getNavigator().canGoBack())

            // Pop to fragment
            preSetupForHistoryTest(this)
            getNavigator().navigateTo(ThirdFragment::class, FragmentNavigator.NavOptions(remember = true, historyOptions = HistoryOptions.PopToFragment(SecondFragment::class, all = true)))
            getNavigator().getFragmentManager().executePendingTransactions()
            assert(getNavigator().getFragmentManager().backStackEntryCount == 1)
        }
    }

    @Test
    fun AddTransactionTest() {
        activity.with {
            getNavigator().navigateTo(FirstFragment::class)
            getNavigator().navigateTo(SecondFragment::class, FragmentNavigator.NavOptions(remember = true, transaction = FragmentNavigator.TransactionType.ADD))
            getNavigator().navigateTo(ThirdFragment::class, FragmentNavigator.NavOptions(remember = true, transaction = FragmentNavigator.TransactionType.ADD))
            getNavigator().getFragmentManager().executePendingTransactions()

            // Check if container has 3 views
            assert(getNavigator().getContainerView().childCount == 3)
        }
    }

    @Test
    fun TestIfInitialDestinationsAreWorking() {
        activity.with {
            val initials = Destination.of(listOf(FirstFragment::class, SecondFragment::class, ThirdFragment::class))
            val navigator = Navigator.with(this, null)
                .setNavigator(FragmentNavigator::class)
                .initialize(findViewById(com.kpstv.navigation.test.R.id.my_container), initials)

            // Check current fragment
            assert(navigator.getCurrentFragment() is ThirdFragment)

            // Check history
            assert(!navigator.getHistory().isEmpty())
            assert(navigator.getHistory().count() == 2)
        }
    }
}