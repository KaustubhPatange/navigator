package com.kpstv.navigator.compose.sample

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kpstv.navigator.R
import com.kpstv.navigator.compose.sample.ui.GalleryItem
import com.kpstv.navigator.compose.sample.ui.Menu
import com.kpstv.navigator.compose.sample.ui.galleryItems
import com.kpstv.navigator.compose.sample.ui.theme.ComposeTestAppTheme
import com.kpstv.navigator.compose.*
import com.kpstv.navigator.compose.sample.ui.MenuItem
import kotlinx.parcelize.Parcelize

class MainActivity : ComponentActivity() {
    private lateinit var navigator: ComposeNavigator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = ComposeNavigator(this, savedInstanceState)
        setContent {
            ComposeTestAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    StartScreen(navigator = navigator, startRoute = StartRoute.First("Hello world"))
                }
            }
        }
    }
}

sealed class StartRoute : Parcelable {
    @Parcelize
    data class First(val data: String) : StartRoute(), Route
    @Parcelize
    data class Second(val data: String) : StartRoute(), Route
}

@Composable
fun StartScreen(navigator: ComposeNavigator, startRoute: StartRoute) {
    navigator.Setup(initial = startRoute) { controller, dest ->
        val onChanged: (screen: StartRoute) -> Unit = { value ->
            controller.navigateTo(value) {
                withAnimation {
                    enter = EnterAnimation.FadeIn
                    exit = ExitAnimation.FadeOut
                }
            }
        }
        when (dest) {
            is StartRoute.First -> FirstScreen(dest.data, onChanged)
            is StartRoute.Second -> SecondScreen()
        }
    }
}

sealed class FirstRoute : Parcelable {
    @Parcelize
    object Primary : FirstRoute(), Route
    @Parcelize
    object Third : FirstRoute(), Route
}

@Composable
fun FirstScreen(data: String, change: (StartRoute) -> Unit) {
    val navigator = findComposeNavigator()
    navigator.Setup(initial = FirstRoute.Primary as FirstRoute) { controller, dest ->
        when(dest) {
            is FirstRoute.Primary -> PrimaryFirst(data, change, { route ->
                controller.navigateTo(route) {
                    withAnimation {
                        enter = EnterAnimation.SlideInRight
                        exit = ExitAnimation.FadeOut
                    }
                }
            })
            is FirstRoute.Third -> {
                ThirdScreen()
            }
        }
    }
}

@Composable
fun PrimaryFirst(data: String, change: (StartRoute) -> Unit, change2: (FirstRoute) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("First Screen: $data", color = Color.Black)
        Button(
            onClick = { change.invoke(StartRoute.Second("String")) },
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text("Go to second screen")
        }
        Button(
            onClick = { change2.invoke(FirstRoute.Third) },
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text("Go to third screen")
        }
    }
}

@Composable
fun SecondScreen() {
    Column {
        // if the menu is moved inside the navigator scope then it'll too
        // undergo recomposition when destination is changed which looks
        // bad if animations are enabled.

        val destination = remember { mutableStateOf(MenuItem.Home as MenuItem) }
        val controller = remember { mutableStateOf<ComposeNavigator.Controller<MenuItem>?>(null) }

        findComposeNavigator().Setup(modifier = Modifier.weight(1f), initial = MenuItem.Home as MenuItem) { con, dest ->
            destination.value = dest
            controller.value = con

            Box(modifier = Modifier
                .weight(1f)
                .wrapContentSize(Alignment.TopStart)) {
                when (dest) {
                    MenuItem.Home -> Gallery()
                    else -> ReusableComponent(dest.toString())
                }
            }
        }
        Menu.Content(
            state = Menu.State(currentSelection = destination.value),
            onMenuItemClicked = { menuItem ->
                controller.value?.navigateTo(menuItem) {
                    if (menuItem != MenuItem.Home) {
                        singleTop = true
                    }
                    withAnimation {
                        when (destination.value) {
                            MenuItem.Home -> {
                                enter = EnterAnimation.SlideInRight
                                exit = ExitAnimation.SlideOutLeft
                            }
                            MenuItem.Settings -> {
                                enter = EnterAnimation.SlideInLeft
                                exit = ExitAnimation.SlideOutRight
                            }
                            MenuItem.Favourite -> {
                                if (menuItem == MenuItem.Home) {
                                    enter = EnterAnimation.SlideInLeft
                                    exit = ExitAnimation.SlideOutRight
                                } else {
                                    enter = EnterAnimation.SlideInRight
                                    exit = ExitAnimation.SlideOutLeft
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun Gallery() {
    val navigator = findComposeNavigator()
    navigator.Setup(initial = GalleryRoute.Primary as GalleryRoute) { controller, dest ->
        when(dest) {
            is GalleryRoute.Primary -> PrimaryGallery {
                controller.navigateTo(GalleryRoute.Detail(it)) {
                    withAnimation {
                        enter = EnterAnimation.FadeIn
                        exit = ExitAnimation.FadeOut
                    }
                }
            }
            is GalleryRoute.Detail -> GalleryDetail(dest.item)
        }
    }
}

sealed class GalleryRoute : Parcelable {
    @Parcelize
    object Primary : GalleryRoute(), Route
    @Parcelize
    data class Detail(val item: GalleryItem) : GalleryRoute(), Route
}

@Composable
fun PrimaryGallery(onItemSelected: (GalleryItem) -> Unit) {
    val state = rememberLazyListState() // FIXME: Nested navigation doesn't save list state across process death.
    Log.e("PrimaryGallery", "${state.firstVisibleItemIndex} - ${state.firstVisibleItemScrollOffset}")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val image = painterResource(R.drawable.placeholder)
        LazyColumn(state = state) {
            items(galleryItems) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { onItemSelected(item) })
                ) {
                    Row(modifier = Modifier.padding(all = 16.dp)) {
                        Box(modifier = Modifier.size(40.dp, 40.dp)) {
                            Image(image, null)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                item.name,
                                style = typography.subtitle1.copy(fontWeight = FontWeight.Bold)
                            )
                            Text("${item.age} yrs", style = typography.body1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryDetail(item: GalleryItem) {
    val image = painterResource(R.drawable.placeholder)
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        Image(image, null, modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.height(10.dp))
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(item.name, style = typography.h3)
            Text("${item.age} yrs")
        }
    }
}

@Composable
fun ReusableComponent(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text)
    }
}

@Composable
fun ThirdScreen() {
    findComposeNavigator().Setup(initial = ThirdRoute.Third1 as ThirdRoute) { controller, dest ->
        val onChanged: (ThirdRoute) -> Unit = { screen ->
            controller.navigateTo(screen) {
                withAnimation {
                    enter = EnterAnimation.ShrinkIn
                    exit = ExitAnimation.ShrinkOut
                }
            }
        }
        when(dest) {
            is ThirdRoute.Third1 -> ThirdScreen1(onChanged)
            is ThirdRoute.Third2 -> ThirdScreen2()
        }
    }
}

sealed class ThirdRoute : Parcelable {
    @Parcelize
    object Third1 : ThirdRoute(), Route
    @Parcelize
    object Third2 : ThirdRoute(), Route
}

@Composable
fun ThirdScreen1(change: (ThirdRoute) -> Unit) {
    val controller = findController<ThirdRoute>()
    val navigator = findComposeNavigator()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Third Screen 1\n\nBackpress is suppressed for this screen & can only be triggered by pressing the icon below.",
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center)
        Button(
            onClick = { change.invoke(ThirdRoute.Third2) },
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text("Go to third screen")
        }
        Spacer(modifier = Modifier.height(10.dp))
        IconButton(onClick = { controller.goBack() }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
        }
    }

    DisposableEffect(key1 = controller) {
        navigator.isBackPressedEnabled = false
        onDispose {
            navigator.isBackPressedEnabled = true
        }
    }
}

@Composable
fun ThirdScreen2() {
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