package com.kpstv.navigation.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.kpstv.navigation.compose.ComposeNavigator
import com.kpstv.navigation.compose.rememberNavController
import com.kpstv.navigation.compose.sample.ui.screens.CloseDialog
import com.kpstv.navigation.compose.sample.ui.screens.MainFirstRoute
import com.kpstv.navigation.compose.sample.ui.screens.MainRoute
import com.kpstv.navigation.compose.sample.ui.screens.MainScreen
import com.kpstv.navigation.compose.sample.ui.theme.ComposeTestAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var navigator: ComposeNavigator
    private lateinit var controller: ComposeNavigator.Controller<MainRoute>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = ComposeNavigator.with(this, savedInstanceState)
            .registerTransitions(SlideWithFadeRightTransition, SlideWithFadeLeftTransition)
            .initialize()

        setContent {
            controller = rememberNavController()
            ComposeTestAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen(
                        navigator = navigator,
                        controller = controller,
                        mainRoute = MainRoute.First("Hello world")
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        // Show the dialog when navigation history contains 1 item
        // & close the dialog when dialog history contains the close dialog.
        if (navigator.getAllHistory().last() !is MainFirstRoute.Primary ||
            controller.getAllDialogHistory().lastOrNull() is CloseDialog ||
            navigator.suppressBackPress
        ) {
            super.onBackPressed()
        } else {
            controller.showDialog(CloseDialog)
        }
    }
}