package com.kpstv.navigation.compose.sample.ui.screens

import androidx.compose.runtime.Composable
import com.kpstv.navigation.compose.Route

private class MainFirstThirdHomeKey { companion object Key : Route.Key<MenuHomeRoute> }

@Composable
fun MainFirstThirdHome() {
    MenuHomeScreen(routeKey = MainFirstThirdHomeKey.key)
}