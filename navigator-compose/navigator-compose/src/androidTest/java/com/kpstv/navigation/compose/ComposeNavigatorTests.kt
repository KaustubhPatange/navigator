package com.kpstv.navigation.compose

import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kpstv.navigation.compose.internels.*
import com.kpstv.navigation.compose.test.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class ComposeNavigatorTests {
    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> = createAndroidComposeRule()

    @Test
    public fun NavigateToSecondScreenTests() {
        val app_name = composeTestRule.activity.getString(R.string.app_name)
        val go_to_second = composeTestRule.activity.getString(R.string.go_to_second)
        val second_screen = composeTestRule.activity.getString(R.string.second_screen)

        composeTestRule.activity.apply {
            // Check if we are on FirstScreen
            composeTestRule.onNodeWithText(app_name).assertIsDisplayed()
            composeTestRule.onNodeWithText(go_to_second).performClick()

            composeTestRule.onNodeWithText(second_screen).assertIsDisplayed()

            val history = navigator.backStackMap[StartRoute.key]

            assert(history != null)
            assert(history!!.get().size == 2) // 2 because we restrict last screen to be not popped on back press.
        }
        // Simulate process death
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(second_screen).assertIsDisplayed()

            onBackPressed()

            composeTestRule.onNodeWithText(second_screen).assertDoesNotExist()
            composeTestRule.onNodeWithText(app_name).assertIsDisplayed()

            composeTestRule.onNodeWithText(go_to_second).performClick()

            composeTestRule.onNodeWithText(second_screen).assertIsDisplayed()

            composeTestRule.onNodeWithTag("icon_button").performClick() // this will call controller.goBack()

            val history = navigator.backStackMap[StartRoute.key]
            assert(history != null && history.get().size == 1)
        }
    }

    @ExperimentalTestApi
    @Test
    public fun SaveableStateHolderTestWithLazyColumn() {
        // this also tests nested navigation as well
        val go_to_third = composeTestRule.activity.getString(R.string.go_to_third)
        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(go_to_third).performClick()

            val lazyColumn = composeTestRule.onNodeWithTag("lazy_column")
            lazyColumn.assertIsDisplayed()

            lazyColumn.performScrollToIndex(3)

            // The first node of child is 3rd index of the list.
            lazyColumn.onChildAt(0).assertTextContains(galleryItems[3].name)

            lazyColumn.onChildAt(0).performClick()
        }

        // Simulate process death
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.activity.apply {
            val item = galleryItems[3]

            composeTestRule.onNodeWithText(getString(R.string.detail_screen, item.name, item.age))

            onBackPressed()

            val lazyColumn = composeTestRule.onNodeWithTag("lazy_column")
            lazyColumn.assertIsDisplayed()

            // Check if rememberSaveable is restoring state properly.
            lazyColumn.onChildAt(0).assertTextContains(item.name)

            assert(navigator.backStackMap.size == 2)

            onBackPressed()

            assert(navigator.backStackMap.size == 1)
        }
    }

    @Test
    public fun PopUpToWithoutInclusiveTest() {
        val go_to_second = composeTestRule.activity.getString(R.string.go_to_second)
        val go_to_third = composeTestRule.activity.getString(R.string.go_to_third)
        composeTestRule.activity.apply {
            val history = navigator.backStackMap[StartRoute.key]

            composeTestRule.onNodeWithText(go_to_second).performClick()

            assert(history!!.get().map { it.key::class } == listOf(
                StartRoute.First::class, StartRoute.Second::class
            ))

            composeTestRule.onNodeWithText(go_to_third).performClick()

            assert(history.get().map { it.key::class } == listOf(
                StartRoute.First::class, StartRoute.Third::class
            ))
        }
    }

    @Test
    public fun MultipleStackTest() {
        val go_to_second_bottom = composeTestRule.activity.getString(R.string.go_to_second_bottom)
        val go_to_forth = composeTestRule.activity.getString(R.string.go_to_forth)

        val bottomScreen1 = MultipleStack.First::class.qualifiedName!!

        val bottomButton1 = MultipleStack.First::class.simpleName!!
        val bottomButton2 = MultipleStack.Second::class.simpleName!!
        val bottomButton3 = MultipleStack.Third::class.simpleName!!

        val nextFirst = NextRoute.First::class.qualifiedName!!
        val nextSecond = NextRoute.Second::class.qualifiedName!!

        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(go_to_forth).performClick()

            // go to third bottom screen
            composeTestRule.onNodeWithText(bottomButton3).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(nextFirst).assertIsDisplayed()
            assert(navigator.backStackMap.size == 3)

            composeTestRule.onNodeWithText(go_to_second_bottom).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(nextSecond).assertIsDisplayed()

            // go to first bottom screen
            composeTestRule.onNodeWithText(bottomButton1).performClick()

            assert(navigator.backStackMap.keys.last() == MultipleStack.key)

            onBackPressed()

            composeTestRule.onNodeWithText(nextSecond).assertIsDisplayed()
            assert(navigator.backStackMap.keys.last() == NextRoute.key)

            onBackPressed()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(nextFirst).assertIsDisplayed()

            onBackPressed()

            assert(navigator.backStackMap.size == 2)
            composeTestRule.onNodeWithText(bottomScreen1).assertIsDisplayed()

            // go to third screen
            composeTestRule.onNodeWithText(bottomButton3).performClick()
            composeTestRule.onNodeWithText(bottomButton2).performClick()
            composeTestRule.onNodeWithText(bottomButton3).performClick()
            composeTestRule.onNodeWithText(go_to_second_bottom).performClick()
            composeTestRule.onNodeWithText(bottomButton1).performClick()
            composeTestRule.waitForIdle()

            assert(navigator.backStackMap.keys.last() == MultipleStack.key)
        }
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.activity.apply {
            assert(navigator.backStackMap.keys.last() == MultipleStack.key)

            onBackPressed()
            composeTestRule.waitForIdle()

            assert(navigator.backStackMap.keys.last() == NextRoute.key)
            composeTestRule.onNodeWithText(nextSecond).assertIsDisplayed()

            onBackPressed()
            composeTestRule.onNodeWithText(nextFirst).assertIsDisplayed()

            onBackPressed()
            composeTestRule.onNodeWithText(bottomButton2).assertIsDisplayed()

            onBackPressed()
            composeTestRule.onNodeWithText(nextFirst).assertIsDisplayed()

            onBackPressed()
            composeTestRule.onNodeWithText(bottomScreen1).assertIsDisplayed()

            onBackPressed()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(go_to_forth).assertIsDisplayed()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    public fun DialogTest() {
        val go_to_forth = composeTestRule.activity.getString(R.string.go_to_forth)
        val show_dialog = composeTestRule.activity.getString(R.string.show_dialog)
        val choose_item = composeTestRule.activity.getString(R.string.choose_item)
        val close = composeTestRule.activity.getString(R.string.close)
        val detail_item_dialog = composeTestRule.activity.getString(R.string.detail_dialog, galleryItems[12].name, galleryItems[12].age)
        val go_back = composeTestRule.activity.getString(R.string.go_back)

        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(go_to_forth).performClick()

            composeTestRule.onNodeWithText(MultipleStack.Third::class.simpleName!!).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(show_dialog).performClick()

            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(choose_item).assertIsDisplayed()

            val lazyColumn = composeTestRule.onNodeWithTag("lazy_column")
            lazyColumn.performScrollToIndex(12)
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(galleryItems[12].name).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(detail_item_dialog).assertIsDisplayed()
        }
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(detail_item_dialog).assertIsDisplayed()

            composeTestRule.onNodeWithText(go_back).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(choose_item).assertIsDisplayed()
            composeTestRule.onNodeWithText(galleryItems[12].name).assertIsDisplayed()

            composeTestRule.activity.onBackPressed()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(choose_item).assertDoesNotExist()
            composeTestRule.onNodeWithText(galleryItems[12].name).assertDoesNotExist()

            composeTestRule.onNodeWithText(show_dialog).assertIsDisplayed()
        }
    }

    @Test
    public fun NavigationDialogTest() {
        val go_to_forth = composeTestRule.activity.getString(R.string.go_to_forth)
        val navigation_dialog = composeTestRule.activity.getString(R.string.navigation_dialog)
        val go_to_second = composeTestRule.activity.getString(R.string.go_to_second)
        val close = composeTestRule.activity.getString(R.string.close)
        val go_back = composeTestRule.activity.getString(R.string.go_back)

        val first_dialog_route = NavigationDialogRoute.First::class.qualifiedName.toString()
        val second_dialog_route = NavigationDialogRoute.Second::class.qualifiedName.toString()

        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(go_to_forth).performClick()

            composeTestRule.onNodeWithText(MultipleStack.Third::class.simpleName!!).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(navigation_dialog).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(first_dialog_route).assertIsDisplayed()

            composeTestRule.onNodeWithText(go_to_second).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(second_dialog_route, substring = true).assertIsDisplayed()

            onBackPressed()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(first_dialog_route).assertIsDisplayed()

            onBackPressed()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(first_dialog_route).assertDoesNotExist()
            composeTestRule.onNodeWithText(navigation_dialog).assertIsDisplayed()
            composeTestRule.onNodeWithText(navigation_dialog).performClick()

            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(go_to_second).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(close).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(second_dialog_route, substring = true).assertDoesNotExist()
            composeTestRule.onNodeWithText(navigation_dialog).performClick()

            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(go_to_second).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(go_back).performClick()
            composeTestRule.onNodeWithText(second_dialog_route, substring = true).assertDoesNotExist()
            composeTestRule.onNodeWithText(first_dialog_route).assertIsDisplayed()

            composeTestRule.onNodeWithText(go_to_second).performClick()
            composeTestRule.waitForIdle()
        }
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(second_dialog_route, substring = true).assertIsDisplayed()

            onBackPressed()
            composeTestRule.waitForIdle()
            onBackPressed()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(second_dialog_route, substring = true).assertDoesNotExist()
            composeTestRule.onNodeWithText(first_dialog_route).assertDoesNotExist()

            composeTestRule.onNodeWithText(navigation_dialog).assertIsDisplayed()
        }
    }

    @Test
    public fun HandleDismissRequestDialogTest() {
        val go_to_forth = composeTestRule.activity.getString(R.string.go_to_forth)
        val dismiss_dialog_button = composeTestRule.activity.getString(R.string.dismiss_dialog)
        val force_close = composeTestRule.activity.getString(R.string.force_close)
        val close = composeTestRule.activity.getString(R.string.close)

        val dialog_text = DismissDialog::class.qualifiedName.toString()

        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(go_to_forth).performClick()

            composeTestRule.onNodeWithText(MultipleStack.Third::class.simpleName!!).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(dismiss_dialog_button).performClick()
            composeTestRule.waitForIdle()

            onBackPressed()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(dialog_text).assertIsDisplayed()

            onBackPressed()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(dialog_text).assertDoesNotExist()

            // We will use dismiss button which should work same as onBackPressed()

            composeTestRule.onNodeWithText(dismiss_dialog_button).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(close).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(dialog_text).assertIsDisplayed()

            composeTestRule.onNodeWithText(close).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(dialog_text).assertDoesNotExist()

            // Now we will force close dialog (no twice)

            composeTestRule.onNodeWithText(dismiss_dialog_button).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(force_close).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(dialog_text).assertDoesNotExist()
        }
    }

    @Test // I don't like mocking so better be it an instrumented test
    public fun DialogHistorySavedInstanceTest() {
        var dialogHistory = ComposeNavigator.History.DialogHistory()
        dialogHistory.createDialogScope(DialogRoutes.FirstDialog) { false }
        dialogHistory.createDialogScope(DialogRoutes.SecondDialog) { false }

        dialogHistory.add(DialogRoutes.FirstDialog)

        val bundle = Bundle()
        dialogHistory.saveState(bundle)

        dialogHistory = ComposeNavigator.History.DialogHistory()

        dialogHistory.restoreState(bundle)

        assert(dialogHistory.peek() is DialogRoutes.FirstDialog)
        assert(dialogHistory.count() == 1)
    }

    @OptIn(UnstableNavigatorApi::class)
    @Test
    public fun NavigatorGoBackUntilTest() {

        fun createDefaultNavigator() : ComposeNavigator {
            val navigator = ComposeNavigator.with(composeTestRule.activity, null)
                .disableDefaultBackPressLogic()
                .disableOnSaveStateInstance()
                .initialize()

            val startRouteHistory = ComposeNavigator.History(StartRoute.key, null, StartRoute.First(""))
            startRouteHistory.push(StartRoute.Second(""))
            startRouteHistory.push(StartRoute.Third(""))

            val nextRouteHistory = ComposeNavigator.History(NextRoute.key, StartRoute.Third::class, NextRoute.First())
            nextRouteHistory.push(NextRoute.Second())

            val thirdRouteHistory = ComposeNavigator.History(ThirdRoute.key, NextRoute.Second::class, ThirdRoute.Primary())
            thirdRouteHistory.push(ThirdRoute.Secondary(GalleryItem("test", 0)))

            navigator.backStackMap[StartRoute::class] = startRouteHistory
            navigator.backStackMap[NextRoute::class] = nextRouteHistory
            navigator.backStackMap[ThirdRoute::class] = thirdRouteHistory
            return navigator
        }

        fun ComposeNavigator.peekLastFromBackStack() : Route {
            return backStackMap.lastValue()!!.peek().key
        }

        var navigator = createDefaultNavigator()

        assert(navigator.peekLastFromBackStack() is ThirdRoute.Secondary)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "s:1" (inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntil(StartRoute.First::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is StartRoute.First)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "s:1" (not inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntil(StartRoute.First::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is StartRoute.First)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "t:2" (inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntil(ThirdRoute.Secondary::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is ThirdRoute.Primary)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "t:2" (not inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntil(ThirdRoute.Secondary::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is ThirdRoute.Secondary)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "t:1" (inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntil(ThirdRoute.Primary::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is NextRoute.Second)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "t:1" (not inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntil(ThirdRoute.Primary::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is ThirdRoute.Primary)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "n:1" (not inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntil(NextRoute.First::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is NextRoute.First)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "n:1" (inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntil(NextRoute.First::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is StartRoute.Third)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "n:1" (not inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntil(NextRoute.First::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is NextRoute.First)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "n:2" (not inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntil(NextRoute.Second::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is NextRoute.Second)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "n:2" (inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntil(NextRoute.Second::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is NextRoute.First)
    }

    private fun <T : Route> ComposeNavigator.History<T>.push(element: T) {
        push(ComposeNavigator.History.BackStackRecord(element))
    }
}