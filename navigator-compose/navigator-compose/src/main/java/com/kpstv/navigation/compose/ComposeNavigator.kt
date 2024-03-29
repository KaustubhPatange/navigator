@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.kpstv.navigation.compose

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.*
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.kpstv.navigation.compose.Route.Key
import kotlinx.coroutines.flow.*
import kotlinx.parcelize.Parcelize
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Navigation root key to be provided during `ComposeNavigator.Setup(...)` to make current navigation
 * unique in the backstack &amp; to enforce correct typesafe navigation.
 *
 * This key then can be used to retrieve an instance of [ComposeNavigator.Controller<T>] to manage
 * navigation within this [Route].
 *
 * @see Route
 * @see Route.Key
 * @see ComposeNavigator.Controller
 */
public typealias RouteKey<T> = KClass<out Route.Key<T>>

/**
 * If marked on a function or a property then it means that the function or property is unstable &amp;
 * should be carefully use in production code as it's behavior might not be stable.
 *
 * In most cases, these behaviors are tested in the test suite to fulfill some common use-case.
 */
@RequiresOptIn("The method might be unstable and may not work correct in some edge cases.", RequiresOptIn.Level.WARNING)
public annotation class UnstableNavigatorApi

/**
 * Find the [ComposeNavigator] provided by the nearest [CompositionLocalProvider].
 */
@Composable
public fun findComposeNavigator() : ComposeNavigator = LocalNavigator.current

/**
 * Find & remember an instance of [ComposeNavigator.Controller] provided by the nearest [CompositionLocalProvider] for a Navigation root key.
 *
 * @see RouteKey
 * @see ComposeNavigator.Controller
 */
@Composable
public fun <T : Route> findNavController(key: RouteKey<T>): ComposeNavigator.Controller<T> {
    val localNavigator = LocalNavigator.current
    return rememberComposable {
        localNavigator.getLocalController(key).current ?: throw Exception("Could not find Controller for key \"${key.qualifiedName}\". Did you forgot to call \"Navigator.Setup\"?")
    }
}

/**
 * Create & remember the instance of the [ComposeNavigator.Controller] that will be used for
 * navigation within the [Route] of type [T].
 *
 * ```
 * val controller = rememberNavController()
 * navigator.Setup(... , controller = controller) { _, dest ->
 *  ...
 * }
 * ```
 *
 * @see ComposeNavigator.Controller
 */
@Composable
public fun <T : Route> rememberNavController(): ComposeNavigator.Controller<T> {
    return remember { ComposeNavigator.Controller() }
}

/**
 * Class must implement this interface to identify as navigation root. Each navigation
 * root should specify a [Key] to identify this route as a unique route in the backstack.
 *
 * A route can then declare destinations as data classes with parameters which will become
 * destination arguments as shown below.
 *
 * ```
 * sealed class MyRoute : Route {  // <-- Navigation root
 *    @Parcelize @Immutable
 *    data class First(val arg: String) : MyRoute()   // <-- Destination of MyRoute (Navigation root)
 *
 *    companion object Key : Route.Key<MyRoute> // <-- Navigation root key
 * }
 * ```
 *
 * @see Route.Key
 */
public interface Route : Parcelable {

    /**
     * A [LifecycleController] associated with this [Route].
     *
     * Each destination will inherit this controller that'll help to manage lifecycle related
     * things scoped to this [Route].
     *
     * @see LifecycleController
     */
    public val lifecycleController: LifecycleController get() = LifecycleController.get(this)

    /**
     * A [key] for Route [T] to make this navigation unique in the backstack.
     *
     * ```
     * sealed class MyRoute : Route {
     *    ...
     *    companion object Key : Key<MyRoute> // <-- Key implementation
     * }
     * ```
     * or
     * ```
     * class MyRouteKey { companion object : Route.Key<MyRoute> }
     * ```
     *
     * @see Route
     */
    public interface Key<T : Route> {
        public val key: KClass<out Key<T>> get() = this::class
    }
}

/**
 * A class which is tied to every [Route]'s destination that itself is [SavedStateRegistryOwner] &
 * [ViewModelStoreOwner].
 *
 * This helps to incorporate scoped [ViewModel]s within a particular destination. Since it also implements
 * methods to handle [SavedStateRegistry], we can save state in a Bundle which will be tied to the Lifecycle
 * of the associated destination for eg: [SavedStateHandle]'s data will be properly restored on process death.
 * The same also carries for any [rememberSaveable]s used within the scope of the destination. Saving state
 * happens when the activity's `onSaveInstanceState()` is called & restoring happens when the destination is
 * recreated from the restored backstack.
 *
 * ViewModel instances on configuration change are same which is why we keep static references to this class
 * in [LifecycleControllerStore]. The mechanism is very similar to how fragment retains ViewModel instances
 * when it is recreated due to configuration change.
 *
 * The class does implement a [LifecycleOwner] which we might need to clear [ViewModelStore] on `onDestroy()`
 * or to correctly trigger saving states through [SavedStateRegistry]. One can listen to the lifecycle changes
 * to the destination [Route] by adding a [LifecycleObserver], but to still give you a clarity how lifecycle
 * event affects during navigation here is an example.
 *
 * ```
 * navigator.navigateTo(A)
 * // A -> onCreate -> onStart -> onResume
 * // backstack = [A]
 *
 * navigator.navigateTo(B)
 * // A -> onPause -> onStop
 * // B -> onCreate -> onStart -> onResume
 * // backstack = [A, B]
 *
 * navigator.goBack()
 * // B -> onPause -> onStop -> onDestroy
 * // A -> onStart -> onResume
 * // backstack = [A]
 * ```
 *
 */
public class LifecycleController private constructor() : SavedStateRegistryOwner, ViewModelStoreOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val viewModelStore = ViewModelStore()

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
    override fun getSavedStateRegistry(): SavedStateRegistry = savedStateRegistryController.savedStateRegistry
    override fun getViewModelStore(): ViewModelStore = viewModelStore

    internal val providers: List<ProvidedValue<*>> get() = listOf(
        LocalViewModelStoreOwner provides this,
        LocalSavedStateRegistryOwner provides this,
    )

    internal fun performSave(outState: Bundle) {
        savedStateRegistryController.performSave(outState)
    }

    internal fun performRestore(savedInstanceState: Bundle?) {
        if (!isRestored()) {
            savedStateRegistryController.performRestore(savedInstanceState)
        }
    }

    internal fun isRestored(): Boolean = savedStateRegistryController.savedStateRegistry.isRestored

    internal fun clearViewModelStore() {
        viewModelStore.clear()
    }

    internal fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }

    internal companion object {
        fun get(screen: Route) : LifecycleController {
            return LifecycleControllerStore.getOrPut(screen) { LifecycleController() }
        }
    }
}

/**
 * A singleton to store instance of [LifecycleController] which is associated to a
 * particular [Route]'s destination.
 *
 * This will help us to retain ViewModelStore on configuration change.
 */
internal object LifecycleControllerStore {
    private val map = ConcurrentHashMap<Route, LifecycleController>()

    internal fun getOrPut(screen: Route, default: () -> LifecycleController): LifecycleController {
        return map.getOrPut(screen, default)
    }
    internal fun remove(screen: Route) {
        map.remove(screen)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    fun getSnapshot(): Map<Route, LifecycleController> = map.toMap()
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun clear() {
        map.clear()
    }
}

/**
 * Destination must implement this interface to identify a Route as Dialog.
 *
 * @see ComposeNavigator.Controller.CreateDialog
 */
public interface DialogRoute : Route

/**
 * Set of options that can be optionally specified when navigating to new destination through
 * [ComposeNavigator.Controller.navigateTo].
 *
 * These includes [singleTop], [popUpTo] & [withAnimation].
 */
public data class NavOptions<T : Route>(
    /**
     * Ensures that there will be only once instance of this destination in the backstack.
     *
     * This works different compared to the traditional Fragment system where the
     * backstack will be popped till the instance is found in the `FragmentTransaction`.
     * Here it'll directly remove all of the instances from history.
     */
    public var singleTop: Boolean = false,
    internal var popOptions: PopUpOptions<T>? = null,
    internal var animationOptions: NavAnimation = NavAnimation()
) {
    /**
     * @param target Set the transition for the target destination composable.
     * @param current Set the transition for the current destination composable.
     */
    @Parcelize
    public data class NavAnimation(
        var target: TransitionKey = None,
        var current: TransitionKey = None
    ) : Parcelable

    /**
     * @param inclusive Include this destination to be popped as well.
     * @param all There could be situation where multiple destinations could be present on the backstack,
     *            this will recursively pop till the first one in the backstack. Otherwise, the last
     *            added one will be chosen.
     */
    public data class PopUpOptions<T: Route>(internal var dest: KClass<out T>, var inclusive: Boolean = false, var all: Boolean = false)

    /**
     * Pop up to a destination. Additional parameters can be set through [options] DSL.
     *
     * @see PopUpOptions
     */
    public fun popUpTo(destKey: KClass<out T>, options: PopUpOptions<T>.() -> Unit = {}) {
        popOptions = PopUpOptions(destKey).apply(options)
    }

    /**
     * Customize transition for this navigation. You have to specify the transition for
     * target & current destination.
     *
     * Suppose "A" is the target destination & "B" is the current destination. So when
     * navigating from A -> B, forward transition will be played on "A" and backward
     * transition will be played on "B" & vice-versa.
     *
     * @see NavAnimation
     */
    public fun withAnimation(options: NavAnimation.() -> Unit = {}) {
        animationOptions = NavAnimation().apply(options)
    }
}

/**
 * A key to uniquely identify this transition that can be used in conjunction with [NavOptions.withAnimation]
 * to specify animations for navigating to a new destination.
 *
 * @see SlideLeft
 * @see SlideRight
 */
@Parcelize
@JvmInline
public value class TransitionKey(internal val key: String) : Parcelable // "class" (but inlined) so that IDE can show appropriate suggestions.

/**
 * Interface to define custom transition.
 *
 * - [key] - A unique string so that [ComposeNavigator] can identify this transition upon use.
 * - [forwardTransition] - Define the forward transition that'll be used when navigating to the target destination.
 * - [backwardTransition] - Define the backward transition that'll be used when navigating from the target
 *                          destination to the previous one (usually on back press).
 */
public abstract class NavigatorTransition {
    public abstract val key: TransitionKey
    public abstract val forwardTransition: ComposeTransition
    public abstract val backwardTransition: ComposeTransition

    public open val animationSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 300)
}

/**
 * Transition are usually implemented by customizing the [Modifier.graphicsLayer].
 *
 * You will receive a "progress" (float) argument that goes from 0f -> 1f i.e the lifetime of the animation
 * which you can use to modify alpha, translation on the modifier.
 *
 * You will also receive "width" & "height" of the container in which this navigation is hosted.
 */
public fun interface ComposeTransition {
    public fun invoke(modifier: Modifier, width: Int, height: Int, progress: Float): Modifier
}

/**
 * A navigator for managing pure composable navigation in Jetpack Compose.
 */
public class ComposeNavigator private constructor(private val activity: ComponentActivity, savedInstanceState: Bundle?) {

    public companion object {
        private const val NAVIGATOR_SAVED_STATE_BUNDLE_KEY = "compose_navigator:saved_state:"
        private const val HISTORY_SAVED_STATE = "compose_navigator:state:"
        private const val NAVIGATOR_SAVED_STATE_SUFFIX = "_compose_navigator"

        /**
         * Creates a new builder for [ComposeNavigator].
         */
        public fun with(activity: ComponentActivity, savedInstanceState: Bundle?) : Builder = Builder(activity, savedInstanceState)
    }

    public class Builder internal constructor(activity: ComponentActivity, savedInstanceState: Bundle?) {
        private val navigator = ComposeNavigator(activity, savedInstanceState)

        init {
            // Register built-in transitions
            registerTransitions(NoneTransition, FadeTransition, SlideRightTransition, SlideLeftTransition)
        }

        /**
         * This will disable Navigator's internal back press logic which you then have to manually
         * handle in Activity's `onBackPressed()`.
         */
        public fun disableDefaultBackPressLogic(): Builder {
            navigator.backPressHandler.remove()
            return this
        }

        /**
         * This will disable saving of all ComposeNavigator's internal state along with destination
         * states during [activity]'s `onSaveStateInstance()`.
         */
        public fun disableOnSaveStateInstance(): Builder {
            (navigator.activity.applicationContext as Application).unregisterActivityLifecycleCallbacks(navigator.activityLifecycleCallbacks)
            return this
        }

        /**
         * Register custom transitions. This will allow you to set custom transition when
         * navigating to target destination.
         */
        public fun registerTransitions(vararg transitions: NavigatorTransition): Builder {
            val list = (navigator.navigatorTransitions + transitions).distinct()
            navigator.navigatorTransitions.clear()
            navigator.navigatorTransitions.addAll(list)
            return this
        }

        /**
         * Returns the configured instance of [ComposeNavigator].
         */
        public fun initialize() : ComposeNavigator = navigator
    }

    /**
     * [DialogScope] provides functions that are convenient and only applicable in the context of
     * [Controller.CreateDialog] such as [dismiss], [dialogRoute] & a [dialogNavigator].
     */
    public class DialogScope<T : DialogRoute> internal constructor(public val dialogRoute: T, private val handleOnDismissRequest: () -> Boolean, private val forceCloseDialog: () -> DialogRoute?) {
        internal var navigator: ComposeNavigator? = null

        private var savedInstanceState: Bundle? = null

        /**
         * A [ComposeNavigator] that should be used to `Setup` navigation inside this dialog.
         */
        public val dialogNavigator: ComposeNavigator
            @Composable
            get() {
                if (navigator == null) {
                    val activityNavigator = findComposeNavigator()
                    val activity = LocalContext.current.findActivity()
                    navigator = with(activity, savedInstanceState)
                        .disableDefaultBackPressLogic()
                        .disableOnSaveStateInstance()
                        .registerTransitions(*activityNavigator.navigatorTransitions.toTypedArray()) // register all transitions of activity's ComposeNavigator
                        .initialize()
                }
                return navigator!!
            }

        /**
         * Go back in [dialogNavigator] & return the route.
         */
        public fun goBack(): Route? {
            return navigator?.goBack()
        }

        /**
         * Dismisses the dialog.
         *
         * This will first call [handleOnDismissRequest] to determine whether the dismiss request
         * is handled or not. If `true` then it will return null otherwise proceeds to close the dialog.
         *
         * The default implementation of [handleOnDismissRequest] in [Controller.CreateDialog] returns false.
         *
         * @see Controller.CreateDialog
         */
        public fun dismiss(): DialogRoute? {
            if (!handleOnDismissRequest()) return forceCloseDialog()
            return null
        }

        internal fun saveState(outState: Bundle) {
            navigator?.onSaveInstanceState(outState)
        }

        internal fun restoreState(bundle: Bundle?) {
            savedInstanceState = bundle
        }
    }

    /**
     * [associateKey] A parent key for this [key]. This means [associateKey] has setup navigation for this [key].
     */
    internal class History<T : Route> internal constructor(internal val key: RouteKey<T>, internal val associateRoute: Route?, internal val initial: T) {
        private companion object {
            private const val LAST_REMOVED_ITEM_KEY = "history:last_item:key"
            private const val LAST_REMOVED_ITEM_ANIMATION = "history:last_item:animation"

            private const val BACKSTACK_RECORDS = "history:backstack_records"
            private const val BACKSTACK_LAST_ITEM = "history:last_item"
        }
        internal enum class NavType { Forward, Backward }

        internal class DialogHistory internal constructor() {

            private var savedInstanceState: Bundle? = null
            private val dialogScopes = arrayListOf<DialogScope<*>>()

            internal fun<T : DialogRoute> createDialogScope(route: T, handleOnDismissRequest: () -> Boolean): DialogScope<T> {
                val scope = DialogScope(route, handleOnDismissRequest = handleOnDismissRequest, forceCloseDialog = { remove(route::class) })

                // restore state
                val bundleKey = scope.generateScopeKey()
                savedInstanceState?.getBundle(bundleKey)?.let { state ->
                    scope.restoreState(state)
                }
                savedInstanceState?.remove(bundleKey)

                // add the scope
                dialogScopes.add(scope)
                return scope
            }

            private val backStack = mutableStateListOf<DialogRoute>()

            internal fun getScopes(): List<DialogScope<*>> = dialogScopes.toList()
            internal fun isEmpty(): Boolean = backStack.isEmpty()
            internal fun add(route: DialogRoute) { // when added the dialog will be visible.
                backStack.add(route)
            }
            internal fun remove(route: KClass<out DialogRoute>): DialogRoute? {
                val last = backStack.findLast { it::class == route }
                last?.let { lastRoute ->
                    dialogScopes.removeAll { it.dialogRoute::class == route }
                    backStack.remove(lastRoute)
                    return last
                }
                return null
            }
            internal fun peek(): DialogRoute? = backStack.lastOrNull()
            internal fun pop(): Route? {
                if (!isEmpty()) {
                    val peek = peek()
                    val scope = dialogScopes.findLast { it.dialogRoute === peek }
                    scope?.goBack() ?: return scope?.dismiss() // eventually calls remove(peek::class)
                }
                return null
            }
            internal fun get(): List<DialogRoute> = backStack
            internal fun get(key: KClass<out DialogRoute>): DialogRoute? = backStack.findLast { it::class == key }
            internal fun count() = backStack.size
            internal fun clear() {
                while (pop() != null);
            }

            internal fun saveState(outState: Bundle) {
                outState.putParcelableArrayList(SAVESTATE_BACKSTACK, ArrayList(backStack))
                dialogScopes.forEach { scope ->
                    val bundle = Bundle()
                    scope.saveState(bundle)
                    outState.putBundle(scope.generateScopeKey(), bundle)
                }
            }
            internal fun restoreState(bundle: Bundle?) {
                val backStackArrayList = bundle?.getParcelableArrayList<DialogRoute>(SAVESTATE_BACKSTACK) ?: return
                backStack.addAll(backStackArrayList)
                bundle.remove(SAVESTATE_BACKSTACK) // restore once then clear

                // scopes are automatically recreated during config so no action.

                // we will save the bundle to lazily restore Dialog scopes later.
                savedInstanceState = bundle
            }

            private companion object {
                private val SAVESTATE_BACKSTACK = "${DialogHistory::class.qualifiedName}_backstack"
                private fun DialogScope<*>.generateScopeKey() : String {
                    return "DialogScope_${this.dialogRoute::class.qualifiedName}"
                }
            }
        }

        // TODO: Wait for Kotlin 1.5.20 (KT-42652). Make this parcelable for saving states.
        @Parcelize
        internal data class BackStackRecord<T: Route>(val key: T, val animation: NavOptions.NavAnimation = NavOptions.NavAnimation()) : Parcelable

        private var current: BackStackRecord<T> = BackStackRecord(initial)
        private var backStack by mutableStateOf(listOf(current))

        internal val dialogHistory = DialogHistory()

        internal val associateKey: KClass<out Route>? = if (associateRoute != null) { associateRoute::class } else { null }

        internal var lastTransactionStatus: NavType = NavType.Forward

        internal fun get(): List<BackStackRecord<T>> = backStack

        internal fun set(elements: List<BackStackRecord<T>>, navType: NavType) {
            if (elements != backStack) {
                current = elements.last()
                lastTransactionStatus = navType
                backStack = elements
            }
        }

        internal fun push(element: BackStackRecord<T>) {
            current = element
            lastTransactionStatus = NavType.Forward
            backStack = get().plus(element)
        }
        internal fun peek(): BackStackRecord<T> = backStack.last()
        internal fun pop(): BackStackRecord<T>? {
            if (canGoBack()) { // last item will not be popped
                lastTransactionStatus = NavType.Backward
                current = backStack.last()
                backStack = backStack.subList(0, backStack.lastIndex)
                return current
            }
            return null
        }
        internal fun popUntil(key: KClass<out T>, inclusive: Boolean) : List<BackStackRecord<T>>? {
            val index = backStack.indexOfLast { it.key::class == key }
            if (index != -1) {
                val root = backStack.first()
                val clamp = if (inclusive) index else minOf(index + 1, backStack.size)

                val final = backStack.subList(0, clamp).ifEmpty { listOf(root) }
                val exclusive = backStack.subtract(final).toList()

                lastTransactionStatus = NavType.Backward
                current = backStack.last()
                backStack = final

                return exclusive
            }
            return null
        }
        internal fun canGoBack(): Boolean = backStack.size > 1
        internal fun canGoBackDialog(): Boolean = dialogHistory.count() > 0

        internal fun getCurrentRecord(): BackStackRecord<T> = current

        internal fun saveState(outState: Bundle) {
            if (backStack.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putParcelableArrayList(BACKSTACK_RECORDS, ArrayList(backStack))
                    putParcelable(BACKSTACK_LAST_ITEM, current)

                    backStack.forEach { record ->
                        val savedStateBundle = Bundle()
                        record.key.lifecycleController.performSave(savedStateBundle)
                        putBundle("$NAVIGATOR_SAVED_STATE_BUNDLE_KEY${record.key::class.qualifiedName}", savedStateBundle)
                    }
                    dialogHistory.saveState(this)
                }
                val name = "$HISTORY_SAVED_STATE${key.qualifiedName}"
                outState.putBundle(name, bundle)
            }
        }

        internal fun restoreState(bundle: Bundle?): String? {
            val name = "$HISTORY_SAVED_STATE${key.qualifiedName}"
            bundle?.getBundle(name)?.let { inner ->
                val records = inner.getParcelableArrayList<BackStackRecord<T>>(BACKSTACK_RECORDS)!!
                backStack = records

                inner.getParcelable<BackStackRecord<T>>(BACKSTACK_LAST_ITEM)?.let { current = it }

                backStack.fastForEach { record ->
                    val savedStateBundle = inner.getBundle("$NAVIGATOR_SAVED_STATE_BUNDLE_KEY${record.key::class.qualifiedName}")
                        ?: return@fastForEach
                    record.key.lifecycleController.performRestore(savedStateBundle)
                }
                dialogHistory.restoreState(inner)
                return name
            }
            return null
        }
    }

    /**
     * A controller to manage navigation for Route [T].
     *
     * One can create an instance using [rememberNavController] for setting up navigation through `navigator.Setup(...)`.
     *
     * One can retrieve an instance of controller using [findNavController] & passing the required Navigation root key.
     *
     * @see RouteKey
     */
    public class Controller<T : Route> internal constructor() {
        internal lateinit var routeKey: RouteKey<out Route>
        private var navigator: ComposeNavigator? = null
        private var history: History<T>? = null

        private val dialogCreateStack = arrayListOf<KClass<out DialogRoute>>()

        internal fun setup(key: RouteKey<out Route>, navigator: ComposeNavigator, history: History<T>) {
            this.routeKey = key; this.navigator = navigator; this.history = history
        }

        /**
         * When there are multiple dialogs to show on the screen each dialog content composable
         * will be stacked upon one another.
         *
         * This behavior is disabled by default but can be enabled when set to true.
         */
        public var enableDialogOverlay: Boolean = false

        /**
         * The current active [Route] in the navigation history.
         */
        public val currentRoute: T? get() = history?.get()?.lastOrNull()?.key

        /**
         * The parent [Route] associated with this nested navigation if it already is otherwise null.
         */
        public val parentRoute: Route? get() = history?.associateRoute

        /**
         * Navigate to a new destination of type [T]. Additional parameters can be set through [options] DSL.
         *
         * @see NavOptions
         */
        public fun navigateTo(destination: T, options: NavOptions<T>.() -> Unit = {}) {
            val navigator = navigator
            val history = history
            checkNotNull(navigator) { "Cannot navigate when navigator is not set." }
            checkNotNull(history) { "Cannot navigate when navigator is not set." }

            val current = NavOptions<T>().apply(options)
            val snapshot = ArrayList(history.get())

            val popOptions = current.popOptions
            if (popOptions != null) { // recursive remove till pop options
                val dest = if (popOptions.all)
                    snapshot.find { it.key::class == popOptions.dest }
                else
                    snapshot.findLast { it.key::class == popOptions.dest }
                val index = snapshot.indexOf(dest)
                if (index != -1) {
                    val clamp = if (popOptions.inclusive) index else minOf(index + 1, snapshot.lastIndex)
                    for(i in snapshot.size - 1 downTo clamp) {
                        val removed = snapshot.removeLast()
                        navigator.removeStateAndConfiguration(removed.key)
                    }
                }
            }
            if (current.singleTop) { // remove duplicates
                val items = snapshot.filter { it.key == destination}
                snapshot.removeAll(items)
                navigator.removeStateAndConfiguration(items.map { it.key })
            }

            snapshot.add(History.BackStackRecord(destination, current.animationOptions))

            if (!navigator.backStackMap.containsKey(routeKey)) {
                // This should not happen but it happened!
                navigator.backStackMap[routeKey] = history
            }
            navigator.backStackMap.bringToTop(routeKey)

            // recursively onStop previous key & it's associated children
            if (snapshot.size > 1) {
                val previousKey = snapshot[maxOf(0, snapshot.lastIndex - 1)].key
                navigator.recursiveHandleLifecycleEvent(previousKey, Lifecycle.Event.ON_STOP)
            }

            history.set(snapshot, History.NavType.Forward)
        }

        /**
         * @return A snapshot of all the keys (or empty list) associated with the current navigation backStack in the
         *         ascending order where the last one being the current screen.
         */
        public fun getAllHistory(): List<T> = history?.get()?.map { it.key } ?: emptyList()

        /**
         * @return A snapshot of all the dialog routes (or empty list) that this controller has created & being actively
         *         present in the backStack in the ascending order where the last one being the current
         *         dialog shown on the screen.
         */
        public fun getAllDialogHistory(): List<DialogRoute> = history?.dialogHistory?.get() ?: emptyList()

        /**
         * @return A [Flow] that will emit the current active route whenever it changes. This doesn't include dialogs.
         */
        public fun getCurrentRouteAsFlow(): Flow<T> = snapshotFlow {
            history?.get()?.lastOrNull()?.key
        }.filterNotNull()

        /**
         * @return If it safe to go back i.e up the stack. If false then it means the current composable
         *         is the last screen. This also means that the backstack is empty.
         */
        public fun canGoBack(): Boolean = navigator?.canGoBack() ?: false

        /**
         * Go back to the previous destination.
         *
         * @return The removed key.
         */
        public fun goBack(): T? = navigator?.goBack() as? T

        /**
         * Go back until the required destination is satisfied by [destKey] parameter ([inclusive] or not).
         *
         * @throws IllegalArgumentException if [destKey] doesn't exist in the backstack.
         * @return List of removed keys else empty if nothing is removed.
         */
        public fun goBackUntil(destKey: KClass<out T>, inclusive: Boolean = false): List<T> {
            val history = history
            val navigator = navigator
            checkNotNull(history) { "Cannot perform this operation until navigator is not set." }
            checkNotNull(navigator) { "Cannot perform this operation until navigator is not set." }

            val exclusive = history.popUntil(destKey, inclusive)?.map { it.key } ?: throw IllegalArgumentException("Required key: $destKey does not exist in the backstack")
            navigator.removeStateAndConfiguration(exclusive)
            return exclusive
        }

        /**
         * Jump back to the root destination.
         *
         * @return List of removed keys else empty if nothing is removed.
         */
        public fun goBackToRoot() : List<T> {
            val history = history
            checkNotNull(history) { "Cannot perform this operation until navigator is not set." }

            return goBackUntil(history.get().first().key::class, inclusive = false)
        }

        /**
         * Setup a composable that will be displayed in the [Dialog] with the backStack functionality.
         *
         * @param key Key associated with the dialog usually a data class.
         * @param dialogProperties [DialogProperties] to customize dialog.
         * @param handleOnDismissRequest Intercept whether dismiss request is handled or not. Returning `true`
         *                               will considered as request being handled & will not dismiss the dialog.
         * @param content The composable body of the dialog which supplies two arguments.
         *               `dialogRoute` which is passed throw showDialog() & `dismiss` lambda
         *                which can be called to dismiss current dialog.
         */
        @Composable
        public fun<T : DialogRoute> CreateDialog(key: KClass<T>, dialogProperties: DialogProperties = DialogProperties(), handleOnDismissRequest: () -> Boolean = { false }, content: @Composable DialogScope<T>.() -> Unit) {
            if (LocalInspectionMode.current) return

            val history = history
            val navigator = navigator
            checkNotNull(navigator) { "Cannot create dialog when navigator is not set." }
            checkNotNull(history) { "Cannot create dialog when history is not set." }

            @Composable
            fun Inner(peek: DialogRoute) {
                val dialogScope = remember { history.dialogHistory.createDialogScope(peek, handleOnDismissRequest) }
                Dialog(onDismissRequest = { dialogScope.goBack() ?: dialogScope.dismiss() }, properties = dialogProperties) {
                    val lifecycleController = remember(peek) {
                        val lifecycleController = peek.lifecycleController
                        if (!lifecycleController.isRestored()) {
                            lifecycleController.performRestore(null)
                        }
                        lifecycleController.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                        return@remember lifecycleController
                    }
                    CompositionLocalProvider(*lifecycleController.providers.toTypedArray()) {
                        content(dialogScope as DialogScope<T>)
                    }
                    DisposableEffect(Unit) {
                        onDispose {
                            peek.lifecycleController.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                            navigator.removeStateAndConfiguration(peek)
                        }
                    }
                }
            }

            if (!history.dialogHistory.isEmpty()) {
                val peek = if (enableDialogOverlay) {
                    history.dialogHistory.get(key)
                } else {
                    remember(history.dialogHistory.count()) { history.dialogHistory.peek()!! }
                }
                if (peek != null && peek::class == key) {
                    Inner(peek)
                }
            }

            LaunchedEffect(Unit) {
                if (!dialogCreateStack.contains(key)) dialogCreateStack.add(key)
            }
        }

        /**
         * Show a dialog which was created before using [CreateDialog].
         */
        public fun<T : DialogRoute> showDialog(key: T) {
            val history = history
            checkNotNull(history) { "Cannot show dialog when navigator is not set." }

            if (!dialogCreateStack.contains(key::class)) {
                throw IllegalStateException("Dialog with the key \"${key::class.qualifiedName}\" is not present in the backStack. Did you forgot to create Dialog using \"controller.CreateDialog(...)\".")
            }
            if (history.dialogHistory.get().contains(key)) throw IllegalStateException("Cannot show dialog \"${key::class.qualifiedName}\" twice.")

            history.dialogHistory.add(key)
        }

        /**
         * Force dismiss an ongoing dialog which is currently being shown or was in the backStack. This
         * respects neither [handleOnDismissRequest] nor [DialogScope.goBack]'s backstack.
         *
         * @param key The key that was used to [CreateDialog].
         * @throws IllegalStateException When the dialog associated with the [key] does not exist in the backstack.
         */
        public fun closeDialog(key: KClass<out DialogRoute>): DialogRoute {
            val history = history
            checkNotNull(history) { "Cannot close dialog when navigator is not set." }
            return history.dialogHistory.remove(key) ?: throw IllegalStateException("Dialog with key \"$key\" does not exist in the backstack to close.")
        }
    }

    /**
     * @return A snapshot of all the keys that were used during setup of navigation including
     *         the nested ones (if they are present).
     */
    public fun getAllKeys(): List<RouteKey<out Route>> {
        return backStackMap.map { it.key }
    }

    /**
     * @return A snapshot of the combined history (nested navigation) including dialog routes
     *         which are stored in the backStack in the ascending order where the last one
     *         being the current route.
     */
    public fun getAllHistory(): List<Route> {
        return backStackMap.flatMap { (_, history) ->
            history.get().map { it.key }
        } + (backStackMap.lastValue()?.dialogHistory?.get() ?: emptyList())
    }

    /**
     * @param key Key which is used during the setup of navigation.
     * @return A snapshot of the history for the [key] including all dialog routes.
     */
    public fun getHistory(key: RouteKey<out Route>): List<Route> {
        val value = backStackMap[key] ?: throw RuntimeException("No navigation was setup using key: $key")
        return value.get().map { it.key } + value.dialogHistory.get()
    }

    /**
     * Save all the ComposeNavigator's internal states along with destination states.
     */
    public fun onSaveInstanceState(outState: Bundle) {
        val navigatorBundle = Bundle()
        backStackMap.forEach { (_, v) -> v.saveState(navigatorBundle) }
        outState.putBundle("${activity::class.qualifiedName}$NAVIGATOR_SAVED_STATE_SUFFIX", navigatorBundle)
    }

    /**
     * Recursive reverse calls to [History.canGoBack] from [backStackMap] to identify if back navigation is possible or not.
     */
    public fun canGoBack(): Boolean {
        val last = backStackMap.lastValue()
        if (last?.canGoBackDialog() == true) return true
        if (backStackMap.size > 1) {
            val aggregate = backStackMap.values.sumOf { it.get().size }
            if (aggregate == backStackMap.size) return false
            return true
        }
        return last?.canGoBack() == true
    }

    /**
     * Recursive reverse call to [History.canGoBack] to identify if back navigation is possible or not.
     *
     * If possible then the [History.pop] will be called to remove the last item from the backstack.
     */
    public fun goBack(): Route? {
        val last = backStackMap.lastValue()

        // dialogs
        if (last != null && !last.dialogHistory.isEmpty()) {
            last.dialogHistory.pop()?.let { return it }
            return null // this means dismiss() in DialogScope has been handled by someone
        }

        if (backStackMap.size > 1 && !last!!.canGoBack()) {
            backStackMap.removeLastOrNull()
            return goBack()
        }

        val popped = last?.pop()
        last?.let { _ ->
            val currentLastKey = last.get().last().key::class
            var associateKey: RouteKey<out Route>? = null
            backStackMap.forEach call@{ (key, history) ->
                if (history.associateKey == currentLastKey) {
                    associateKey = key
                    return@call
                }
            }
            associateKey?.let { backStackMap.bringToTop(it) }
        }

        return popped?.key
    }

    /**
     * Go back call until destination is satisfied. It also considers destination from parent navigation.
     *
     * Note: The result may not be expected but it will mostly be theoretically correct.
     *       For eg: If a destination is currently used only for for nested navigation
     *       then it will choose the initial key of this nested navigation if inclusive
     *       is false.
     *
     * @throws IllegalArgumentException if [destKey] doesn't exist in the backstack or [destKey] is [DialogRoute].
     */
    @UnstableNavigatorApi
    public fun goBackUntil(destKey: KClass<out Route>, inclusive: Boolean = false): Boolean {
        if (getAllHistory().none { it::class == destKey }) {
            throw IllegalArgumentException("Required key: $destKey does not exist in the backstack.")
        }
        if (destKey is DialogRoute) throw IllegalArgumentException("Dialog routes are not supported.")

        /* This works more like a heap compacting,
         * Eg: BackStackMap where we want to pop until "d" (inclusive).
         *     [key1 = {a,b,c} , key2 = {d,e,f} , key3 = {g,h,i}, key4 = {j,k,l}]
         * We iterate through backstack in reverse order...
         * 1st iteration -> key4
         * -------------
         *   It doesn't contain "d" so we will modify it to become key4 = {l}
         *   essentially keeping only one item i.e current one
         * 2nd iteration -> key3
         * -------------
         *   It doesn't contain "d" so we remove it completely from BackStackMap.
         * 3rd iteration -> key2
         * -------------
         *   This contains "d" so we will modify it to become key2 = {d}.
         *   Since "d" is the first time this is a special case,
         *      where now we should pop till "c" exclusively from previous map.
         *      our destKey becomes "c" & we remove this pair completely.
         * 4th iteration -> key1 (special case)
         * -------------
         *   This contains "c" as item so no changes to the list.
         *   (If suppose it was "b" then we modify it to become key1 = {a,b})
         *
         * Lastly our map looks like [key1 = {a,b,c} , key4 = {l}].
         * Now we call [goBack] to go back with the animation; becoming [key1 = {a,b,c}]
         */
        var finalDestKey = destKey
        var finalInclusive = inclusive

        var firstIterationSet = false
        var secondIterationSet = false
        if (backStackMap.isNotEmpty()) {
            var keys = backStackMap.keys.toList()
            val associatedItem = backStackMap.filter { it.value.associateKey == finalDestKey }.firstNotNullOfOrNull { it }
            val sourceItem = backStackMap.filter { entry -> entry.value.get().any { it.key::class == finalDestKey } }.firstNotNullOf { it }
            val sourceKeyIndex = keys.indexOf(sourceItem.key)
            if (associatedItem != null) {
                val associatedKeyIndex = keys.indexOf(associatedItem.key)
                if (associatedKeyIndex < sourceKeyIndex) {
                    backStackMap.moveItemIndex(associatedKeyIndex, sourceKeyIndex + 1)
                    if (sourceItem.value.get().last().key::class != finalDestKey) {
                        sourceItem.value.popUntil(finalDestKey as KClass<Nothing>, inclusive = false)
                            ?.also { removed -> removeStateAndConfiguration(removed.map { it.key }) }
                    }
                }
                if (!inclusive) {
                    return goBackUntil(associatedItem.value.get().first().key::class, inclusive = false)
                } else {
                    val newIndex = backStackMap.indexOfKey(sourceItem.key)
                    if (newIndex > 0 && (sourceItem.value.get().size == 1 || sourceItem.value.get().last().key::class != finalDestKey)) {
                        val previousEntry = backStackMap.getIndex(newIndex - 1)
                        val previousEntryLastKey = previousEntry.value.get().last().key
                        if (sourceItem.value.associateKey == previousEntry.value.get().last().key::class) {
                            return goBackUntil(previousEntryLastKey::class, inclusive = true)
                        }
                        return goBackUntil(previousEntryLastKey::class, inclusive = false)
                    } else if (newIndex == 0 && sourceItem.value.get().size == 1) {
                        return goBackUntil(associatedItem.value.get().first().key::class, inclusive = false)
                    }
                }
            } else if (sourceItem.value.associateKey != null && (sourceKeyIndex != backStackMap.lastIndex || sourceItem.value.get().indexOfFirst { it.key::class == finalDestKey } == 0)) {
                val associatedKeyItem = backStackMap.filter { entry -> entry.value.get().any { it.key::class == sourceItem.value.associateKey } }.firstNotNullOf { it }
                val associatedKeyIndex = keys.indexOf(associatedKeyItem.key)
                if (sourceKeyIndex < associatedKeyIndex) {
                    backStackMap.moveItemIndex(sourceKeyIndex, associatedKeyIndex + 1)
                    if (associatedKeyItem.value.get().last().key::class != sourceItem.value.associateKey) {
                        associatedKeyItem.value.popUntil(sourceItem.value.associateKey as KClass<Nothing>, inclusive = false)
                            ?.also { removed -> removeStateAndConfiguration(removed.map { it.key }) }
                    }
                }
                if (inclusive && sourceItem.value.get().indexOfFirst { it.key::class == finalDestKey } == 0) {
                    return goBackUntil(associatedKeyItem.value.get().last().key::class, inclusive = true)
                }
            }
            // keys may have changed due to reordering so better to calculate it again.
            keys = backStackMap.keys.toList()
            val lastRouteKey = backStackMap.lastKey()

            for (i in keys.size - 1 downTo 0) {
                if (firstIterationSet && secondIterationSet) break

                val routeKey = keys[i]
                val history = backStackMap[routeKey] as? History<out Route> ?: continue
                val snapshot = history.get()
                val index = snapshot.indexOfLast { it.key::class == finalDestKey }
                // Key is present & is the current backstack but key is not the topKey.
                if (index != -1 && routeKey == lastRouteKey && !finalInclusive) {
                    // clear dialogs
                    history.dialogHistory.clear()
                    // we found the key in the last map itself so popUntil & gracefully return
                    val exclusive = history.popUntil(finalDestKey as KClass<Nothing>, finalInclusive) ?: emptyList()
                    removeStateAndConfiguration(exclusive.map { it.key })
                    return true
                // Key is present
                } else if (index != -1 && !firstIterationSet) {
                    // clear dialogs
                    history.dialogHistory.clear()

                    val clamp = if (finalInclusive) index else minOf(index + 1, snapshot.size)
                    var final = snapshot.subList(0, clamp)
                    // Special case key is topKey & inclusive is true
                    if (final.isEmpty() && i != 0) {
                        finalDestKey = backStackMap[keys[i - 1]]!!.get().last().key::class // change key to last item from previous backstack
                        finalInclusive = false
                        if (routeKey == lastRouteKey) {
                            val current = history.peek()
                            history.get().filter { it != current }.also { stack -> removeStateAndConfiguration(stack.map { it.key }) }
                            history.set(listOf(current) as List<Nothing>, History.NavType.Forward)
                        } else {
                            val backstack = backStackMap.remove(routeKey)?.get() ?: emptyList()
                            removeStateAndConfiguration(backstack.map { it.key })
                        }
                        firstIterationSet = true
                        continue
                    // The selected root is the first route of the leaf navigator.
                    } else if (final.isEmpty()&& i == 0) {
                        final = listOf(snapshot.first())
                    }
                    snapshot.subtract(final).toList().also { stack -> removeStateAndConfiguration(stack.map { it.key }) }
                    history.set(final as List<Nothing>, History.NavType.Forward)

                    firstIterationSet = true

                // Key is not present but the current route key is the last route
                } else if (index == -1 && routeKey == lastRouteKey && !secondIterationSet) {
                    // clear dialogs
                    history.dialogHistory.clear()

                    val current = history.get().last()
                    history.get().filter { it != current }.also { stack -> removeStateAndConfiguration(stack.map { it.key }) }
                    history.set(listOf(current) as List<Nothing>, History.NavType.Forward)

                    secondIterationSet = true

                // Key is not present & is neither the last route. Just remove it.
                } else if (index == -1 && routeKey != lastRouteKey) {
                    val backstack = backStackMap.remove(routeKey)?.get() ?: emptyList()
                    removeStateAndConfiguration(backstack.map { it.key })
                }
            }

            if (backStackMap.size > 1) {
                keys = backStackMap.keys.toList()
                val lastBackStack = backStackMap.lastValue()!!
                val secondLastBackStack = backStackMap[keys[keys.lastIndex - 1]]!!

                if ((lastBackStack.get().count() == 1 && secondLastBackStack.get().count() == 1) || lastBackStack.associateKey != secondLastBackStack.key) {
                    val last = secondLastBackStack.get().last()
                    val finalElement = if (secondLastBackStack.get().size == 1 && last.animation.current == None && last.animation.target == None)
                        last.copy(animation = lastBackStack.get().last().animation) // We use animation defined by the current screen if None
                    else
                        last
                    val mutatedSecondSnapshot = secondLastBackStack.get().plus(finalElement)
                    secondLastBackStack.set(mutatedSecondSnapshot as List<Nothing>, History.NavType.Forward)

                    return goBack() != null
                }
            }

            return goBack() != null
        }

        return false
    }

    /**
     * Jump back to the root destination of the leaf navigation.
     */
    @UnstableNavigatorApi
    public fun goBackToRoot() : Boolean {
        val leafRoute = getAllHistory().first()::class
        return goBackUntil(leafRoute, inclusive = false)
    }

    private val backPressHandler = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            goBack()
        }
    }

    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {

        private fun handleLastRouteLifecycleEvent(event: Lifecycle.Event) {
            val route = getAllHistory().lastOrNull() ?: return
            if (route.lifecycleController.isRestored()) {
                recursiveHandleLifecycleEvent(route, event)
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(act: Activity) {
            if (activity === act) {
                handleLastRouteLifecycleEvent(Lifecycle.Event.ON_START)
            }
        }
        override fun onActivityResumed(act: Activity) {
            if (activity === act) {
                handleLastRouteLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
        }
        override fun onActivityPaused(act: Activity) {
            if (activity === act) {
                handleLastRouteLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            }
        }
        override fun onActivityStopped(act: Activity) {
            if (activity === act) {
                handleLastRouteLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
        }
        override fun onActivityDestroyed(activity: Activity) {}
        override fun onActivitySaveInstanceState(act: Activity, outState: Bundle) {
            if (activity === act) {
                onSaveInstanceState(outState)
            }
        }
    }

    private fun recursiveHandleLifecycleEvent(key: Route, event: Lifecycle.Event) {
        key.lifecycleController.handleLifecycleEvent(event)
        // 1. manage lifecycles of descendants
        // 2. manage lifecycles of ascendants, for eg: if "key" is first key of nested navigation
        //    then also change lifecycle of its parent.
        for (history in backStackMap.values) {
            if (history.associateRoute === key) {
                history.get().fastForEach { child ->
                    recursiveHandleLifecycleEvent(child.key, event)
                }
            }
            if (history.get().firstOrNull()?.key === key && history.associateRoute != null) {
                history.associateRoute.lifecycleController.handleLifecycleEvent(event)
            }
        }
    }

    private fun removeStateAndConfiguration(vararg items: Route, filterByCurrent: Boolean = true) {
        removeStateAndConfiguration(items.toList(), filterByCurrent)
    }
    private fun removeStateAndConfiguration(items: List<Route>, filterByCurrent: Boolean = true) {
        items.fastForEach { route ->
            if (filterByCurrent && route === currentNavRoute) return@fastForEach
            val lifecycleController = route.lifecycleController
            if (!activity.isChangingConfigurations) {
                lifecycleController.clearViewModelStore()
            }
            if (lifecycleController.lifecycle.currentState != Lifecycle.State.INITIALIZED) {
                lifecycleController.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
            LifecycleControllerStore.remove(route)
        }
    }

    internal val backStackMap = mutableMapOf<RouteKey<out Route>, History<*>>()
    private val navigatorTransitions: ArrayList<NavigatorTransition> = arrayListOf()
    private var savedState: Bundle? = null
    private var currentNavRoute: Route? = null

    init {
        activity.onBackPressedDispatcher.addCallback(backPressHandler)
        (activity.applicationContext as Application).registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        // find correct state
        savedState = savedInstanceState?.getBundle("${activity::class.qualifiedName}$NAVIGATOR_SAVED_STATE_SUFFIX")
    }

    private fun<T : Route> fetchOrUpdateHistory(key: RouteKey<T>, associateKey: Route?, initial: T): History<T> {
        val present = backStackMap.containsKey(key)
        if (!present) {
            // restore from saved state
            val history = if (associateKey != null) {
                History(key, associateKey, initial)
            } else {
                History(key, null, initial)
            }
            savedState?.remove(history.restoreState(savedState))
            backStackMap[key] = history
            return history
        }
        return (backStackMap[key] as History<T>)
    }

    /**
     * An entry to setup navigation.
     *
     * Destinations should be managed within [content]. [content] is composable lambda that receives a parameter
     * of type [T] which is the current active destination in the backstack.
     *
     * @param key Will be used for uniquely identifying this Route in the composition tree.
     * @param initial The start destination for the navigation.
     * @param controller Controller that will be used for managing navigation for [key]
     *
     * @see Route
     * @see RouteKey
     * @see Controller
     */
    @Composable
    public fun<T : Route> Setup(modifier: Modifier = Modifier, key: RouteKey<T>, initial: T, controller: Controller<T>, content: @Composable (currentRoute: T) -> Unit) {
        val associateKey : Route? = remember {
            if (backStackMap.isNotEmpty()) {
                backStackMap.lastValue()!!.getCurrentRecord().key
            } else null
        }
        val history = remember { fetchOrUpdateHistory(key, associateKey, initial) }
        val controllerInternal = remember { controller.apply { setup(key, this@ComposeNavigator, history) } }
        val compositionLocalScope = rememberComposable { getLocalController(key) }
        val saveableStateHolder = rememberSaveableStateHolder()

        @Composable
        fun Inner(body: @Composable () -> Unit) = Box(modifier) { body() }

//       TODO: Make sure there is no twice key in the backStack
        Inner {
            // recompose on history change
            CompositionLocalProvider(compositionLocalScope provides controllerInternal, LocalNavigator provides this) {
                val record = history.peek()
                if (backStackMap.containsKey(key)) {
                    val animation = if (history.lastTransactionStatus == History.NavType.Forward) record.animation else history.getCurrentRecord().animation
                    CommonEffect(targetState = record.key, animation = animation, isBackward = history.lastTransactionStatus == History.NavType.Backward) { peek ->
                        val stateHolderObserver = remember(peek) { object : LifecycleEventObserver {
                            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                                if (event == Lifecycle.Event.ON_DESTROY) {
                                    saveableStateHolder.removeState(record.key)
                                    source.lifecycle.removeObserver(this)
                                }
                            }
                        } }

                        val lifecycleController = remember(peek) {
                            val lifecycleController = peek.lifecycleController
                            // since restoring of savedStateRegistry happens when history is created when savedState is not null.
                            // in any case if it is null we must restore an empty state.
                            if (!lifecycleController.isRestored()) {
                                lifecycleController.performRestore(null)
                                lifecycleController.lifecycle.addObserver(stateHolderObserver)

                                // onCreate()
                                lifecycleController.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                            }
                            lifecycleController
                        }

                        CompositionLocalProvider(*lifecycleController.providers.toTypedArray()) {
                            saveableStateHolder.SaveableStateProvider(key = peek) {
                                content(peek)
                            }
                        }

                        DisposableEffect(peek) {
                            currentNavRoute = peek
                            if (peek.lifecycleController.lifecycle.currentState != Lifecycle.State.RESUMED) {
                                peek.lifecycleController.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                            }

                            onDispose {
                                if (!history.get().fastAny { it.key == record.key }) {
                                    removeStateAndConfiguration(record.key, filterByCurrent = false)
                                }
                            }
                        }
                    }
                }
                DisposableEffect(key, record.key) {
                    backPressHandler.isEnabled = canGoBack() // update if back press is enabled or not.
                    onDispose {
                        if (!backStackMap.containsKey(key)) {
                            history.get().forEach { record ->
                                removeStateAndConfiguration(record.key, filterByCurrent = false)
                            }
                        }
                    }
                }
            }
        }
        DisposableEffect(key) {
            onDispose { compositionLocalScopeList.remove(compositionLocalScope) }
        }
    }

    @Composable
    private fun <T> CommonEffect(
        targetState: T,
        animation: NavOptions.NavAnimation,
        isBackward: Boolean = false, // if triggered on back press.
        content: @Composable (T) -> Unit
    ) {
        val items = remember { mutableStateListOf<CommonAnimationItemHolder<T>>() }
        val transitionState = remember { MutableTransitionState(targetState) }
        val targetChanged = (targetState != transitionState.targetState)
        transitionState.targetState = targetState
        val transition = updateTransition(transitionState, label = "transition")

        val enterAnimation = remember(animation) { navigatorTransitions.find { it.key == animation.target } ?: throw IllegalArgumentException("Could not find the enter animation \"${animation.target.key}\". Did you forgot to register it?") }
        val exitAnimation = remember(animation) { navigatorTransitions.find { it.key == animation.current } ?: throw IllegalArgumentException("Could not find the enter animation \"${animation.target.key}\". Did you forgot to register it?") }

        fun getAnimationSpec(key: T): FiniteAnimationSpec<Float> {
            return if (key == targetState) {
                if (!isBackward) enterAnimation.animationSpec else exitAnimation.animationSpec
            } else {
                if (!isBackward) exitAnimation.animationSpec else enterAnimation.animationSpec
            }
        }

        fun getUpdatedModifier(width: Int, height: Int, progress: Float, key: T): Modifier {
            val predicate = if (!isBackward) key == targetState else key != targetState
            val composeTransition = if (predicate) {
                if (!isBackward) enterAnimation.forwardTransition else enterAnimation.backwardTransition
            } else {
                if (!isBackward) exitAnimation.forwardTransition else exitAnimation.backwardTransition
            }
            return composeTransition.invoke(Modifier, width, height, if (!isBackward) progress else 1 - progress)
        }

        if (targetChanged || items.isEmpty()) {
            val keys = items.map { it.key }.run {
                if (!contains(targetState)) {
                    toMutableList().also { it.add(targetState) }
                } else {
                    this
                }
            }
            items.clear()
            keys.mapTo(items) { key ->
                CommonAnimationItemHolder(key) {
                    BoxWithConstraints {
                        val animationSpec = getAnimationSpec(key)

                        val progress by transition.animateFloat(
                            transitionSpec = { animationSpec }, label = "normal")
                        { if (it == key) 1f else 0f }

                        val width = with(LocalDensity.current) { maxWidth.toPx().toInt() }
                        val height = with(LocalDensity.current) { maxHeight.toPx().toInt() }
                        val internalModifier = getUpdatedModifier(width, height, progress, key)
                        Box(internalModifier) {
                            content(key)
                        }
                    }
                }
            }
        } else if (transitionState.currentState == transitionState.targetState) {
            items.removeAll { it.key != transitionState.targetState }
        }

        items.fastForEach {
            key(it.key) {
                it.content()
            }
        }
    }

    private data class CommonAnimationItemHolder<T>(
        val key: T,
        val content: @Composable () -> Unit
    )

    private val compositionLocalScopeList = arrayListOf<ProvidableCompositionLocal<*>>() // for memoization
    @Composable
    internal fun<T: Route> getLocalController(key: RouteKey<T>): ProvidableCompositionLocal<Controller<T>?> {
        compositionLocalScopeList.asReversed().fastForEach { scope ->
            val controller = scope.current as? Controller<*>
            if (controller?.routeKey == key) return scope as ProvidableCompositionLocal<Controller<T>?>
        }
        val scope = compositionLocalOf<Controller<T>?> { null }
        compositionLocalScopeList.add(scope)
        return scope
    }
}

private val LocalNavigator = staticCompositionLocalOf<ComposeNavigator> { throw Exception("Compose Navigator not set. Did you forgot to call \"Navigator.Setup\"?") }

private val<K, V> Map<K, V>.lastIndex get() = size - 1
private fun<K, V> Map<K,V>.lastKey(): K? = keys.lastOrNull()
private fun<K, V> Map<K,V>.lastValue(): V? = get(keys.lastOrNull())
private fun<K, V> MutableMap<K,V>.removeLastOrNull(): V? = remove(keys.lastOrNull())
private fun<K, V> MutableMap<K, V>.bringToTop(key: K) = remove(key)?.let { put(key, it) }
private fun<K, V> MutableMap<K, V>.moveItemIndex(src: Int, dest: Int) {
    val tempMap = mutableMapOf<K, V>()

    val keyList = keys.toList()
    val sourceKey = keyList[src]
    if (dest > keyList.lastIndex) {
        val value = remove(sourceKey)!!
        put(sourceKey, value)
        return
    }
    val destKey = keyList[minOf(dest, keyList.lastIndex)]

    if (sourceKey == destKey) return

    forEach { (key, value) ->
        if (key != sourceKey && key != destKey) {
            tempMap[key] = value
        } else if (key == destKey) {
            tempMap[sourceKey] = get(sourceKey)!!
            tempMap[key] = value
        }
    }

    clear()

    tempMap.forEach { put(it.key, it.value) }
    tempMap.clear()
}
private fun <K, V> Map<K, V>.indexOfKey(key: K) : Int {
    return keys.indexOf(key)
}
private fun <K, V> Map<K, V>.getIndex(index: Int) : Map.Entry<K, V> {
    return entries.elementAt(index)
}

/**
 * Remember the value produced by @[Composable] that does not have a [Unit] return type.
 */
@Composable
private inline fun <T> rememberComposable(calculation: @Composable () -> T): T {
    val internal = remember { mutableStateOf<T?>(null) }
    if (internal.value == null) {
        internal.value = calculation()
    }
    return internal.value!!
}

private fun Context.findActivity(): ComponentActivity {
    if (this is ComponentActivity) return this
    if (this is ContextWrapper) {
        val baseContext = this.baseContext
        if (baseContext is ComponentActivity) return baseContext
        return baseContext.findActivity()
    }
    throw NotImplementedError("Could not find activity from $this.")
}