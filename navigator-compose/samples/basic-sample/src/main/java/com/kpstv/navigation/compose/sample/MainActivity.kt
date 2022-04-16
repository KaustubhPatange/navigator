package com.kpstv.navigation.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.kpstv.navigation.compose.ComposeNavigator
import com.kpstv.navigation.compose.rememberNavController
import com.kpstv.navigation.compose.sample.ui.screens.MainRoute
import com.kpstv.navigation.compose.sample.ui.screens.MainScreen
import com.kpstv.navigation.compose.sample.ui.theme.ComposeTestAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        println(outState)
    }
}