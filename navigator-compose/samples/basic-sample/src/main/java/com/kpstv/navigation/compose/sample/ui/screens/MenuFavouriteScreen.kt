package com.kpstv.navigation.compose.sample.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.kpstv.navigation.compose.DialogRoute
import com.kpstv.navigation.compose.Route
import com.kpstv.navigation.compose.findNavController
import com.kpstv.navigation.compose.rememberNavController
import com.kpstv.navigation.compose.sample.ui.componenets.CommonItem
import com.kpstv.navigation.compose.sample.ui.componenets.MenuItem
import kotlinx.parcelize.Parcelize

@Parcelize
private object ListDialog : DialogRoute {
    val key get() = ListDialog::class
}

@Parcelize
private data class DetailDialog(val item: CommonItem) : DialogRoute {
    companion object {
        val key = DetailDialog::class
    }
}

@Parcelize
private object NavigationDialog: DialogRoute {
    val key get() = NavigationDialog::class
}

private sealed class DialogScopeRoute : Route {
    @Parcelize
    data class First(private val noArg: String = "") : DialogScopeRoute()
    @Parcelize
    data class Second(private val noArg: String = "") : DialogScopeRoute()
    companion object Key : Route.Key<DialogScopeRoute>
}

@Composable
fun MenuFavouriteScreen() {
    val controller = findNavController(key = MenuItem.key)

    /* try uncommenting the below line to enable dialog overlays */
//    controller.enableDialogOverlay = true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { controller.showDialog(ListDialog) }) {
            Text("Show a dialog")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { controller.showDialog(NavigationDialog) }) {
            Text("Nested navigation dialog")
        }
    }

    controller.CreateDialog(key = ListDialog.key) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background)
                .border(1.dp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
                .padding(20.dp)
        ) {
            Text("Choose an item", fontSize = 17.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(10.dp))
            Divider()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
            ) {
                MenuHomePrimaryScreen {
                    controller.showDialog(DetailDialog(it))
                }
            }
            Divider()
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = ::dismiss) {
                Text("Close")
            }
        }
    }

    controller.CreateDialog(key = DetailDialog.key) {
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colors.background)
                .border(1.dp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
                .padding(20.dp)
        ) {
            Text(text = "You selected item with name ${dialogRoute.item.name} & age ${dialogRoute.item.age}!")
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = ::dismiss) {
                Text(text = "Go Back")
            }
        }
    }

    // Dialog with navigation
    controller.CreateDialog(key = NavigationDialog.key, dialogProperties = DialogProperties(dismissOnClickOutside = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background)
                .clipToBounds() // clip content within the bounds, needed for animation to not look weird.
                .border(1.dp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
        ) {
            val dialogScopeController = rememberNavController<DialogScopeRoute>()
            dialogNavigator.Setup(key = DialogScopeRoute.key, initial = DialogScopeRoute.First(), controller = dialogScopeController) { dest ->
                when(dest) {
                    is DialogScopeRoute.First -> {
                        Column(modifier = Modifier
                            .background(MaterialTheme.colors.background)
                            .height(300.dp)
                            .fillMaxSize(),verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "You are on first screen")
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = {
                                dialogScopeController.navigateTo(DialogScopeRoute.Second()) {
                                    // Enable animations as well
                                    /*withAnimation {
                                        target = SlideRight
                                        current = Fade
                                    }*/
                                }
                            }) {
                                Text(text = "Go to second")
                            }
                        }
                    }
                    is DialogScopeRoute.Second -> {
                        Column(modifier = Modifier
                            .background(MaterialTheme.colors.background)
                            .height(300.dp)
                            .fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "You are on second screen")
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = { controller.goBack() }) {
                                Text(text = "Go back")
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = ::dismiss) {
                                Text(text = "Dismiss")
                            }
                        }
                    }
                }
            }
        }
    }
}