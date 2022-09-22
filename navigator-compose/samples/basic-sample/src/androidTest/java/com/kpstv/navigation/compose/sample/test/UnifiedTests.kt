@file:Suppress("invisible_reference", "invisible_member")

package com.kpstv.navigation.compose.sample.test

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kpstv.navigation.compose.LifecycleControllerStore
import com.kpstv.navigation.compose.sample.MainActivity
import com.kpstv.navigation.compose.sample.ui.componenets.MenuItem
import com.kpstv.navigation.compose.sample.ui.screens.MainFirstRoute
import com.kpstv.navigation.compose.sample.ui.screens.MainRoute
import com.kpstv.navigation.compose.sample.ui.screens.MenuHomeRoute
import com.kpstv.navigation.compose.sample.ui.screens.MenuHomeRouteKey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class UnifiedTests {
    @get:Rule
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> = createAndroidComposeRule()

    // Found that "Go to root" button was crashing app on device due to incorrect lifecycle events
    // handling like onDestroy() when app is not initialized.
    @Test
    fun goBackToRootTest() {
        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText("Go to second screen").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Settings").assertIsDisplayed()

            composeTestRule.onNodeWithText("Settings").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Go to Settings:Second").assertIsDisplayed()

            composeTestRule.onNodeWithText("Go to Settings:Second").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Go to Root").assertIsDisplayed()

            val routesAndLifecycles = navigator.getAllHistory().map { it to it.lifecycleController.lifecycle }

            composeTestRule.onNodeWithText("Go to Root").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Go to second screen").assertIsDisplayed()

            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            routesAndLifecycles.forEach { (route, lifecycle) ->
                if (route is MainFirstRoute.Primary || route is MainRoute.First) {
                    assert(route.lifecycleController.lifecycle.currentState == Lifecycle.State.RESUMED)
                } else {
                    assert(lifecycle.currentState == Lifecycle.State.DESTROYED)
                }
            }
        }
    }

    @Test
    fun correctLifecycleEventsTest() {
        composeTestRule.activity.apply {
            var states = navigator.getAllHistory().map { it.lifecycleController.lifecycle.currentState }

            // When application starts all states should be resumed.
            assert(states.all { it == Lifecycle.State.RESUMED })

            // Activity onPause()
            composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
            composeTestRule.waitForIdle()
            // Verify if all incoming states are onPaused()
            states = navigator.getAllHistory().map { it.lifecycleController.lifecycle.currentState }
            assert(states.all { it == Lifecycle.State.STARTED })

            // Activity onResume()
            composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

            composeTestRule.onNodeWithText("Go to second screen").performClick()
            composeTestRule.waitForIdle()

            val mainFirstRouteHistory = navigator.getHistory(MainFirstRoute.key)
            val mainFirstRoutePrimary = mainFirstRouteHistory.last()

            assert(mainFirstRoutePrimary is MainFirstRoute.Primary)
            // onPause()
            assert(mainFirstRoutePrimary.lifecycleController.lifecycle.currentState == Lifecycle.State.CREATED)

            with(navigator.getHistory(MainRoute.key)) {
                assert(last() is MainRoute.Second)
                assert(last().lifecycleController.lifecycle.currentState == Lifecycle.State.RESUMED)
            }

            with(navigator.getHistory(MenuItem.key)) {
                assert(last() is MenuItem.Home)
                assert(last().lifecycleController.lifecycle.currentState == Lifecycle.State.RESUMED)
            }

            with(navigator.getHistory(MenuHomeRouteKey.key)) {
                assert(last() is MenuHomeRoute.Primary)
                assert(last().lifecycleController.lifecycle.currentState == Lifecycle.State.RESUMED)
            }

            onBackPressed()
            composeTestRule.waitForIdle()

            // MenuHomeRoute.Primary, MenuItem.Home should be removed from LifecycleControllerStore
            assert(LifecycleControllerStore.getSnapshot().keys.none { it is MenuHomeRoute.Primary })
            assert(LifecycleControllerStore.getSnapshot().keys.none { it is MenuItem.Home })

            // onResume()
            assert(mainFirstRoutePrimary.lifecycleController.lifecycle.currentState == Lifecycle.State.RESUMED)
        }
    }
}