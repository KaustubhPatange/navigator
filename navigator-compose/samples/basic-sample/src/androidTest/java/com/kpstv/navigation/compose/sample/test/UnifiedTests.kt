package com.kpstv.navigation.compose.sample.test

import android.util.Log
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
import com.kpstv.navigation.compose.sample.MainActivity
import com.kpstv.navigation.compose.sample.ui.screens.MainFirstRoute
import com.kpstv.navigation.compose.sample.ui.screens.MainRoute
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
}