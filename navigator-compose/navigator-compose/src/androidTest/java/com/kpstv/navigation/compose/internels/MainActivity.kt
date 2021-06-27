package com.kpstv.navigation.compose.internels

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kpstv.navigation.compose.ComposeNavigator
import com.kpstv.navigation.compose.Route
import com.kpstv.navigation.compose.findComposeNavigator
import com.kpstv.navigation.compose.findController
import com.kpstv.navigation.compose.test.R
import kotlinx.parcelize.Parcelize

public class MainActivity : ComponentActivity() {

    public lateinit var navigator: ComposeNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = ComposeNavigator.with(this, savedInstanceState).initialize()
        setContent {
            StartScreen(navigator)
        }
    }
}

public sealed class StartRoute : Route {
    @Parcelize
    public data class First(val data: String) : StartRoute()
    @Parcelize
    public data class Second(private val noArg: String = "") : StartRoute()
    @Parcelize
    public data class Third(private val noArg: String = "") : StartRoute()
    @Parcelize public data class Forth(private val noArg: String = "") : StartRoute()
    internal companion object {
        val key = StartRoute::class
    }
}

@Composable
public fun StartScreen(navigator: ComposeNavigator) {
    navigator.Setup(key = StartRoute.key, initial = StartRoute.First(stringResource(R.string.app_name)) as StartRoute) { controller, dest ->
        when(dest) {
            is StartRoute.First -> StartFirstScreen(
                data = dest.data,
                goToSecond = { controller.navigateTo(StartRoute.Second()) },
                goToThird = { controller.navigateTo(StartRoute.Third()) },
                goToForth = { controller.navigateTo(StartRoute.Forth()) }
            )
            is StartRoute.Second -> StartSecondScreen()
            is StartRoute.Third -> ThirdScreen()
            is StartRoute.Forth -> MultipleStackScreen()
        }
    }
}

@Composable
public fun StartFirstScreen(data: String, goToSecond: () -> Unit, goToThird: () -> Unit, goToForth: () -> Unit) {
    Column {
        Text(data)
        Button(onClick = { goToSecond.invoke() }) {
            Text(text = stringResource(id = R.string.go_to_second))
        }
        Button(onClick = { goToThird.invoke() }) {
            Text(text = stringResource(id = R.string.go_to_third))
        }
        Button(onClick = { goToForth.invoke() }) {
            Text(text = stringResource(id = R.string.go_to_forth))
        }
    }
}

@Composable
public fun StartSecondScreen() {
    val controller = findController(StartRoute.key)
    Column {
        Text(text = stringResource(id = R.string.second_screen))
        IconButton(onClick = { controller.goBack() }, Modifier.testTag("icon_button")) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
        }
        Button(onClick = {
            controller.navigateTo(StartRoute.Third()) {
                popUpTo(controller.getAllHistory().first()) {
                    inclusive = false // should be equivalent to history First, Third
                }
            }
        }) {
            Text(text = stringResource(id = R.string.go_to_third))
        }
    }
}


public sealed class ThirdRoute : Route {
    @Parcelize
    public data class Primary(private val noArg: String = "") : ThirdRoute()
    @Parcelize
    public data class Secondary(val item: GalleryItem) : ThirdRoute()
    internal companion object {
        val key = ThirdRoute::class
    }
}

@Composable
public fun ThirdScreen() {
    val navigator = findComposeNavigator()
    navigator.Setup(key = ThirdRoute.key, initial = ThirdRoute.Primary() as ThirdRoute) { controller, dest ->
        when(dest) {
            is ThirdRoute.Primary -> ThirdPrimaryScreen { controller.navigateTo(ThirdRoute.Secondary(it)) }
            is ThirdRoute.Secondary -> ThirdSecondaryScreen(dest.item)
        }
    }
}

@Composable
public fun ThirdPrimaryScreen(onNavigateToDetail: (GalleryItem) -> Unit) {
    val state = rememberLazyListState()
    LazyColumn(state = state, modifier = Modifier.testTag("lazy_column")) {
        items(galleryItems) { item ->
            Column(modifier = Modifier
                .clickable {
                    onNavigateToDetail(item)
                }
                .padding(10.dp)) {
                Text(text = item.name)
                Text(text = "${item.age} yrs")
            }
        }
    }
}

@Composable
public fun ThirdSecondaryScreen(item: GalleryItem) {
    Text(text = stringResource(id = R.string.detail_screen, item.name, item.age))
}