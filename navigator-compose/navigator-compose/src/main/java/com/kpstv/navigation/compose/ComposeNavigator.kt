@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.kpstv.navigation.compose

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.util.fastForEach
import com.kpstv.navigation.compose.EnterAnimation.Companion.reverse
import com.kpstv.navigation.compose.ExitAnimation.Companion.reverse
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KClass

/**
 * Find the [ComposeNavigator] provided by the nearest CompositionLocalProvider.
 */
@Composable
public fun findComposeNavigator() : ComposeNavigator = LocalNavigator.current

/**
 * Find & remember the [ComposeNavigator.Controller] provided by the nearest CompositionLocalProvider for [key].
 */
@Composable
public fun <T : Route> findController(key: KClass<T>): ComposeNavigator.Controller<T> {
    return rememberComposable {
        getLocalController(key).current ?: throw Exception("Could not find Controller for key \"${key.qualifiedName}\". Did you forgot to call \"Navigator.Setup\"?")
    }
}


/**
 * Destination must implement this interface to identify as Key for the root
 */
public interface Route : Parcelable

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
     * @param enter Set the enter transition for the destination composable.
     * @param exit Set the exit transition for the current composable.
     */
    @Parcelize
    public data class NavAnimation(
        var enter: EnterAnimation = EnterAnimation.None,
        var exit: ExitAnimation = ExitAnimation.None
    ) : Parcelable

    /**
     * @param inclusive Include this destination to be popped as well.
     * @param all There could be situation where multiple destinations could be present on the backstack,
     *            this will recursively pop till the first one in the backstack. Otherwise, the last
     *            added one will be chosen.
     */
    public data class PopUpOptions<T: Route>(internal var dest: T, var inclusive: Boolean = true, var all: Boolean = false)

    /**
     * Pop up to the destination. Additional parameters can be set through [options] DSL.
     */
    public fun popUpTo(dest: T, options: PopUpOptions<T>.() -> Unit = {}) {
        popOptions = PopUpOptions(dest).apply(options)
    }

    /**
     * Customize enter & exit animations through [options] DSL.
     */
    public fun withAnimation(options: NavAnimation.() -> Unit = {}) {
        animationOptions = NavAnimation().apply(options)
    }
}

@Parcelize
public enum class EnterAnimation : Parcelable {
    None, FadeIn, SlideInRight, SlideInLeft, ShrinkIn;
    internal companion object {
        internal fun EnterAnimation.reverse() : ExitAnimation {
            return when(this) {
                FadeIn -> ExitAnimation.FadeOut
                SlideInRight -> ExitAnimation.SlideOutRight
                SlideInLeft -> ExitAnimation.SlideOutLeft
                ShrinkIn -> ExitAnimation.ShrinkOut
                else -> ExitAnimation.None
            }
        }
    }
}

@Parcelize
public enum class ExitAnimation : Parcelable {
    None, FadeOut, SlideOutRight, SlideOutLeft, ShrinkOut;
    internal companion object {
        internal fun ExitAnimation.reverse() : EnterAnimation {
            return when(this) {
                FadeOut -> EnterAnimation.FadeIn
                SlideOutRight -> EnterAnimation.SlideInRight
                SlideOutLeft -> EnterAnimation.SlideInLeft
                ShrinkOut -> EnterAnimation.ShrinkIn
                else -> EnterAnimation.None
            }
        }
    }
}

/**
 * A navigator for managing navigation in Jetpack Compose.
 */
public class ComposeNavigator private constructor(private val activity: ComponentActivity, savedInstanceState: Bundle?) {

    public companion object {
        private const val HISTORY_SAVED_STATE = "compose_navigator:state:"
        private const val NAVIGATOR_SAVED_STATE_SUFFIX = "_compose_navigator"

        /**
         * Creates a new builder for [ComposeNavigator].
         */
        public fun with(activity: ComponentActivity, savedInstanceState: Bundle?) : Builder = Builder(activity, savedInstanceState)
    }

    public class Builder internal constructor(activity: ComponentActivity, savedInstanceState: Bundle?) {
        private val navigator = ComposeNavigator(activity, savedInstanceState)

        /**
         * This will disable Navigator's internal back press logic if set to `False` which you then have to manually
         * handle in Activity's `onBackPressed()`.
         */
        public fun setDefaultBackPressEnabled(value: Boolean): Builder {
            if (!value) {
                navigator.backPressHandler.remove()
            }
            return this
        }

        /**
         * Specify the animation spec that should be used when transitioning between screens.
         */
        public fun setAnimationSpec(spec: FiniteAnimationSpec<Float>): Builder {
            navigator.animationSpec = spec
            return this
        }

        /**
         * Returns the configured instance of [ComposeNavigator].
         */
        public fun initialize() : ComposeNavigator = navigator

        // TODO: Option to set custom Animation Spec (if needed).
    }

    //TODO: Make primary destination feature which will be navigated automatically when backstack becomes empty.
    /**
     * @param associateKey A parent key for this [key]. This means [associateKey] has setup navigation for this [key].
     */
    internal class History<T : Route> internal constructor(private val key: KClass<T>, internal val associateKey: KClass<out Route>?, internal val initial: T) {
        companion object {
            private const val LAST_REMOVED_ITEM_KEY = "history:last_item:key"
            private const val LAST_REMOVED_ITEM_ANIMATION = "history:last_item:animation"
        }
        internal enum class NavType { Forward, Backward }

        // TODO: Wait for Kotlin 1.5.20 (KT-42652). Make this parcelable for saving states.
        internal data class BackStackRecord<T: Route>(val key: T, val animation: NavOptions.NavAnimation = NavOptions.NavAnimation())

        private var backStack by mutableStateOf(listOf(BackStackRecord(initial)))
        private var lastRemoved: BackStackRecord<T>? = null

        internal var lastTransactionStatus: NavType = NavType.Forward

        internal fun get(): List<BackStackRecord<T>> = backStack

        internal fun set(elements: List<BackStackRecord<T>>) {
            if (elements != backStack) {
                lastTransactionStatus = NavType.Forward
                backStack = elements
            }
        }

        internal fun peek(): BackStackRecord<T> = backStack.last()
        internal fun pop(): BackStackRecord<T>? {
            if (canGoBack()) { // last item will not be popped
                lastTransactionStatus = NavType.Backward
                lastRemoved = backStack.last()
                backStack = backStack.subList(0, backStack.lastIndex)
                return lastRemoved
            }
            return null
        }
        internal fun canGoBack(): Boolean = backStack.size > 1

        internal fun getLastRemovedItem(): BackStackRecord<T>? = lastRemoved

        internal fun saveState(outState: Bundle) {
            if (backStack.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putParcelableArrayList(BackStackRecord<T>::key.name, ArrayList(backStack.map { it.key }))
                    putParcelableArrayList(BackStackRecord<T>::animation.name, ArrayList(backStack.map { it.animation }))
                    putParcelable(LAST_REMOVED_ITEM_KEY, lastRemoved?.key)
                    putParcelable(LAST_REMOVED_ITEM_ANIMATION, lastRemoved?.animation)
                }
                val name = "$HISTORY_SAVED_STATE${key.qualifiedName}"
                outState.putBundle(name, bundle)
            }
        }

        internal fun restoreState(bundle: Bundle?): String? {
            val name = "$HISTORY_SAVED_STATE${key.qualifiedName}"
            bundle?.getBundle(name)?.let { inner ->
                val keys: List<T> = inner.getParcelableArrayList(BackStackRecord<T>::key.name)!!
                val animations: List<NavOptions.NavAnimation> = inner.getParcelableArrayList(BackStackRecord<T>::animation.name)!!

                val lastKey: T? = inner.getParcelable(LAST_REMOVED_ITEM_KEY)
                val lastAnimation: NavOptions.NavAnimation? = inner.getParcelable(LAST_REMOVED_ITEM_ANIMATION)
                if (lastKey != null && lastAnimation != null) {
                    lastRemoved = BackStackRecord(lastKey, lastAnimation)
                }

                backStack = keys.zip(animations).map { BackStackRecord(it.first, it.second) }

                return name
            }
            return null
        }
    }

    /**
     * A controller to manage navigation for Route [T].
     *
     * This should be used to handle forward as well as backward navigation.
     */
    public class Controller<T : Route> internal constructor(internal val key: KClass<out Route>, private val navigator: ComposeNavigator, private val history: History<T>) {

        /**
         * Navigate to other destination composable. Additional parameters can be set through [options] DSL.
         */
        public fun navigateTo(destination: T, options: NavOptions<T>.() -> Unit = {}) {
            val current = NavOptions<T>().apply(options)
            val snapshot = ArrayList(history.get())

            val popOptions = current.popOptions
            if (popOptions != null) { // recursive remove till pop options
                val dest = if (popOptions.all)
                    snapshot.find { it.key == popOptions.dest }
                else
                    snapshot.findLast { it.key == popOptions.dest }
                val index = snapshot.indexOf(dest)
                if (index != -1) {
                    val clamp = if (popOptions.inclusive) index else minOf(index + 1, snapshot.lastIndex)
                    for(i in snapshot.size - 1 downTo clamp) {
                        val removed = snapshot.removeLast()
                        navigator.saveableStateHolder.removeState(removed.key)
                    }
                }
            }
            if (current.singleTop) { // remove duplicates
                val items = snapshot.filter { it.key == destination}
                snapshot.removeAll(items)
                items.forEach { navigator.saveableStateHolder.removeState(it.key) }
            }

            snapshot.add(History.BackStackRecord(destination, current.animationOptions))

            if (!navigator.backStackMap.containsKey(key)) {
                // This should not happen but it happened!
                navigator.backStackMap[key] = history
            }
            navigator.backStackMap.bringToTop(key)

            history.set(snapshot)
        }

        /**
         * @return A snapshot of all the keys associated with the current navigation backStack in the
         * ascending order where the last one being the current screen.
         */
        public fun getAllHistory(): List<T> = history.get().map { it.key }

        /**
         * @return If it safe to go back i.e up the stack. If false then it means the current composable
         *         is the last screen. This also means that the backstack is empty.
         */
        public fun canGoBack(): Boolean = navigator.canGoBack()

        /**
         * Go back to the previous destination.
         *
         * @return The removed key.
         */
        public fun goBack(): T? = navigator.goBack()?.key as? T
    }

    /**
     * Recursive reverse call to [History.canGoBack] to identify if back navigation is possible or not.
     *
     * If possible then the [History.pop] will be called to remove the last item from the backstack.
     */
    private fun goBack(): History.BackStackRecord<out Route>? {
        val last = backStackMap.lastValue()
        if (backStackMap.size > 1 && !last!!.canGoBack()) {
            backStackMap.removeLastOrNull()?.let { saveableStateHolder.removeState(it.initial) }
            return goBack()
        }
        val popped = last?.pop()
        popped?.let { saveableStateHolder.removeState(it.key) }
        last?.let { _ ->
            val currentLastKey = last.get().last().key::class
            var associateKey: KClass<out Route>? = null
            backStackMap.forEach call@{ (key, history) ->
                if (history.associateKey == currentLastKey) {
                    associateKey = key
                    return@call
                }
            }
            associateKey?.let { backStackMap.bringToTop(it) }
        }

        return popped
    }

    /**
     * Recursive reverse call to [History.canGoBack] to identify if back navigation is possible or not.
     */
    internal fun canGoBack(): Boolean {
        val last = backStackMap.lastValue()
        if (backStackMap.size > 1) {
            val aggregate = backStackMap.values.sumOf { it.get().size }
            if (aggregate == backStackMap.size) return false
            return true
        }
        return last?.canGoBack() == true
    }

    private fun onSaveInstance(outState: Bundle) {
        val navigatorBundle = Bundle()
        backStackMap.forEach { (_, v) -> v.saveState(navigatorBundle) }
        outState.putBundle("${activity::class.qualifiedName}$NAVIGATOR_SAVED_STATE_SUFFIX", navigatorBundle)
    }

    private val backPressHandler = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!shouldSuppressBackPress()) goBack()
        }
    }

    private fun shouldSuppressBackPress() : Boolean = suppressBackPress

    internal val backStackMap = mutableMapOf<KClass<out Route>, History<*>>()
    private lateinit var saveableStateHolder: SaveableStateHolder
    private var animationSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 300)
    private var savedState: Bundle? = null

    init {
        activity.onBackPressedDispatcher.addCallback(backPressHandler)
        (activity.applicationContext as Application).registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivityDestroyed(activity: Activity) {}
            override fun onActivitySaveInstanceState(act: Activity, outState: Bundle) {
                if (activity === act) {
                    onSaveInstance(outState)
                }
            }
        })
        // find correct state
        savedState = savedInstanceState?.getBundle("${activity::class.qualifiedName}$NAVIGATOR_SAVED_STATE_SUFFIX")
    }

    private fun<T : Route> fetchOrUpdateHistory(key: KClass<T>, associateKey: KClass<out Route>?, initial: T): History<T> {
        val present = backStackMap.containsKey(key)
        if (!present) {
            // restore from saved state
            val history = History(key, associateKey, initial)
            savedState?.remove(history.restoreState(savedState))
            backStackMap[key] = history
            return history
        }
        return (backStackMap[key] as History<T>)
    }

    /**
     * When enabled, the back navigation will be disabled & no destinations will be popped from the history.
     *
     * This is different from [Builder.setDefaultBackPressEnabled] where if set to `False` then you need to manually handle
     * Activity's `onBackPressed()` logic.
     */
    public var suppressBackPress: Boolean = false

    /**
     * An entry to the navigation composable.
     *
     * Destinations should be managed within [content]. [content] is composable lambda that receives two parameters,
     * [Controller] which is used to manage navigation & [T] which is the current destination.
     *
     * @param key Will be used for uniquely identifying this Route in the composition tree.
     * @param initial The start destination for the navigation.
     *
     * @see Controller
     */
    @Composable
    public fun<T : Route> Setup(modifier: Modifier = Modifier, key: KClass<T>, initial: T, content: @Composable (controller: Controller<T>, dest: T) -> Unit) {
        val associateKey = remember {
            if (backStackMap.isNotEmpty()) {
                backStackMap.lastValue()!!.get().last().key::class
            } else null
        }
        val history = remember { fetchOrUpdateHistory(key, associateKey, initial) }
        val controller = remember { Controller(key, this, history)}
        val compositionLocalScope = rememberComposable { getLocalController(key) }
        if (!::saveableStateHolder.isInitialized) saveableStateHolder = rememberSaveableStateHolder() // is this ok

        @Composable
        fun Inner(body: @Composable () -> Unit) = Box(modifier) { body() }

//        android.util.Log.d("Render", "${key.qualifiedName} Recomposed()/Composed()") TODO: Make sure there is no twice key in the backStack
        Inner {
            // recompose on history change
//            android.util.Log.d("Inner", "Recomposed()/Composed() ${history.peek()} - ${key.qualifiedName}")
            CompositionLocalProvider(compositionLocalScope provides controller, LocalNavigator provides this) {
                val record = history.peek()
                val animation = if (history.lastTransactionStatus == History.NavType.Forward) record.animation else history.getLastRemovedItem()?.animation ?: NavOptions.NavAnimation()
                CommonEffect(targetState = record.key, animation = animation, isBackward = history.lastTransactionStatus == History.NavType.Backward) { peek ->
                    saveableStateHolder.SaveableStateProvider(key = peek) {
                        content(controller, peek)
                    }
                }
            }
            LaunchedEffect(key1 = history.get(), block = {
                backPressHandler.isEnabled = canGoBack() // update if back press is enabled or not.
            })
        }
    }

    @Composable
    private fun <T> CommonEffect(
        targetState: T,
        animation: NavOptions.NavAnimation,
        isBackward: Boolean = false, // if triggered on back press.
        content: @Composable (T) -> Unit
    ) {
        val animationSnapShot = animation.copy()
        val items = remember { mutableStateListOf<CommonAnimationItemHolder<T>>() }
        val transitionState = remember { MutableTransitionState(targetState) }
        val targetChanged = (targetState != transitionState.targetState)
        transitionState.targetState = targetState
        val transition = updateTransition(transitionState, label = "transition")

        if (isBackward) {
            animationSnapShot.enter = animation.exit.reverse()
            animationSnapShot.exit = animation.enter.reverse()
        }

        fun getUpdatedModifier(width: Float, progress: Float, shrinkProgress: Float, key: T): Modifier { // for target 0-1 else 1-0
            return if (key == targetState) { // enter transition
                when(animationSnapShot.enter) {
                    EnterAnimation.FadeIn -> Modifier.graphicsLayer { this.alpha = progress }
                    EnterAnimation.SlideInRight -> Modifier.graphicsLayer { this.translationX = width + (-1) * width * progress }
                    EnterAnimation.SlideInLeft -> Modifier.graphicsLayer { this.translationX = width * (1 - progress) * (-1) }
                    EnterAnimation.ShrinkIn -> Modifier.graphicsLayer { this.alpha = progress; this.scaleX = (1 * shrinkProgress); this.scaleY = (1 * shrinkProgress) }
                    EnterAnimation.None -> Modifier
                    else -> throw IllegalArgumentException("Could not find this animation.")
                }
            } else { // exit transition
                when(animationSnapShot.exit) {
                    ExitAnimation.FadeOut -> Modifier.graphicsLayer { this.alpha = progress }
                    ExitAnimation.SlideOutLeft -> Modifier.graphicsLayer { this.translationX = (-1) * width * (1 - progress) }
                    ExitAnimation.SlideOutRight -> Modifier.graphicsLayer { this.translationX = width * (1 - progress) }
                    ExitAnimation.ShrinkOut -> Modifier.graphicsLayer { this.alpha = progress; this.scaleX = shrinkProgress; this.scaleY = shrinkProgress }
                    ExitAnimation.None -> Modifier
                    else -> throw IllegalArgumentException("Could not find this animation.")
                }
            }
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
                        val progress by transition.animateFloat(
                            transitionSpec = { animationSpec }, label = "normal")
                        { if (it == key) 1f else 0f }

                        val shrinkProgress by transition.animateFloat(
                            transitionSpec = { animationSpec }, label = "shrink")
                        { if (it == key) 1f else 0.9f }
                        val width = with(LocalDensity.current) { maxWidth.toPx() }
                        val internalModifier = getUpdatedModifier(width, progress, shrinkProgress, key)
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
}

private val compositionLocalScopeList = arrayListOf<ProvidableCompositionLocal<*>>() // for memoization
@Composable
private fun<T: Route> getLocalController(key: KClass<T>): ProvidableCompositionLocal<ComposeNavigator.Controller<T>?> {
    compositionLocalScopeList.asReversed().forEach { scope ->
        val controller = scope.current as? ComposeNavigator.Controller<*>
        if (controller?.key == key) return scope as ProvidableCompositionLocal<ComposeNavigator.Controller<T>?>
    }
    val scope = compositionLocalOf<ComposeNavigator.Controller<T>?> { null }
    compositionLocalScopeList.add(scope)
    return scope
}

private val LocalNavigator = staticCompositionLocalOf<ComposeNavigator> { throw Exception("Compose Navigator not set. Did you forgot to call \"Navigator.Setup\"?") }

private fun<K, V> Map<K,V>.lastKey(): K? = keys.last()
private fun<K, V> Map<K,V>.lastValue(): V? = get(keys.last())
private fun<K, V> MutableMap<K,V>.removeLastOrNull(): V? = remove(keys.last())
private fun<K, V> MutableMap<K, V>.bringToTop(key: K) = remove(key)?.let { put(key, it) }

/**
 * Remember the value produced by @[Composable] that does not have a [Unit] return type.
 */
@Composable
private inline fun <T> rememberComposable(calculation: @Composable () -> T): T {
    var internal: T? = null
    if (internal == null) {
        val value = calculation()
        internal = remember { value }
    }
    return internal!!
}