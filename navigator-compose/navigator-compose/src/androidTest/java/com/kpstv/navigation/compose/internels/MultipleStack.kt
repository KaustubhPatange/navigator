package com.kpstv.navigation.compose.internels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kpstv.navigation.compose.ComposeNavigator
import com.kpstv.navigation.compose.Route
import com.kpstv.navigation.compose.findComposeNavigator
import com.kpstv.navigation.compose.test.R
import kotlinx.parcelize.Parcelize

@Composable
public fun MultipleStackScreen() {
    Column {
        var controller: ComposeNavigator.Controller<MultipleStack>? = null
        findComposeNavigator().Setup(modifier = Modifier.weight(1f), key = MultipleStack.key, initial = MultipleStack.First() as MultipleStack) { con, dest ->
            controller = con
            when(dest) {
                is MultipleStack.Third -> NextScreen()
                else -> ReusableScreen(dest::class.qualifiedName!!)
            }
        }
        Row {
            0.rangeTo(2).forEach { value ->
                val route = MultipleStack.getRouteFromIndex(value)
                Button(onClick = { controller?.navigateTo(route) }) {
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
    findComposeNavigator().Setup(key = NextRoute.key, initial = NextRoute.First() as NextRoute) { controller, dest ->
        when(dest) {
            is NextRoute.First -> NextScreenFirst { controller.navigateTo(NextRoute.Second()) }
            is NextRoute.Second -> ReusableScreen(dest::class.qualifiedName!!)
        }
    }
}

@Composable
internal fun NextScreenFirst(next: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(NextRoute.First::class.qualifiedName!!)
        Button(onClick = { next.invoke() }) {
            Text(stringResource(id = R.string.go_to_second_bottom))
        }
    }
}