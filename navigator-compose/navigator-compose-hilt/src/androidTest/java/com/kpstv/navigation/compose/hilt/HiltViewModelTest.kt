@file:Suppress("invisible_reference", "invisible_member")

package com.kpstv.navigation.compose.hilt

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.kpstv.navigation.compose.LifecycleControllerStore
import com.kpstv.navigation.compose.hilt.internals.MainActivity
import com.kpstv.navigation.compose.hilt.test.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
public class HiltViewModelTest {
    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> = createAndroidComposeRule()

    @Before
    public fun init() {
        LifecycleControllerStore.clear()
    }

    @Test
    public fun HiltViewModelCreationTest() {
        val go_to_hilt_screen = composeTestRule.activity.getString(R.string.go_to_hilt_screen)

        composeTestRule.activity.apply {
            composeTestRule.onNodeWithText(go_to_hilt_screen).performClick()
            composeTestRule.waitForIdle()

            // automatically tests the creation of hilt viewmodel & crash when fails
        }
    }
}