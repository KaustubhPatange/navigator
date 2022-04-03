package com.kpstv.navigation.compose.sample.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kpstv.navigation.compose.*
import kotlinx.parcelize.Parcelize

sealed class MenuSettingRoute : Route {
    @Parcelize @Immutable
    data class First(private val noArg: String = "") : MenuSettingRoute()
    @Parcelize @Immutable
    data class Second(private val noArg: String = "") : MenuSettingRoute()
    companion object Key : Route.Key<MenuSettingRoute>
//    companion object { val key = MenuSettingRoute::class }
}

@Composable
fun MenuSettingScreen() {
    val navigator = findComposeNavigator()
    val settingController = rememberNavController<MenuSettingRoute>()
    navigator.Setup(key = MenuSettingRoute.key, controller = settingController, initial = MenuSettingRoute.First()) { dest ->
        when(dest) {
            is MenuSettingRoute.First -> MenuSettingFirstScreen()
            is MenuSettingRoute.Second -> MenuSettingSecondScreen()
        }
    }
}

@OptIn(UnstableNavigatorApi::class)
@Composable
private fun MenuSettingFirstScreen() {
    val navigator = findComposeNavigator()
    val settingController = findNavController(key = MenuSettingRoute.key)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val text = rememberSaveable { mutableStateOf("") }

        Text(
            text = "Enter some text, this will be saved while the screen is in the backstack.",
            modifier = Modifier.padding(horizontal = 20.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextField(value = text.value, onValueChange = { text.value = it })

        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { settingController.navigateTo(MenuSettingRoute.Second()) {
            withAnimation {
                target = Fade
                current = Fade
            }
        } }) {
            Text(text = "Go to Settings:Second")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { navigator.goBackUntil(MainRoute.Second::class, inclusive = true) }) { // equivalent to "navigator.goBackUntil(MenuItem.Home::class, inclusive = false)"
            Text(text = "Go to StartRoute:Second navigation")
        }
    }
}

@OptIn(UnstableNavigatorApi::class)
@Composable
private fun MenuSettingSecondScreen() {
    val navigator = findComposeNavigator()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navigator.goBackToRoot() }) {
            Text(text = "Go to Root")
        }
    }
}