package com.kpstv.navigation.compose.sample.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kpstv.navigation.compose.*
import kotlinx.parcelize.Parcelize

sealed interface MainFirstRoute : Route {
    @Immutable
    @Parcelize
    data class Primary(private val noArg: String = "") : MainFirstRoute

    @Immutable
    @Parcelize
    data class Third(private val noArg: String = "") : MainFirstRoute
    companion object Key : Route.Key<MainFirstRoute>
}

@Composable
fun MainFirstScreen(data: String, goToSecond: (MainRoute) -> Unit) {
    val navigator = findComposeNavigator()
    val firstRouteController = rememberNavController<MainFirstRoute>()
    navigator.Setup(key = MainFirstRoute.key, initial = MainFirstRoute.Primary(), controller = firstRouteController) { dest ->
        when (dest) {
            is MainFirstRoute.Primary -> MainFirstPrimaryScreen(data, goToSecond) { route ->
                firstRouteController.navigateTo(route) {
                    withAnimation {
                        target = SlideRight
                        current = Fade
                    }
                }
            }
            is MainFirstRoute.Third -> MainFirstThirdScreen()
        }
    }
}

@Composable
private fun MainFirstPrimaryScreen(data: String, goToSecond: (MainRoute) -> Unit, goToThird: (MainFirstRoute) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("First Screen: $data", color = Color.Black)
        Button(
            onClick = { goToSecond.invoke(MainRoute.Second()) },
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text("Go to second screen")
        }
        Button(
            onClick = { goToThird.invoke(MainFirstRoute.Third()) },
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text("Go to third screen")
        }
    }
}