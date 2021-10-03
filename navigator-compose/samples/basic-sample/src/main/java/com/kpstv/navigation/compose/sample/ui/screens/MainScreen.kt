package com.kpstv.navigation.compose.sample.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kpstv.navigation.compose.ComposeNavigator
import com.kpstv.navigation.compose.DialogRoute
import com.kpstv.navigation.compose.Fade
import com.kpstv.navigation.compose.Route
import com.kpstv.navigation.compose.sample.MainActivity
import kotlinx.parcelize.Parcelize

sealed interface MainRoute : Route {
    @Immutable
    @Parcelize
    data class First(val data: String) : MainRoute

    @Immutable
    @Parcelize
    data class Second(private val noArgPlaceholder: String = "") : MainRoute
    companion object {
        val key = MainRoute::class
    }
}

@Parcelize
object CloseDialog : DialogRoute {
    val key get() = CloseDialog::class
}

@Composable
fun MainScreen(
    navigator: ComposeNavigator,
    controller: ComposeNavigator.Controller<MainRoute>,
    mainRoute: MainRoute
) {
    val activity = LocalContext.current as MainActivity

    navigator.Setup(
        key = MainRoute.key,
        initial = mainRoute,
        controller = controller
    ) { dest ->
        val onChanged: (screen: MainRoute) -> Unit = { value ->
            controller.navigateTo(value) {
                withAnimation {
                    target = Fade
                    current = Fade
                }
            }
        }
        when (dest) {
            is MainRoute.First -> MainFirstScreen(dest.data, onChanged)
            is MainRoute.Second -> MainSecondScreen()
        }

        /* Create "Close" dialog */
        controller.CreateDialog(key = CloseDialog.key) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
                    .padding(15.dp)
            ) {
                Text("Do you want to close the app?", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = ::dismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { activity.finish() }) {
                        Text("Yes")
                    }
                }
            }
        }
    }
}