package com.kpstv.navigation.compose

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
}