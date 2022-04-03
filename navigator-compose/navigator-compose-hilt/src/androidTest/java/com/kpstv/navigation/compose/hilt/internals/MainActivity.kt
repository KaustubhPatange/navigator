package com.kpstv.navigation.compose.hilt.internals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kpstv.navigation.compose.ComposeNavigator
import com.kpstv.navigation.compose.Route
import com.kpstv.navigation.compose.findNavController
import com.kpstv.navigation.compose.hilt.hiltViewModel
import com.kpstv.navigation.compose.rememberNavController
import kotlinx.parcelize.Parcelize
import com.kpstv.navigation.compose.hilt.test.R
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
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
    public data class Main(private val noArg: String = "") : StartRoute()
    @Parcelize
    public data class HiltRoute(private val noArg: String = "") : StartRoute()

    public companion object Key : Route.Key<StartRoute>
}

@Composable
public fun StartScreen(navigator: ComposeNavigator) {
    val controller = rememberNavController<StartRoute>()
    navigator.Setup(key = StartRoute.key, initial = StartRoute.Main(), controller = controller) { dest ->
        when(dest) {
            is StartRoute.Main -> MainScreen(goToHiltRoute = {
                controller.navigateTo(StartRoute.HiltRoute())
            })
            is StartRoute.HiltRoute -> HiltScreen()
        }
    }
}

@Composable
private fun MainScreen(goToHiltRoute: () -> Unit) {
    Column {
        Button(onClick = goToHiltRoute) {
            Text(text = stringResource(R.string.go_to_hilt_screen))
        }
    }
}

@Composable
private fun HiltScreen() {
    val controller = findNavController(key = StartRoute.key)

    // This itself will check if hilt viewmodel can be created or not.
    val testViewModel = hiltViewModel<TestHiltViewModel>()

    val data = testViewModel.data.observeAsState(initial = "Hello world")

    Column {
        Text(text = data.value)
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { controller.goBack() }) {
            Text(text = stringResource(R.string.go_back))
        }
    }
}

@HiltViewModel
internal class TestHiltViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val data = savedStateHandle.getLiveData("data", "Hello world")
}