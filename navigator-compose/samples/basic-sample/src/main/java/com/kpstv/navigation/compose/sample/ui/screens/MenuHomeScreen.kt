package com.kpstv.navigation.compose.sample.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kpstv.navigation.R
import com.kpstv.navigation.compose.Fade
import com.kpstv.navigation.compose.Route
import com.kpstv.navigation.compose.findComposeNavigator
import com.kpstv.navigation.compose.rememberNavController
import com.kpstv.navigation.compose.sample.ui.componenets.CommonItem
import com.kpstv.navigation.compose.sample.ui.componenets.sampleCommonItems
import kotlinx.parcelize.Parcelize

private sealed interface MenuHomeRoute : Route {
    @Immutable @Parcelize
    data class Primary(private val noArg: String = "") : MenuHomeRoute
    @Immutable @Parcelize
    data class Detail(val item: CommonItem) : MenuHomeRoute
    companion object {
        val key = MenuHomeRoute::class
    }
}

@Composable
fun MenuHomeScreen() {
    val navigator = findComposeNavigator()
    val galleryController = rememberNavController<MenuHomeRoute>()
    navigator.Setup(key = MenuHomeRoute.key, initial = MenuHomeRoute.Primary(), controller = galleryController) { dest ->
        when (dest) {
            is MenuHomeRoute.Primary -> MenuHomePrimaryScreen {
                galleryController.navigateTo(MenuHomeRoute.Detail(it)) {
                    withAnimation {
                        target = Fade
                        current = Fade
                    }
                }
            }
            is MenuHomeRoute.Detail -> MenuHomeDetailScreen(dest.item)
        }
    }
}

// Also used in Menu's Favourite Screen
@Composable
fun MenuHomePrimaryScreen(onItemSelected: (CommonItem) -> Unit) {
    val state = rememberLazyListState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val image = painterResource(R.drawable.placeholder)
        LazyColumn(state = state) {
            items(sampleCommonItems) { item ->
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
                                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold)
                            )
                            Text("${item.age} yrs", style = MaterialTheme.typography.body1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuHomeDetailScreen(item: CommonItem) {
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
            Text(item.name, style = MaterialTheme.typography.h3)
            Text("${item.age} yrs")
        }
    }
}
