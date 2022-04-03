package com.kpstv.navigation.compose.hilt

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.kpstv.navigation.compose.LifecycleController
import com.kpstv.navigation.compose.Route
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap
import javax.inject.Inject

// Solution is highly inspired from https://github.com/adrielcafe/voyager/tree/main/voyager-hilt

/**
 * Provide an instance of ViewModel annotated with @[HiltViewModel] scoped to the destination [Route]'s
 * [LifecycleController] which is a [ViewModelStoreOwner].
 *
 * This ViewModel will be tied to the current destination & will be cleared when the destination is removed
 * from the backstack.
 */
@Composable
public inline fun <reified T : ViewModel> hiltViewModel(viewModelProviderFactory: ViewModelProvider.Factory? = null) : T {
    val context = LocalContext.current
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    val owner = LocalSavedStateRegistryOwner.current

    return remember(key1 = T::class) {
        val activity = context.findActivity()
        val factory = NavigatorHiltViewModelFactories.getFactory(
            activity = activity,
            owner = owner,
            delegateFactory = viewModelProviderFactory
        )
        val provider = ViewModelProvider(viewModelStoreOwner ?: activity, factory)
        provider[T::class.java]
    }
}

/**
 * Similar to [DefaultViewModelFactories] but an explicit [SavedStateRegistryOwner] as parameter.
 */
public object NavigatorHiltViewModelFactories {
    public fun getFactory(
        activity: ComponentActivity,
        owner: SavedStateRegistryOwner,
        delegateFactory: ViewModelProvider.Factory?
    ): ViewModelProvider.Factory {
        return EntryPoints.get(activity, ViewModelFactoryEntryPoint::class.java)
            .internalViewModelFactory()
            .fromActivity(activity, owner, delegateFactory)
    }

    internal class InternalViewModelFactory @Inject internal constructor(
        private val application: Application,
        @HiltViewModelMap.KeySet private val keySet: Set<String>,
        private val viewModelComponentBuilder: ViewModelComponentBuilder
    ) {
        fun fromActivity(
            activity: ComponentActivity,
            owner: SavedStateRegistryOwner,
            delegateFactory: ViewModelProvider.Factory?
        ): ViewModelProvider.Factory {
            val defaultArgs = activity.intent?.extras
            val delegate = delegateFactory ?: SavedStateViewModelFactory(application, owner, defaultArgs)
            return HiltViewModelFactory(owner, defaultArgs, keySet, delegate, viewModelComponentBuilder)
        }
    }

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    internal interface ViewModelFactoryEntryPoint {
        fun internalViewModelFactory(): InternalViewModelFactory
    }
}

@PublishedApi
internal fun Context.findActivity(): ComponentActivity {
    if (this is ComponentActivity) return this
    if (this is ContextWrapper) {
        val baseContext = this.baseContext
        if (baseContext is ComponentActivity) return baseContext
        return baseContext.findActivity()
    }
    throw NotImplementedError("Could not find activity from $this.")
}