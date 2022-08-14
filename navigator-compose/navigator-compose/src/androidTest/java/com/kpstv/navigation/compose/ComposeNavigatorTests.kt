@file:Suppress("invisible_reference", "invisible_member")

package com.kpstv.navigation.compose

import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kpstv.navigation.compose.internels.*
import com.kpstv.navigation.compose.test.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.reflect.KClass

@LargeTest
@RunWith(AndroidJUnit4::class)
public class ComposeNavigatorTests {
    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> = createAndroidComposeRule()

    @Before
    public fun init() {
        LifecycleControllerStore.clear()
    }

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

            backpress()

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

            backpress()

            val lazyColumn = composeTestRule.onNodeWithTag("lazy_column")
            lazyColumn.assertIsDisplayed()

            // Check if rememberSaveable is restoring state properly.
            lazyColumn.onChildAt(0).assertTextContains(item.name)

            assert(navigator.backStackMap.size == 2)

            backpress()

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

            backpress()

            composeTestRule.onNodeWithText(nextSecond).assertIsDisplayed()
            assert(navigator.backStackMap.keys.last() == NextRoute.key)

            backpress()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(nextFirst).assertIsDisplayed()

            backpress()

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

            backpress()
            composeTestRule.waitForIdle()

            assert(navigator.backStackMap.keys.last() == NextRoute.key)
            composeTestRule.onNodeWithText(nextSecond).assertIsDisplayed()

            backpress()
            composeTestRule.onNodeWithText(nextFirst).assertIsDisplayed()

            backpress()
            composeTestRule.onNodeWithText(bottomButton2).assertIsDisplayed()

            backpress()
            composeTestRule.onNodeWithText(nextFirst).assertIsDisplayed()

            backpress()
            composeTestRule.onNodeWithText(bottomScreen1).assertIsDisplayed()

            backpress()
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

            backpress()
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

            backpress()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(first_dialog_route).assertIsDisplayed()

            backpress()
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

            backpress()
            composeTestRule.waitForIdle()
            backpress()
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

            backpress()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(dialog_text).assertIsDisplayed()

            backpress()
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

        // [s = {1,2,3} , n = {1,2} , t = {1,2}]
        fun createDefaultNavigator() : ComposeNavigator {
            val navigator = ComposeNavigator.with(composeTestRule.activity, null)
                .disableDefaultBackPressLogic()
                .disableOnSaveStateInstance()
                .initialize()

            val startRouteHistory = ComposeNavigator.History(StartRoute.key, null, StartRoute.First(""))
            startRouteHistory.push(StartRoute.Second(""))
            val thirdRoute = StartRoute.Third("")
            startRouteHistory.push(thirdRoute)

            val nextRouteSecond = NextRoute.Second()
            val nextRouteHistory = ComposeNavigator.History(NextRoute.key, thirdRoute, NextRoute.First())
            nextRouteHistory.push(nextRouteSecond)

            val thirdRouteHistory = ComposeNavigator.History(ThirdRoute.key, nextRouteSecond, ThirdRoute.Primary())
            thirdRouteHistory.push(ThirdRoute.Secondary(GalleryItem("test", 0)))

            navigator.backStackMap[StartRoute.key] = startRouteHistory
            navigator.backStackMap[NextRoute.key] = nextRouteHistory
            navigator.backStackMap[ThirdRoute.key] = thirdRouteHistory
            return navigator
        }

        fun ComposeNavigator.peekLastFromBackStack() : Route {
            return backStackMap.lastValue()!!.peek().key
        }

        fun ComposeNavigator.goBackUntilMainThread(key: KClass<out Route>, inclusive: Boolean = false) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync { goBackUntil(key, inclusive = inclusive) }
        }

        fun ComposeNavigator.goBackToRootMainThread() {
            InstrumentationRegistry.getInstrumentation().runOnMainSync { goBackToRoot() }
        }

        var navigator = createDefaultNavigator()

        assert(navigator.peekLastFromBackStack() is ThirdRoute.Secondary)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "s:1" (inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntilMainThread(StartRoute.First::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is StartRoute.First)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "s:1" (not inclusive) = (also equals jump to root)
        navigator = createDefaultNavigator()
        navigator.goBackUntilMainThread(StartRoute.First::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is StartRoute.First)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "t:2" (inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntilMainThread(ThirdRoute.Secondary::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is ThirdRoute.Primary)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "t:2" (not inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntilMainThread(ThirdRoute.Secondary::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is ThirdRoute.Secondary)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "t:1" (inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntilMainThread(ThirdRoute.Primary::class, inclusive = true)
        // why NextRoute.First? because n:2 is associated to t:1 so it only serves as nested navigation.
        // Having set inclusive means to to exclude it & go back previous i.e n:1
        assert(navigator.peekLastFromBackStack() is NextRoute.First)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "t:1" (not inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntilMainThread(ThirdRoute.Primary::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is ThirdRoute.Primary)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "n:1" (not inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntilMainThread(NextRoute.First::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is NextRoute.First)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "n:1" (inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntilMainThread(NextRoute.First::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is StartRoute.Second)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "n:1" (not inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntilMainThread(NextRoute.First::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is NextRoute.First)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "n:2" (not inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntilMainThread(NextRoute.Second::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is ThirdRoute.Primary)

        // [s = {1,2,3} , n = {1,2} , t = {1,2}] target is "n:2" (inclusive)
        navigator = createDefaultNavigator()
        navigator.goBackUntilMainThread(NextRoute.Second::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is NextRoute.First)

        // ******
        // createDefaultNavigator2()
        // [s = {1} , n = {1,2} , t = {1,2}]
        fun createDefaultNavigator2() : ComposeNavigator {
            val navigator = ComposeNavigator.with(composeTestRule.activity, null)
                .disableDefaultBackPressLogic()
                .disableOnSaveStateInstance()
                .initialize()

            val startFirstRoute = StartRoute.First("")
            val startRouteHistory = ComposeNavigator.History(StartRoute.key, null, startFirstRoute)

            val nextRouteHistory = ComposeNavigator.History(NextRoute.key, startFirstRoute, NextRoute.First())

            val nextSecondRoute = NextRoute.Second()
            nextRouteHistory.push(nextSecondRoute)

            val thirdRouteHistory = ComposeNavigator.History(ThirdRoute.key, nextSecondRoute, ThirdRoute.Primary())
            thirdRouteHistory.push(ThirdRoute.Secondary(GalleryItem("test", 0)))

            navigator.backStackMap[StartRoute.key] = startRouteHistory
            navigator.backStackMap[NextRoute.key] = nextRouteHistory
            navigator.backStackMap[ThirdRoute.key] = thirdRouteHistory
            return navigator
        }

        // [s = {1} , n = {1,2} , t = {1,2}] target is s:1 (not inclusive) i.e jump to root.
        navigator = createDefaultNavigator2()
        navigator.goBackToRootMainThread()
        assert(navigator.peekLastFromBackStack() is NextRoute.First)

        // [s = {1} , n = {1,2} , t = {1,2}] target is n:1 (not inclusive)
        navigator = createDefaultNavigator2()
        navigator.goBackUntilMainThread(NextRoute.First::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is NextRoute.First)

        // [s = {1} , n = {1,2} , t = {1,2}] target is n:1 (inclusive)
        navigator = createDefaultNavigator2()
        navigator.goBackUntilMainThread(NextRoute.First::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is NextRoute.First)

        // [s = {1} , n = {1,2} , t = {1,2}] target is n:1 (inclusive)
        navigator = createDefaultNavigator2()
        navigator.goBackUntilMainThread(NextRoute.First::class, inclusive = false)
        assert(navigator.peekLastFromBackStack() is NextRoute.First)

        // ******
        // createDefaultNavigator3()
        // [s = {1} , n = {1} , t = {1,2}]
        fun createDefaultNavigator3() : ComposeNavigator {
            val navigator = ComposeNavigator.with(composeTestRule.activity, null)
                .disableDefaultBackPressLogic()
                .disableOnSaveStateInstance()
                .initialize()

            val startFirstRoute = StartRoute.First("")
            val startRouteHistory = ComposeNavigator.History(StartRoute.key, null, startFirstRoute)

            val nextRouteFirst = NextRoute.First()
            val nextRouteHistory = ComposeNavigator.History(NextRoute.key, startFirstRoute, nextRouteFirst)
            val thirdRouteHistory = ComposeNavigator.History(ThirdRoute.key, nextRouteFirst, ThirdRoute.Primary())
            thirdRouteHistory.push(ThirdRoute.Secondary(GalleryItem("test", 0)))

            navigator.backStackMap[StartRoute.key] = startRouteHistory
            navigator.backStackMap[NextRoute.key] = nextRouteHistory
            navigator.backStackMap[ThirdRoute.key] = thirdRouteHistory
            return navigator
        }

        // [s = {1} , n = {1} , t = {1,2}] target is s:1 (not inclusive) i.e jump to route
        navigator = createDefaultNavigator3()
        navigator.goBackToRootMainThread()
        assert(navigator.peekLastFromBackStack() is ThirdRoute.Primary)

        // [s = {1} , n = {1} , t = {1,2}] target is s:1 (inclusive)
        navigator = createDefaultNavigator3()
        navigator.goBackUntilMainThread(StartRoute.First::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is ThirdRoute.Primary)

        // [s = {1} , n = {1} , t = {1,2}] target is n:1 (inclusive)
        navigator = createDefaultNavigator3()
        navigator.goBackUntilMainThread(NextRoute.First::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is ThirdRoute.Primary)

        // [s = {1} , n = {1} , t = {1,2}] target is t:1 (inclusive)
        navigator = createDefaultNavigator3()
        navigator.goBackUntilMainThread(ThirdRoute.Primary::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is ThirdRoute.Primary)

        // ******
        // createDefaultNavigator3()
        // [m1 = {1} , m = {1,2} , m21 = {1} , m2 = {1,2,3} , m23 = {1,2}]
        // m1 is associated to m:1
        // m21 is associated to m2:1
        // m2 is associated to m:2
        // m23 is associated to m2:3
        // which means backstack is not in proper order.
        fun createDefaultNavigator4() : ComposeNavigator {
            val navigator = ComposeNavigator.with(composeTestRule.activity, null)
                .disableDefaultBackPressLogic()
                .disableOnSaveStateInstance()
                .initialize()

            val mainRouteFirst = Routes.MainRoute.First()
            val mainRouteSecond = Routes.MainRoute.Second()
            val mainSecondRouteFirst = Routes.MainSecondRoute.First()
            val mainSecondRouteThird = Routes.MainSecondRoute.Third()

            val m1Route = ComposeNavigator.History(Routes.MainFirstRoute.key, mainRouteFirst, Routes.MainFirstRoute.First())

            val mRoute = ComposeNavigator.History(Routes.MainRoute.key, null, mainRouteFirst)
            mRoute.push(mainRouteSecond)

            val m21Route = ComposeNavigator.History(Routes.MainSecondFirstRoute.key, mainSecondRouteFirst, Routes.MainSecondFirstRoute.First())

            val m2Route = ComposeNavigator.History(Routes.MainSecondRoute.key, mainRouteSecond, mainSecondRouteFirst)
            m2Route.push(Routes.MainSecondRoute.Second())
            m2Route.push(mainSecondRouteThird)

            val m23Route = ComposeNavigator.History(Routes.MainSecondThirdRoute.key, mainSecondRouteThird, Routes.MainSecondThirdRoute.First())
            m23Route.push(Routes.MainSecondThirdRoute.Second())

            navigator.backStackMap[m1Route.key] = m1Route
            navigator.backStackMap[mRoute.key] = mRoute
            navigator.backStackMap[m21Route.key] = m21Route
            navigator.backStackMap[m2Route.key] = m2Route
            navigator.backStackMap[m23Route.key] = m23Route
            return navigator
        }

        // [m1 = {1} , m = {1,2} , m21 = {1} , m2 = {1,2,3} , m23 = {1,2}] target is m1:1 (not inclusive) i.e jump to root
        navigator = createDefaultNavigator4()
        navigator.goBackToRootMainThread()
        assert(navigator.peekLastFromBackStack() is Routes.MainFirstRoute.First)

        // [m1 = {1} , m = {1,2} , m21 = {1} , m2 = {1,2,3} , m23 = {1,2}] target is m2:1 (inclusive)
        navigator = createDefaultNavigator4()
        navigator.goBackUntilMainThread(Routes.MainSecondRoute.First::class, inclusive = true)
        assert(navigator.peekLastFromBackStack() is Routes.MainFirstRoute.First)
    }

    private fun <T : Route> ComposeNavigator.History<T>.push(element: T) {
        push(ComposeNavigator.History.BackStackRecord(element))
    }

    @Test
    public fun LifecycleControllerStoreTest() {
        val go_to_second = composeTestRule.activity.getString(R.string.go_to_second)
        val go_to_third = composeTestRule.activity.getString(R.string.go_to_third)

        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(go_to_second).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(go_to_third).performClick()
            composeTestRule.waitForIdle()
        }
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.activity.apply {
            composeTestRule.waitForIdle()
            var snapshot = LifecycleControllerStore.getSnapshot()
            assert(snapshot.isNotEmpty() && snapshot.size == 3)

            // We will compare the routes by preserving the order.
            var routes = snapshot.keys.map { it::class.qualifiedName }
            var orderedRoutes = listOf(
                StartRoute.Third::class.qualifiedName,
                ThirdRoute.Primary::class.qualifiedName,
                StartRoute.First::class.qualifiedName,
            )
            assert(routes == orderedRoutes)

            backpress()
            composeTestRule.waitForIdle()

            snapshot = LifecycleControllerStore.getSnapshot()
            routes = snapshot.keys.map { it::class.qualifiedName }
            orderedRoutes = listOf(
                StartRoute.First::class.qualifiedName
            )
            assert(snapshot.size == 1)
            assert(routes == orderedRoutes)
        }
    }

    @Test
    public fun ScopedViewModelTest() {
        val go_to_viewmodel = composeTestRule.activity.getString(R.string.go_to_viewmodel_screen)
        val go_to_different_viewmodel_screen = composeTestRule.activity.getString(R.string.go_to_different_viewmodel_screen)

        var global_testViewModelData: ArrayList<String>? = null
        var global_differentViewModelData: ArrayList<String>? = null

        fun verifyOrCaptureBundle(navigator: ComposeNavigator) {
            val bundle = Bundle()
            navigator.onSaveInstanceState(bundle)

            val firstKey = bundle.keySet().toList()[0]
            val navigatorBundle = bundle.getBundle(firstKey)!!
            val innerBundle = navigatorBundle.getBundle(navigatorBundle.keySet().toList()[0])!!
            val routeBundle = innerBundle.getBundle("compose_navigator:saved_state:${ViewModelRoute.TestViewModelInstances::class.qualifiedName}")!!
            val savedStateBundle = routeBundle.getBundle("androidx.lifecycle.BundlableSavedStateRegistry.key")!!
            val testViewModelBundle = savedStateBundle.getBundle("androidx.lifecycle.ViewModelProvider.DefaultKey:${TestViewModel::class.qualifiedName}")!!
            val testViewModelData = testViewModelBundle.getStringArrayList("values")

            val differentViewModelBundle = savedStateBundle.getBundle("different")!!
            val differentViewModelData = differentViewModelBundle.getStringArrayList("values")

            if (global_testViewModelData == null && global_differentViewModelData == null) {
                global_testViewModelData = testViewModelData
                global_differentViewModelData = differentViewModelData
                return
            }

            assert(global_testViewModelData == testViewModelData)
            assert(global_differentViewModelData == differentViewModelData)
        }

        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(go_to_viewmodel).performClick()
            composeTestRule.waitForIdle()

            // we will move to one screen ahead to capture the data of viewmodels
            // from previous screens.
            composeTestRule.onNodeWithText(go_to_different_viewmodel_screen).performClick()
            composeTestRule.waitForIdle()

            // automatically should tests viewmodels
            verifyOrCaptureBundle(navigator)
        }
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.activity.apply {
            verifyOrCaptureBundle(navigator)
        }
    }

    /*
     * If you are reusing Routes in navigation, then the LifecycleController instance will not
     * be different. Ideally you should avoid reusing routes, but if you to have to then remember
     * that the ViewModelStore & SaveStateRegistryOwner will not be different as they are tied to
     * Route & not the Route's key.
     *
     * Also reusing means composing routes with different
     */
    @Test
    public fun TestDifferentViewModelInstanceWhenReusingSameRoutes() {
        val go_to_viewmodel = composeTestRule.activity.getString(R.string.go_to_viewmodel_screen)
        val test_nested_viewmodel = composeTestRule.activity.getString(R.string.test_nested_viewmodel)
        val go_back = composeTestRule.activity.getString(R.string.go_back)

        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(go_to_viewmodel).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(test_nested_viewmodel).performClick()
            composeTestRule.waitForIdle()

            // automatically should test for same viewmodel store instance for Route.
            composeTestRule.onNodeWithText(go_back).performClick()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    public fun TestLifecycleEvents() {
        val go_to_second = composeTestRule.activity.getString(R.string.go_to_second)
        val second_screen = composeTestRule.activity.getString(R.string.second_screen)
        val go_to_forth = composeTestRule.activity.getString(R.string.go_to_forth)
        val show_dialog = composeTestRule.activity.getString(R.string.show_dialog)

        composeTestRule.activity.apply {
            val startRouteHistory = navigator.getHistory(StartRoute.key)
            val startFirstRoute = startRouteHistory.last()
            val startFirstLifecycle = startFirstRoute.lifecycleController.lifecycle
            assert(startFirstRoute is StartRoute.First)

            assert(startFirstLifecycle.currentState == Lifecycle.State.RESUMED)

            composeTestRule.onNodeWithText(go_to_second).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(second_screen).assertIsDisplayed()

            // onStop()
            assert(startFirstLifecycle.currentState == Lifecycle.State.CREATED)

            val startSecondRoute = navigator.getHistory(StartRoute.key).last()
            val startSecondLifecycle = startSecondRoute.lifecycleController.lifecycle
            // onResume()
            assert(startSecondLifecycle.currentState == Lifecycle.State.RESUMED)

            backpress()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(go_to_second).assertIsDisplayed()

            // onDestroy()
            assert(startSecondLifecycle.currentState == Lifecycle.State.DESTROYED)
            // onResume()
            assert(startFirstLifecycle.currentState == Lifecycle.State.RESUMED)

            composeTestRule.onNodeWithText(go_to_forth).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(MultipleStack.First::class.qualifiedName!!).assertIsDisplayed()

            val multiStackFirstLifecycle = navigator.getHistory(MultipleStack.key).last().lifecycleController.lifecycle

            // onStop()
            assert(startFirstLifecycle.currentState == Lifecycle.State.CREATED)
            // onResume()
            assert(multiStackFirstLifecycle.currentState == Lifecycle.State.RESUMED)

            composeTestRule.onNodeWithText(MultipleStack.Third::class.simpleName!!).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(NextRoute.First::class.qualifiedName!!).assertIsDisplayed()

            val nextFirstLifecycle = navigator.getHistory(NextRoute.key).last().lifecycleController.lifecycle

            // onStop()
            assert(multiStackFirstLifecycle.currentState == Lifecycle.State.CREATED)
            // onResume()
            assert(nextFirstLifecycle.currentState == Lifecycle.State.RESUMED)

            // show a dialog & track lifecycle

            composeTestRule.onNodeWithText(show_dialog).performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(getString(R.string.choose_item)).assertIsDisplayed()

            val galleryDialogRoute = navigator.getAllHistory().last().lifecycleController.lifecycle
            // onResume()
            assert(galleryDialogRoute.currentState == Lifecycle.State.RESUMED)

            backpress()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(getString(R.string.choose_item)).assertDoesNotExist()

            // onDestroy()
            assert(galleryDialogRoute.currentState == Lifecycle.State.DESTROYED)

            backpress()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(MultipleStack.First::class.qualifiedName!!).assertIsDisplayed()

            // onDestroy()
            assert(nextFirstLifecycle.currentState == Lifecycle.State.DESTROYED)

            backpress()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(go_to_second).assertIsDisplayed()

            // onDestroy()
            assert(multiStackFirstLifecycle.currentState == Lifecycle.State.DESTROYED)
        }
    }

    @Test
    public fun TestLifecycleBasedOnActivityStateChange() {
        val go_to_third = composeTestRule.activity.getString(R.string.go_to_third)

        composeTestRule.activity.apply {
            val startRouteHistory = navigator.getHistory(StartRoute.key)
            val startFirstLifecycle = startRouteHistory.last().lifecycleController.lifecycle

            composeTestRule.onNodeWithText(go_to_third).performClick()
            composeTestRule.waitForIdle()

            val thirdRoutePrimaryLifecycle = navigator.getHistory(ThirdRoute.key).last().lifecycleController.lifecycle

            composeTestRule.onNodeWithText(galleryItems[0].name).performClick()
            composeTestRule.waitForIdle()

            val thirdRouteSecondaryLifecycle = navigator.getHistory(ThirdRoute.key).last().lifecycleController.lifecycle

            // onPause()
            composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)

            assert(startFirstLifecycle.currentState == Lifecycle.State.CREATED) // onStop
            assert(thirdRoutePrimaryLifecycle.currentState == Lifecycle.State.CREATED) // onStop
            assert(thirdRouteSecondaryLifecycle.currentState == Lifecycle.State.STARTED) // onPause

            composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED) // onResume

            assert(startFirstLifecycle.currentState == Lifecycle.State.CREATED) // onStop
            assert(thirdRoutePrimaryLifecycle.currentState == Lifecycle.State.CREATED) // onStop
            assert(thirdRouteSecondaryLifecycle.currentState == Lifecycle.State.RESUMED) // onPause
        }
    }

}