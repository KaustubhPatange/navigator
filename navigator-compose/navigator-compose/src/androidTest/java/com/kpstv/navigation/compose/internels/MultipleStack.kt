package com.kpstv.navigation.compose.internels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kpstv.navigation.compose.*
import com.kpstv.navigation.compose.test.R
import kotlinx.parcelize.Parcelize

@Composable
public fun MultipleStackScreen() {
    Column {
        val controller = rememberNavController<MultipleStack>()
        findComposeNavigator().Setup(modifier = Modifier.weight(1f), key = MultipleStack.key, initial = MultipleStack.First() as MultipleStack, controller = controller) { dest ->
            when(dest) {
                is MultipleStack.Third -> NextScreen()
                else -> ReusableScreen(dest::class.qualifiedName!!)
            }
        }
        Row {
            0.rangeTo(2).forEach { value ->
                val route = MultipleStack.getRouteFromIndex(value)
                Button(onClick = { controller.navigateTo(route) }) {
                    Text(route::class.simpleName!!)
                }
            }
        }
    }
}

public sealed class MultipleStack : Route {
    @Parcelize public data class First(private val noArg: String = ""): MultipleStack()
    @Parcelize public data class Second(private val noArg: String = ""): MultipleStack()
    @Parcelize public data class Third(private val noArg: String = ""): MultipleStack()

    internal companion object {
        val key = MultipleStack::class
        internal fun getRouteFromIndex(index: Int) = when(index) {
            0 -> First()
            1 -> Second()
            2 -> Third()
            else -> throw NotImplementedError()
        }
    }
}

@Composable
internal fun ReusableScreen(text: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = text)
    }
}

public sealed class NextRoute : Route {
    @Parcelize public data class First(private val noArg: String = ""): NextRoute()
    @Parcelize public data class Second(private val noArg: String = ""): NextRoute()
    internal companion object {
        val key = NextRoute::class
    }
}

@Composable
internal fun NextScreen() {
    val controller = rememberNavController<NextRoute>()
    findComposeNavigator().Setup(key = NextRoute.key, initial = NextRoute.First() as NextRoute, controller = controller) { dest ->
        when(dest) {
            is NextRoute.First -> NextScreenFirst { controller.navigateTo(NextRoute.Second()) }
            is NextRoute.Second -> ReusableScreen(dest::class.qualifiedName!!)
        }
    }
}

@Parcelize
internal object GalleryDialog: DialogRoute

@Parcelize
internal data class GalleryDetailDialog(val item: GalleryItem) : DialogRoute

@Parcelize
internal object NavigationDialog : DialogRoute

internal sealed class NavigationDialogRoute : Route {
    @Parcelize
    data class First(private val noArg: String = ""): NavigationDialogRoute()
    @Parcelize
    data class Second(val message: String): NavigationDialogRoute()
}

@Parcelize
internal object DismissDialog : DialogRoute

@Composable
internal fun NextScreenFirst(next: () -> Unit) {
    val nextScreenController = findController(key = NextRoute.key)

    nextScreenController.enableDialogOverlay = true

    Column(modifier = Modifier.fillMaxSize()) {
        Text(NextRoute.First::class.qualifiedName!!)
        Button(onClick = { next.invoke() }) {
            Text(stringResource(id = R.string.go_to_second_bottom))
        }
        Button(onClick = { nextScreenController.showDialog(GalleryDialog) }) {
            Text(stringResource(id = R.string.show_dialog))
        }
        Button(onClick = { nextScreenController.showDialog(DismissDialog) }) {
            Text(stringResource(id = R.string.dismiss_dialog))
        }
        Button(onClick = { nextScreenController.showDialog(NavigationDialog) }) {
            Text(stringResource(id = R.string.navigation_dialog))
        }
    }

    nextScreenController.CreateDialog(key = GalleryDialog::class) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)) {
            Text(stringResource(id = R.string.choose_item))
            ThirdPrimaryScreen(modifier = Modifier.height(500.dp)) { nextScreenController.showDialog(GalleryDetailDialog(it)) }
            Button(onClick = ::dismiss) {
                Text(stringResource(id = R.string.close))
            }
        }
    }

    nextScreenController.CreateDialog(key = GalleryDetailDialog::class) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)) {
            Text(stringResource(id = R.string.detail_dialog, dialogRoute.item.name, dialogRoute.item.age))
            Button(onClick = ::dismiss) {
                Text(stringResource(id = R.string.go_back))
            }
        }
    }
    
    val twiceClose = remember { mutableStateOf(false) }
    nextScreenController.CreateDialog(key = DismissDialog::class, handleOnDismissRequest = handle@{
        if (!twiceClose.value) {
            twiceClose.value = true
            return@handle true
        }
        return@handle false
    }) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)) {
            Text(DismissDialog::class.qualifiedName.toString())
            Button(onClick = ::dismiss) {
                Text(stringResource(id = R.string.close))
            }
            Button(onClick = { nextScreenController.closeDialog(DismissDialog::class) }) {
                Text(stringResource(id = R.string.force_close))
            }
        }
        LaunchedEffect(Unit) { twiceClose.value = false }
    }

    // Nested navigation
    nextScreenController.CreateDialog(key = NavigationDialog::class) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)) {
            val controller = rememberNavController<NavigationDialogRoute>()
            dialogNavigator.Setup(key = NavigationDialogRoute::class, initial = NavigationDialogRoute.First(), controller = controller) { dest ->
                when(dest) {
                    is NavigationDialogRoute.First -> {
                        Column(modifier = Modifier.height(300.dp)) {
                            Text(dest::class.qualifiedName.toString())
                            Button(onClick = {
                                controller.navigateTo(NavigationDialogRoute.Second("Hello world message"))
                            }) {
                                Text(stringResource(id = R.string.go_to_second))
                            }
                        }
                    }
                    is NavigationDialogRoute.Second -> {
                        Column(modifier = Modifier.height(300.dp)) {
                            Text("${dest::class.qualifiedName.toString()}: ${dest.message}")
                            Button(onClick = ::goBack) {
                                Text(stringResource(id = R.string.go_back))
                            }
                            Button(onClick = ::dismiss) {
                                Text(stringResource(id = R.string.close))
                            }
                        }
                    }
                }
            }
        }
    }

}

