package com.kpstv.navigation.compose.internels

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.kpstv.navigation.compose.Route
import com.kpstv.navigation.compose.findComposeNavigator
import com.kpstv.navigation.compose.findNavController
import com.kpstv.navigation.compose.rememberNavController
import kotlinx.parcelize.Parcelize
import com.kpstv.navigation.compose.test.R
import kotlin.random.Random

public sealed class ViewModelRoute : Route {
    @Parcelize
    public data class TestViewModelInstances(private val noArg: String = "") : ViewModelRoute()

    public companion object : Route.Key<ViewModelRoute>
}

@Composable
public fun ViewModelScreen() {
    val context = LocalContext.current
    val owner = LocalSavedStateRegistryOwner.current
    val testViewModel = viewModel<TestViewModel>(factory = TestViewModel.factory(context, owner))

    val controller = rememberNavController<ViewModelRoute>()
    findComposeNavigator().Setup(key = ViewModelRoute.key, initial = ViewModelRoute.TestViewModelInstances(), controller = controller) { dest ->
        when(dest) {
            is ViewModelRoute.TestViewModelInstances -> TestViewModelInstancesScreen(testViewModel, owner)
        }
    }

}

@Composable
public fun TestViewModelInstancesScreen(comparer: TestViewModel, parentOwner: SavedStateRegistryOwner) {
    val controller = findNavController(key = ViewModelRoute.key)
    val context = LocalContext.current
    val owner = LocalSavedStateRegistryOwner.current

    val testViewModel = viewModel<TestViewModel>(factory = TestViewModel.factory(context, owner))
    val sameTestViewModel = viewModel<TestViewModel>(factory = TestViewModel.factory(context, owner))
    val differentViewModel = viewModel<TestViewModel>(key = "different", factory = TestViewModel.factory(context, owner))

    if (testViewModel.data == "NOT_SET") {
        testViewModel.set(Random.nextDouble().toString())
    }
    if (differentViewModel.data == "NOT_SET") {
        differentViewModel.set(Random.nextDouble().toString())
    }

    // check for references
    assert(comparer !== testViewModel)
    assert(testViewModel === sameTestViewModel)
    assert(testViewModel !== differentViewModel)
    assert(owner !== parentOwner) // makes sure the SavedStateRegistryOwners are different.

    Column {
        Button(onClick = { controller.goBack() }) {
            Text(text = stringResource(R.string.go_back))
        }
    }
}

public class TestViewModel constructor(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    public var data: String = "NOT_SET"
        private set

    init {
        savedStateHandle.get<String>("data")?.also { data = it }
    }

    public fun set(data: String) {
        savedStateHandle.set("data", data)
        this.data = data
    }

    public companion object {
        public fun factory(context: Context, savedStateRegistryOwner: SavedStateRegistryOwner): SavedStateViewModelFactory = SavedStateViewModelFactory(
            context.applicationContext as Application,
            savedStateRegistryOwner
        )
    }
}

