package com.kpstv.navigation.compose.sample.ui.componenets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kpstv.navigation.compose.Route
import kotlinx.parcelize.Parcelize

sealed class MenuItem : Route {
    @Immutable @Parcelize
    data class Home(private val noArg: String = "") : MenuItem() {
        override fun toString() = "Home"
    }
    @Immutable @Parcelize
    data class Favourite(private val noArg: String = "") : MenuItem() {
        override fun toString() = "Favourite"
    }
    @Immutable @Parcelize
    data class Settings(private val noArg: String = "") : MenuItem() {
        override fun toString() = "Settings"
        companion object { val key = Settings::class }
    }
    companion object {
        val key = MenuItem::class
    }
}

interface Menu {

    data class State(
        val menuItems: List<MenuItem> = listOf(
            MenuItem.Home(),
            MenuItem.Favourite(),
            MenuItem.Settings()
        ),
        val currentSelection: MenuItem
    )

    companion object {
        @Composable
        fun Content(modifier: Modifier = Modifier, state: State, onMenuItemClicked: (MenuItem) -> Unit) {
            Row(modifier) {
                state.menuItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        MenuItem(
                            item,
                            item == state.currentSelection
                        ) {
                            onMenuItemClicked(it)
                        }
                    }
                }
            }
        }

        @Composable
        fun MenuItem(item: MenuItem, isSelected: Boolean, onClick: (MenuItem) -> Unit) {
            val color = MaterialTheme.colors

            Surface(
                color = if (isSelected) color.secondary else color.surface,
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            ) {
                Column(
                    modifier = Modifier.clickable(onClick = { onClick.invoke(item) })
                ) {
                    Icon(contentDescription = "icon", modifier= Modifier.fillMaxWidth().padding(top = 8.dp),
                        imageVector = when(item) {
                            is MenuItem.Home -> Icons.Default.Home
                            is MenuItem.Favourite -> Icons.Default.Favorite
                            is MenuItem.Settings -> Icons.Default.Settings
                        })
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(3.dp),
                        text = AnnotatedString(
                            text = item.toString(),
                            paragraphStyle = ParagraphStyle(
                                textAlign = TextAlign.Center
                            )
                        ),
                        style = MaterialTheme.typography.body2.copy(
                            color = if (isSelected) color.onSecondary else color.onSurface
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewMenu() {
    Menu.Content(state = Menu.State(currentSelection = MenuItem.Home()), onMenuItemClicked = {})
}
