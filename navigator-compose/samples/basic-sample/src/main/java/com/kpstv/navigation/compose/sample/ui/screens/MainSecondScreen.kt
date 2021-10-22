package com.kpstv.navigation.compose.sample.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kpstv.navigation.compose.SlideLeft
import com.kpstv.navigation.compose.SlideRight
import com.kpstv.navigation.compose.findComposeNavigator
import com.kpstv.navigation.compose.rememberNavController
import com.kpstv.navigation.compose.sample.ui.componenets.Menu
import com.kpstv.navigation.compose.sample.ui.componenets.MenuItem

@Composable
fun MainSecondScreen() {
    Column {
        // if the menu is moved inside the navigator scope then it'll too
        // undergo recomposition when destination is changed which looks
        // bad if animations are enabled.

        val controller = rememberNavController<MenuItem>()
        val destination = remember { mutableStateOf(MenuItem.Home() as MenuItem) }

        findComposeNavigator().Setup(
            modifier = Modifier.weight(1f),
            controller = controller,
            key = MenuItem.key,
            initial = MenuItem.Home()
        ) { dest ->
            destination.value = dest

            Box(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentSize(Alignment.TopStart)
            ) {
                when (dest) {
                    is MenuItem.Home -> MenuHomeScreen(routeKey = MenuHomeRouteKey.key)
                    is MenuItem.Favourite -> MenuFavouriteScreen()
                    is MenuItem.Settings -> MenuSettingScreen()
                }
            }
        }
        Menu.Content(
            state = Menu.State(currentSelection = destination.value),
            onMenuItemClicked = { menuItem ->
                controller.navigateTo(menuItem) {
                    // if (menuItem is MenuItem.Home) {
                    singleTop = true
                    //}
                    withAnimation {
                        when (destination.value) {
                            is MenuItem.Home -> {
                                target = SlideRight
                                current = SlideLeft
                            }
                            is MenuItem.Settings -> {
                                target = SlideLeft
                                current = SlideRight
                            }
                            is MenuItem.Favourite -> {
                                if (menuItem is MenuItem.Home) {
                                    target = SlideLeft
                                    current = SlideRight
                                } else {
                                    target = SlideRight
                                    current = SlideLeft
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
