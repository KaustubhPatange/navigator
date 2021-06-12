package com.kpstv.navigator.compose.sample.ui

import android.os.Parcelable
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MenuItem : Parcelable {
    Home, Favourite, Settings
}

interface Menu {

    data class State(
        val menuItems: List<MenuItem> = listOf(MenuItem.Home, MenuItem.Favourite, MenuItem.Settings),
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
        private fun MenuItem(item: MenuItem, isSelected: Boolean, onClick: (MenuItem) -> Unit) {
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
                            MenuItem.Home -> Icons.Default.Home
                            MenuItem.Favourite -> Icons.Default.Favorite
                            MenuItem.Settings -> Icons.Default.Settings
                        })
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(3.dp),
                        text = AnnotatedString(
                            text = item.name,
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
    Menu.Content(state = Menu.State(currentSelection = MenuItem.Home), onMenuItemClicked = {})
}
