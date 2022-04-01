package com.kpstv.navigation.compose.sample.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kpstv.navigation.compose.Route
import com.kpstv.navigation.compose.findComposeNavigator
import com.kpstv.navigation.compose.findNavController
import com.kpstv.navigation.compose.hilt.hiltViewModel
import com.kpstv.navigation.compose.rememberNavController
import com.kpstv.navigation.compose.sample.SlideWithFadeLeft
import com.kpstv.navigation.compose.sample.SlideWithFadeRight
import com.kpstv.navigation.compose.sample.ui.viewmodels.HiltTestViewModel
import kotlinx.parcelize.Parcelize

sealed interface MainFirstThirdRoute : Route {
    @Immutable
    @Parcelize
    data class MainFirstThirdPrimary(private val noArg: String = "") : MainFirstThirdRoute

    @Immutable
    @Parcelize
    data class MainFirstThirdHome(private val noArg: String = "") : MainFirstThirdRoute

    @Immutable
    @Parcelize
    data class MainFirstThirdSecondary(private val noArg: String = "") : MainFirstThirdRoute
    companion object Key : Route.Key<MainFirstThirdRoute>
}

@Composable
fun MainFirstThirdScreen() {
    val thirdRouteController = rememberNavController<MainFirstThirdRoute>()
    findComposeNavigator().Setup(
        key = MainFirstThirdRoute.key,
        initial = MainFirstThirdRoute.MainFirstThirdPrimary() as MainFirstThirdRoute,
        controller = thirdRouteController
    ) { dest ->
        val onChanged: (MainFirstThirdRoute) -> Unit = { screen ->
            thirdRouteController.navigateTo(screen) {
                withAnimation {
                    target = SlideWithFadeRight
                    current = SlideWithFadeLeft
                }
            }
        }
        when (dest) {
            is MainFirstThirdRoute.MainFirstThirdPrimary -> MainFirstThirdPrimaryScreen(onChanged)
            is MainFirstThirdRoute.MainFirstThirdHome -> MainFirstThirdHome()
            is MainFirstThirdRoute.MainFirstThirdSecondary -> MainFirstThirdSecondaryScreen()
        }
    }
}

@Composable
private fun MainFirstThirdPrimaryScreen(change: (MainFirstThirdRoute) -> Unit) {
    val viewModel = hiltViewModel<HiltTestViewModel>()

    val controller = findNavController(MainFirstThirdRoute.key)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Third Screen 1", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Random value from ViewModel: \"${viewModel.data}\", should survive configuration & process death.",
            modifier = Modifier.padding(horizontal = 20.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Behold! These navigation uses a custom defined transition.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { change.invoke(MainFirstThirdRoute.MainFirstThirdSecondary()) },
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text("Go to third screen")
        }
        Button(
            onClick = { change.invoke(MainFirstThirdRoute.MainFirstThirdHome()) },
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text("Go to home screen")
        }
        Spacer(modifier = Modifier.height(10.dp))
        IconButton(onClick = { controller.goBack() }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
        }
    }
}

@Composable
private fun MainFirstThirdSecondaryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Third Screen 2")
    }
}