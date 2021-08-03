package com.kpstv.navigation.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kpstv.navigation.R
import com.kpstv.navigation.compose.sample.ui.GalleryItem
import com.kpstv.navigation.compose.sample.ui.Menu
import com.kpstv.navigation.compose.sample.ui.galleryItems
import com.kpstv.navigation.compose.sample.ui.theme.ComposeTestAppTheme
import com.kpstv.navigation.compose.*
import com.kpstv.navigation.compose.sample.ui.MenuItem
import kotlinx.parcelize.Parcelize

class MainActivity : ComponentActivity() {
    private lateinit var navigator: ComposeNavigator
    private lateinit var controller: ComposeNavigator.Controller<StartRoute>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = ComposeNavigator.with(this, savedInstanceState)
            .registerTransitions(SlideWithFadeRightTransition, SlideWithFadeLeftTransition)
            .initialize()

        setContent {
            controller = rememberController()
            ComposeTestAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    StartScreen(
                        navigator = navigator,
                        controller = controller,
                        startRoute = StartRoute.First("Hello world")
                    )
                }
            }
        }
    }

   /* override fun onBackPressed() {
        // Show the dialog when navigation history contains 1 item
        // & close the dialog when dialog history contains the close dialog.
        if (controller.getAllHistory().count() > 1 ||
            controller.getAllDialogHistory().lastOrNull() is CloseDialog ||
            navigator.suppressBackPress
        ) {
            super.onBackPressed()
        } else {
            controller.showDialog(CloseDialog)
        }
    }*/
}

sealed interface StartRoute : Route {
    @Immutable
    @Parcelize
    data class First(val data: String) : StartRoute

    @Immutable
    @Parcelize
    data class Second(private val noArgPlaceholder: String = "") : StartRoute
    companion object {
        val key = StartRoute::class
    }
}

@Parcelize
object CloseDialog : DialogRoute

@Composable
fun StartScreen(
    navigator: ComposeNavigator,
    controller: ComposeNavigator.Controller<StartRoute>,
    startRoute: StartRoute
) {
    val activity = LocalContext.current as MainActivity

    navigator.Setup(
        key = StartRoute.key,
        initial = startRoute,
        controller = controller
    ) { _, dest ->
        val onChanged: (screen: StartRoute) -> Unit = { value ->
            controller.navigateTo(value) {
                withAnimation {
                    target = Fade
                    current = Fade
                }
            }
        }
        when (dest) {
            is StartRoute.First -> FirstScreen(dest.data, onChanged)
            is StartRoute.Second -> SecondScreen()
        }

        controller.CreateDialog(key = CloseDialog::class) { _, dismiss ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
                    .padding(15.dp)
            ) {
                Text("Do you want to close the app?", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = dismiss) {
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

sealed interface FirstRoute : Route {
    @Immutable
    @Parcelize
    data class Primary(private val noArg: String = "") : FirstRoute

    @Immutable
    @Parcelize
    data class Third(private val noArg: String = "") : FirstRoute
    companion object {
        val key = FirstRoute::class
    }
}

@Composable
fun FirstScreen(data: String, change: (StartRoute) -> Unit) {
    val navigator = findComposeNavigator()
    navigator.Setup(key = FirstRoute.key, initial = FirstRoute.Primary()) { controller, dest ->
        when (dest) {
            is FirstRoute.Primary -> PrimaryFirst(data, change) { route ->
                controller.navigateTo(route) {
                    withAnimation {
                        target = SlideRight
                        current = Fade
                    }
                }
            }
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
            onClick = { change.invoke(StartRoute.Second()) },
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text("Go to second screen")
        }
        Button(
            onClick = { change2.invoke(FirstRoute.Third()) },
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

        val controller = rememberController<MenuItem>()
        val destination = remember { mutableStateOf(MenuItem.Home() as MenuItem) }

        findComposeNavigator().Setup(
            modifier = Modifier.weight(1f),
            controller = controller,
            key = MenuItem.key,
            initial = MenuItem.Home()
        ) { _, dest ->
            destination.value = dest

            Box(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentSize(Alignment.TopStart)
            ) {
                when (dest) {
                    is MenuItem.Home -> Gallery()
                    is MenuItem.Favourite -> FavouriteMenuItem()
                    else -> ReusableComponent(dest.toString())
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

@Composable
fun Gallery() {
    val navigator = findComposeNavigator()
    navigator.Setup(key = GalleryRoute.key, initial = GalleryRoute.Primary()) { controller, dest ->
        when (dest) {
            is GalleryRoute.Primary -> PrimaryGallery {
                controller.navigateTo(GalleryRoute.Detail(it)) {
                    withAnimation {
                        target = Fade
                        current = Fade
                    }
                }
            }
            is GalleryRoute.Detail -> GalleryDetail(dest.item)
        }
    }
}

sealed interface GalleryRoute : Route {
    @Immutable
    @Parcelize
    data class Primary(private val noArg: String = "") : GalleryRoute

    @Immutable
    @Parcelize
    data class Detail(val item: GalleryItem) : GalleryRoute
    companion object {
        val key = GalleryRoute::class
    }
}

@Composable
fun PrimaryGallery(onItemSelected: (GalleryItem) -> Unit) {
    val state = rememberLazyListState()
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            image, null, modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(item.name, style = typography.h3)
            Text("${item.age} yrs")
        }
    }
}


@Parcelize
object FirstDialog : DialogRoute

@Parcelize
data class ListDialog(val item: GalleryItem) : DialogRoute

@Composable
fun FavouriteMenuItem() {
    val controller = findController(key = MenuItem.key)

    /* try uncommenting the below line to enable dialog overlays */
//    controller.enableDialogOverlay = true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { controller.showDialog(FirstDialog) }) {
            Text("Show a dialog")
        }
    }

    controller.CreateDialog(key = FirstDialog::class) { _, dismiss ->
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
                PrimaryGallery {
                    controller.showDialog(ListDialog(it))
                }
            }
            Divider()
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = dismiss) {
                Text("Close")
            }
        }
    }

    controller.CreateDialog(key = ListDialog::class) { dialogRoute, dismiss ->
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
            Button(onClick = dismiss) {
                Text(text = "Go Back")
            }
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
    findComposeNavigator().Setup(
        key = ThirdRoute.key,
        initial = ThirdRoute.Third1() as ThirdRoute
    ) { controller, dest ->
        val onChanged: (ThirdRoute) -> Unit = { screen ->
            controller.navigateTo(screen) {
                withAnimation {
                    target = SlideWithFadeRight
                    current = SlideWithFadeLeft
                }
            }
        }
        when (dest) {
            is ThirdRoute.Third1 -> ThirdScreen1(onChanged)
            is ThirdRoute.Third2 -> ThirdScreen2()
        }
    }
}

sealed interface ThirdRoute : Route {
    @Immutable
    @Parcelize
    data class Third1(private val noArg: String = "") : ThirdRoute

    @Immutable
    @Parcelize
    data class Third2(private val noArg: String = "") : ThirdRoute
    companion object {
        val key = ThirdRoute::class
    }
}

@Composable
fun ThirdScreen1(change: (ThirdRoute) -> Unit) {
    val controller = findController(ThirdRoute.key)
    val navigator = findComposeNavigator()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Third Screen 1\n\nBack navigation is suppressed for this screen & can only be triggered by pressing the icon below.\n\nBehold! This button has a custom transition defined.",
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = { change.invoke(ThirdRoute.Third2()) },
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
        navigator.suppressBackPress = true
        onDispose {
            navigator.suppressBackPress = false
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