@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.kpstv.navigator.compose

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
import com.kpstv.navigator.compose.EnterAnimation.Companion.reverse
import com.kpstv.navigator.compose.ExitAnimation.Companion.reverse
import kotlinx.parcelize.Parcelize
import kotlin.IllegalArgumentException
import kotlin.reflect.KClass

/**
 * Find the [ComposeNavigator] provided by the nearest CompositionLocalProvider.
 */
@Composable
public fun findComposeNavigator() : ComposeNavigator = LocalNavigator.current

/**
 * Find the [ComposeNavigator.Controller] provided by the nearest CompositionLocalProvider.
 */
@Composable
public fun <T : Parcelable> findController() : ComposeNavigator.Controller<T> = LocalController.current as ComposeNavigator.Controller<T>

/**
 * Each destination must implement this interface to provide correct mechanism of SaveStateRegistry.
 */
public interface Route {
    /**
     * The key used for the SaveableStateProvider. It should be unique & of type that can be saved in a bundle.
     */
    public val saveableStateProviderKey: Any get() = this::class.qualifiedName!!
}

/**
 * @param singleTop Ensures that there will be only once instance of this destination in the stack.
 *                  If there would be a previous one, then it would be brought front.
 */
public data class NavOptions<T : Parcelable>(
    var singleTop: Boolean = false,
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
    public data class PopUpOptions<T : Parcelable>(internal var dest: T, var inclusive: Boolean = true, var all: Boolean = false)

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
 * A navigator for Jetpack Compose.
 */
public class ComposeNavigator(private val activity: ComponentActivity, savedInstanceState: Bundle?) {
    private companion object {
        private const val HISTORY_SAVED_STATE = "compose_navigator:state:"
        private const val NAVIGATOR_SAVED_STATE_SUFFIX = "_compose_navigator"
    }

    //TODO: A builder to set custom Animation Spec, Make primary destination feature which will be navigated automatically when backstack becomes empty...
    internal class History<T : Parcelable> internal constructor(private val initial: T) {
        companion object {
            private const val LAST_REMOVED_ITEM_KEY = "history:last_item:key"
            private const val LAST_REMOVED_ITEM_ANIMATION = "history:last_item:animation"
        }
        internal enum class NavType { Forward, Backward }

        // TODO: Wait for Kotlin 1.5.20 (KT-42652). Make this parcelable for saving states.
        internal data class BackStackRecord<T: Parcelable>(val key: T, val animation: NavOptions.NavAnimation = NavOptions.NavAnimation())

        private var backStack by mutableStateOf(listOf(BackStackRecord(initial)))
        private var lastRemoved: BackStackRecord<T>? = null

        internal var lastTransactionStatus: NavType = NavType.Forward
        internal var isRestoredFromSavedInstance: Boolean = false

        internal fun getInitialKey(): T = initial

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
                val name = "$HISTORY_SAVED_STATE${initial::class.qualifiedName}"
                outState.putBundle(name, bundle)
            }
        }

        internal fun restoreState(bundle: Bundle?): String? {
            val name = "$HISTORY_SAVED_STATE${initial::class.qualifiedName}"
            bundle?.getBundle(name)?.let { inner ->
                val keys: List<T> = inner.getParcelableArrayList(BackStackRecord<T>::key.name)!!
                val animations: List<NavOptions.NavAnimation> = inner.getParcelableArrayList(BackStackRecord<T>::animation.name)!!

                val lastKey: T? = inner.getParcelable(LAST_REMOVED_ITEM_KEY)
                val lastAnimation: NavOptions.NavAnimation? = inner.getParcelable(LAST_REMOVED_ITEM_ANIMATION)
                if (lastKey != null && lastAnimation != null) {
                    lastRemoved = BackStackRecord(lastKey, lastAnimation)
                }

                isRestoredFromSavedInstance = true
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
    public class Controller<T : Parcelable> internal constructor(private val key: KClass<out Parcelable>, private val navigator: ComposeNavigator, private val history: History<T>) {

        /**
         * Navigate to other destination composable. Additional parameters can be set through [options] DSL.
         */
        public fun navigateTo(destination: T, options: NavOptions<T>.() -> Unit = {}) {
            val current = NavOptions<T>().apply(options)
            var snapshot = ArrayList(history.get())

            val popOptions = current.popOptions
            if (popOptions != null) { // recursive remove till pop options // TODO: Remove saveableStateholder here as well.
                val dest = if (popOptions.all)
                    snapshot.find { it.key == popOptions.dest }
                else
                    snapshot.findLast { it.key == popOptions.dest }
                val index = snapshot.indexOf(dest)
                if (index != -1) {
                    val clamp = if (popOptions.inclusive) index else minOf(index + 1, snapshot.lastIndex)
                    snapshot = ArrayList(snapshot.subList(0, clamp))
                }
            }
            if (current.singleTop) { // remove duplicates
                snapshot.removeAll { it.key == destination}
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

    private fun goBack(): History.BackStackRecord<out Parcelable>? {
        val last = backStackMap.lastValue()
        if (backStackMap.size > 1 && !last!!.canGoBack()) {
            backStackMap.removeLastOrNull()?.let { saveableStateHolder.removeState((it.getInitialKey() as Route).saveableStateProviderKey) }
            return goBack()
        }
        val popped = last?.pop()
        popped?.let { saveableStateHolder.removeState((it.key as Route).saveableStateProviderKey) }
        return popped
    }

    private fun canGoBack(): Boolean {
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
            if (isBackPressedEnabled()) goBack()
        }
    }

    @JvmName("isBackEnabled")
    private fun isBackPressedEnabled() : Boolean = isBackPressedEnabled

    private val backStackMap = mutableMapOf<KClass<out Parcelable>, History<*>>()
    private lateinit var saveableStateHolder: SaveableStateHolder
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

    private fun<T : Parcelable> fetchOrUpdateHistory(clazz: KClass<out Parcelable>, initial: T): History<T> {
        val present = backStackMap.containsKey(clazz)
        if (!present) {
            // restore from saved state
            val history = History(initial)
            savedState?.remove(history.restoreState(savedState))
            backStackMap[clazz] = history
            return history
        }
        return (backStackMap[clazz] as History<T>)
    }

    /**
     * Should back button enqueue a back navigation operation.
     */
    public var isBackPressedEnabled: Boolean = true

    /**
     * An entry to the navigation composable.
     *
     * Destinations should be managed within [content]. [content] is composable lambda that receives two parameters,
     * [Controller] which is used to manage navigation & [T] which is the current destination.
     *
     * @param initial The start destination for the navigation.
     *
     * @see Controller
     */
    @Composable
    public fun<T : Parcelable> Setup(modifier: Modifier = Modifier, initial: T, content: @Composable (controller: Controller<T>, dest: T) -> Unit) {
        val history = remember { fetchOrUpdateHistory(initial::class, initial) }
        val controller = remember { Controller(initial::class, this, history) }
        if (!::saveableStateHolder.isInitialized) saveableStateHolder = rememberSaveableStateHolder() // is this ok

        @Composable
        fun Inner(body: @Composable () -> Unit) = Box(modifier) { body() }

        @Composable
        fun<T: Parcelable> Compose(key: T, content: @Composable () -> Unit) {
            if (key is Route) {
                saveableStateHolder.SaveableStateProvider(key = key.saveableStateProviderKey) { content() }
            } else {
                throw TypeNotPresentException(Route::class.qualifiedName, Throwable("The destination $key must implement \"ScreenRoute\" interface."))
            }
        }

//        Log.d("Render", "${initial::class.qualifiedName} Recomposed()/Composed()")
        Inner {
            // recompose on history change
//            Log.d("Inner", "Recomposed()/Composed() ${history.peek()} - ${initial::class.qualifiedName}")
            CompositionLocalProvider(LocalController provides controller, LocalNavigator provides this) {
                val record = history.peek()
                val animation = if (history.lastTransactionStatus == History.NavType.Forward) record.animation else history.getLastRemovedItem()?.animation ?: NavOptions.NavAnimation()
                CommonEffect(targetState = record.key, animation = animation, isBackward = history.lastTransactionStatus == History.NavType.Backward) { peek ->
                    Compose(peek) { content(LocalController.current as Controller<T>, peek) }
                }

                // TODO: Waiting for #191059138
                /*if (history.isRestoredFromSavedInstance) {
                    Log.e("Inner", "Restored from saved instance: $record")
                    Compose(record.key) { content(LocalController.current as Controller<T>, record.key) }
                } else {
                    Log.e("Inner", "Normal invocation: $record")
                    CommonEffect(targetState = record.key, animation = animation, isBackward = history.lastTransactionStatus == History.NavType.Backward) { peek ->
                        Compose(peek) { content(LocalController.current as Controller<T>, peek) }
                    }
                }*/
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
        animationSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 300),
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

private val LocalController = compositionLocalOf<ComposeNavigator.Controller<out Parcelable>> { throw Exception("NavController not set.") }
private val LocalNavigator = compositionLocalOf<ComposeNavigator> { throw Exception("Compose Navigator not set.") }

private fun<K, V> Map<K,V>.lastKey(): K? = keys.last()
private fun<K, V> Map<K,V>.lastValue(): V? = get(keys.last())
private fun<K, V> MutableMap<K,V>.removeLastOrNull(): V? = remove(keys.last())
private fun<K, V> MutableMap<K, V>.bringToTop(key: K) = remove(key)?.let { put(key, it) }

private fun<T> Bundle.consumeKey(key: String): T? {
    val item = get(key) as T
    remove(key)
    return item
}