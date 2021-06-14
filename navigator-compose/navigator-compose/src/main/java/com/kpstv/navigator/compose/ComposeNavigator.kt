@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.kpstv.navigator.compose

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
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

private val LocalController = compositionLocalOf<ComposeNavigator.Controller<out Parcelable>> { throw Exception("NavController not set.") }
private val LocalNavigator = compositionLocalOf<ComposeNavigator> { throw Exception("Compose Navigator not set.") }

private fun<K, V> Map<K,V>.last(): V? = get(keys.last())
private fun<K, V> MutableMap<K,V>.removeLastOrNull(): V? = remove(keys.last())
private fun<K, V> MutableMap<K, V>.bringToTop(key: K) = remove(key)?.let { put(key, it) }

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
 * @param singleTop Ensures that there will be only once instance of this destination in the stack.
 *                  If there would be a previous one, then it would be brought front.
 * @param animation A DSL to set animations.
 */
public data class NavOptions<T>(
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
    public data class PopUpOptions<T>(internal var dest: T, var inclusive: Boolean = true, var all: Boolean = false)

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
public class ComposeNavigator(private val activity: ComponentActivity, private val savedInstanceState: Bundle?) {
    private companion object {
        private const val HISTORY_SAVED_STATE = "compose_navigator:state:"
        private const val NAVIGATOR_SAVED_STATE_SUFFIX = "_compose_navigator"
    }

    //TODO: A builder to set custom Animation Spec
    internal class History<T : Parcelable> internal constructor(private val initial: T) {
        internal enum class NavType { Forward, Backward }

        private var backStack by mutableStateOf(listOf(initial))
        internal var animationDefinition = arrayListOf(NavOptions.NavAnimation())

        internal var lastTransactionStatus = NavType.Forward

        internal fun get(): List<T> =
            backStack

        internal fun set(elements: List<T>) {
            if (elements != backStack) {
                lastTransactionStatus = NavType.Forward
                backStack = elements
            }
        }

        internal fun peek(): Pair<T, NavOptions.NavAnimation> = backStack.last() to animationDefinition.last()
        internal fun pop(): Boolean {
            if (canGoBack()) { // last item will not be popped
                lastTransactionStatus = NavType.Backward
                backStack = backStack.subList(0, backStack.lastIndex)
                return true
            }
            return false
        }
        internal fun canGoBack(): Boolean = backStack.size > 1

        internal fun saveState(outState: Bundle) {
            if (backStack.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putParcelableArrayList(::backStack.name, ArrayList(backStack))
                    putParcelableArrayList(::animationDefinition.name, animationDefinition)
                }
                val name = "$HISTORY_SAVED_STATE${initial::class.qualifiedName}"
                outState.putBundle(name, bundle)
            }
        }

        internal fun restoreState(bundle: Bundle?) {
            val name = "$HISTORY_SAVED_STATE${initial::class.qualifiedName}"
            bundle?.getBundle(name)?.let { inner ->
                animationDefinition = inner.getParcelableArrayList(::animationDefinition.name)!!
                backStack = inner.getParcelableArrayList(::backStack.name)!!
            }
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
            if (popOptions != null) { // recursive remove till pop options
                val dest = if (popOptions.all)
                    snapshot.find { it == popOptions.dest }
                else
                    snapshot.findLast { it == popOptions.dest }
                val index = snapshot.indexOf(dest)
                if (index != -1) {
                    snapshot = ArrayList(snapshot.subList(0, index))
                }
            }
            if (current.singleTop) { // remove duplicates
                snapshot.removeAll { it == destination}
            }
            snapshot.add(destination)

            if (!navigator.backStackMap.containsKey(key)) {
                // This should not happen but it happened!
                navigator.backStackMap[key] = history
            }
            navigator.backStackMap.bringToTop(key)

            history.animationDefinition.add(current.animationOptions)
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
         * @return true if it went up the stack.
         */
        public fun goBack(): Boolean = navigator.goBack()
    }

    private fun goBack(): Boolean {
        val last = backStackMap.last()
        if (backStackMap.size > 1 && !last!!.canGoBack()) {
            backStackMap.removeLastOrNull()
            return goBack()
        }
        return last?.pop() == true
    }

    private fun canGoBack(): Boolean {
        val last = backStackMap.last()
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
    }

    private fun<T : Parcelable> fetchOrUpdateHistory(clazz: KClass<out Parcelable>, initial: T): History<T> {
        val present = backStackMap.containsKey(clazz)
        if (!present) {
            // update from saved instance state
            val bundle = savedInstanceState?.getBundle("${activity::class.qualifiedName}$NAVIGATOR_SAVED_STATE_SUFFIX")
            val history = History(initial)
            history.restoreState(bundle)
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
        val saveableStateHolder = rememberSaveableStateHolder()

        @Composable
        fun Inner(body: @Composable () -> Unit) = Box(modifier) { body() }
//        Log.i("Render", "${initial::class.qualifiedName} Recomposed()/Composed()")
        Inner {
            // recompose on history change
//            Log.i("Inner", "Recomposed()/Composed() ${history.peek()} - ${initial::class.qualifiedName}")
            CompositionLocalProvider(LocalController provides controller, LocalNavigator provides this) {
                val current = history.peek()
                CommonEffect(targetState = current.first, animation = current.second, isBackward = history.lastTransactionStatus == History.NavType.Backward) { peek ->
                    saveableStateHolder.SaveableStateProvider(key = peek) {
                        content(LocalController.current as Controller<T>, peek)
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
                    val progress by transition.animateFloat(
                        transitionSpec = { animationSpec }, label = "normal")
                    { if (it == key) 1f else 0f }

                    val shrinkProgress by transition.animateFloat(
                        transitionSpec = { animationSpec }, label = "shrink")
                    { if (it == key) 1f else 0.9f }

                    BoxWithConstraints {
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

        Box {
            items.fastForEach {
                key(it.key) {
                    it.content()
                }
            }
        }
    }

    private data class CommonAnimationItemHolder<T>(
        val key: T,
        val content: @Composable () -> Unit
    )
}