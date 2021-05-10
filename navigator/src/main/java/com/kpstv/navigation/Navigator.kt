package com.kpstv.navigation

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.*
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.kpstv.navigation.internals.*
import com.kpstv.navigation.internals.CustomAnimation
import com.kpstv.navigation.internals.HistoryImpl
import com.kpstv.navigation.internals.NavigatorCircularTransform
import com.kpstv.navigation.internals.prepareForSharedTransition
import kotlin.reflect.KClass

internal typealias FragClazz = KClass<out Fragment>
internal typealias DialogFragClazz = KClass<out Fragment>

@Suppress("unused")
class Navigator internal constructor(private val fm: FragmentManager, private val containerView: FrameLayout) {

    /**
     * @param args Pass arguments extended from [BaseArgs].
     * @param transaction See [TransactionType].
     * @param animation See [NavAnimation].
     * @param remember Remembers the fragment transaction so that [goBack] can navigate to this fragment again on back press or by calling manually. Equivalent to addToBackStack in fragment transaction.
     * @param historyOptions Manipulate history during navigating to [Fragment] by specifying any one of [HistoryOptions].
     */
    data class NavOptions(
        val args: BaseArgs? = null,
        val transaction: TransactionType = TransactionType.REPLACE,
        val animation: NavAnimation = AnimationDefinition.None,
        val remember: Boolean = false,
        val historyOptions: HistoryOptions = HistoryOptions.None,
    )

    private var primaryFragClass: FragClazz? = null
    private var hasPrimaryFragment: Boolean = false

    private val navigatorTransitionManager = NavigatorCircularTransform(fm, containerView)
    private val history = HistoryImpl(fm)

    private val simpleNavigator = SimpleNavigator(containerView.context, fm)

    /**
     * Sets the default fragment as the host. The [FragmentManager.popBackStack] will be called recursively
     * with proper [ValueFragment.onBackPressed] till it finds the primary fragment.
     *
     * It will first check if the fragment exists in the backStack otherwise it will create a new one.
     *
     * In short it should be the last fragment in the host so that back press will finish the activity.
     *
     * @hide Need a good reason to expose this API
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    private fun setPrimaryFragment(clazz: FragClazz) {
        this.primaryFragClass = clazz
    }

    /**
     * Navigate to [Fragment]
     *
     * @param clazz Fragment class to which it should navigate.
     * @param navOptions Optional navigation options you can specify.
     */
    fun navigateTo(clazz: FragClazz, navOptions: NavOptions = NavOptions()) = with(navOptions) options@{
        val newFragment = fm.newFragment(containerView.context, clazz)
        if (newFragment is DialogFragment) show(clazz, args) // delegate to dialog navigation
        val tagName = history.getUniqueBackStackName(clazz)

        // any one of the following will be true
        val clearAllHistory = historyOptions is HistoryOptions.ClearHistory
        val singleTop = historyOptions is HistoryOptions.SingleTopInstance

        if (animation is AnimationDefinition.CircularReveal) {
            val oldPayload = animation as? AnimationDefinition.CircularReveal
            val payload = AnimationDefinition.CircularReveal(
                forFragment = oldPayload?.forFragment ?: clazz,
                fromTarget = oldPayload?.fromTarget
            )
            navigatorTransitionManager.circularTransform(payload, clearAllHistory)
        }
        val bundle = createArguments(args)
        // Enqueue a clear history operation
        if (clearAllHistory) {
            history.clearAll()
        }
        // Remove duplicate backStack entry name & add it again if exist.
        // Useful when fragment is navigating to self or maintaining single instance.
        val innerAddToBackStack = if (singleTop) {
            history.clearUpTo(clazz, true)
        } else false
        // Pop to fragment or backstack.
        if (historyOptions is HistoryOptions.PopToFragment) {
            history.clearUpTo(historyOptions.clazz, all = historyOptions.all)
        } else if (historyOptions is HistoryOptions.PopToBackStack) {
            history.clearUpTo(historyOptions.name, historyOptions.inclusive)
        }

        fm.commit {
            if (animation is AnimationDefinition.Custom)
                CustomAnimation(fm, containerView).set(this, animation, clazz)
            if (animation is AnimationDefinition.Shared)
                prepareForSharedTransition(fm, containerView, clazz, animation)

            val sameFragment = fm.findFragmentByTag(tagName)
            if (sameFragment != null && sameFragment::class != clazz) {
                // (maybe) should popUp it's childFragmentManager in this case.
                show(sameFragment)
            } else {
                newFragment.arguments = bundle
                when (transaction) {
                    TransactionType.REPLACE -> replace(containerView.id, newFragment, tagName)
                    TransactionType.ADD -> add(containerView.id, newFragment, tagName)
                }
            }
            setPrimaryNavigationFragment(newFragment) // needed to avoid creating fragment after clearing all history
            // Cannot add to back stack when popUpTo is true
            if (!clearAllHistory && (remember || innerAddToBackStack)) {
                history.add(BackStackRecord(tagName, clazz))
                addToBackStack(tagName)
            }
            setReorderingAllowed(true)
        }
    }

    /**
     * @see SimpleNavigator.show
     */
    fun show(clazz: DialogFragClazz, args: BaseArgs? = null) {
        simpleNavigator.show(clazz, args)
    }

    /**
     * @see History
     */
    fun getHistory(): History = history

    /**
     * Determines if "it can go back" aka backStack is empty or not.
     *
     * The call is recursive through child [Fragment]s that uses [Navigator].
     * By collecting information throughput it will return True or False.
     *
     * @return True means it is safe to [goBack].
     */
    @Suppress("RedundantIf")
    fun canGoBack(): Boolean {
        val count = getBackStackCount()
        if (count == 0) {
            val fragment = getCurrentFragment() ?: return false
            if (fragment is DialogFragment) {
                return true
            }else if (fragment is ValueFragment && fragment.forceBackPress) {
                return true
            } else if (fragment is NavigatorTransmitter) {
                return fragment.getNavigator().canGoBack()
            } else {
                return false
            }
        } else {
            return true
        }
    }

    /**
     * Remove the latest entry from [FragmentManager]'s backStack.
     *
     * The call is recursive to child [Fragment]s, in this way their
     * [ValueFragment.onBackPressed] are also called which returns if the backPress
     * is consumed or not. If consumed then it means child [Fragment] don't want the
     * parent [Navigator] to go back, hence [goBack] will return false
     * & no entry will be removed from the current [FragmentManager].
     *
     * @return True if the entry is removed.
     */
    fun goBack(): Boolean {
        val clazz = primaryFragClass
        if (clazz != null && !hasPrimaryFragment) {
            hasPrimaryFragment =
                fm.fragments.any { it::class.qualifiedName == primaryFragClass?.qualifiedName }
        }
        if (!canGoBack() && clazz != null && !hasPrimaryFragment) {
            // Create primary fragment
            navigateTo(clazz, NavOptions(animation = AnimationDefinition.Fade))
            return false
        }
        val currentFragment = getCurrentFragment()

        // Dialog fragment
        if (currentFragment is DialogFragment && simpleNavigator.isLastFragment(currentFragment)) {
            return simpleNavigator.pop()
        }

        val shouldPopStack = if (currentFragment is ValueFragment) {
            !currentFragment.onBackPressed()
        } else {
            true
        }
        if (shouldPopStack) {
            history.pop()
        }
        return shouldPopStack
    }

    internal fun restoreState(bundle: Bundle?) {
        history.onRestoreState(bundle)
        val simpleNavigatorState = bundle?.getBundle(SimpleNavigator::class.qualifiedName)
        simpleNavigator.restoreState(simpleNavigatorState)
    }

    internal fun onSaveInstance(bundle: Bundle) {
        val simpleNavigatorState = Bundle()
        simpleNavigator.saveState(simpleNavigatorState)
        bundle.putBundle(SimpleNavigator::class.qualifiedName, simpleNavigatorState)
        history.onSaveState(bundle)
    }

    /**
     * Returns the current fragment class.
     */
    fun getCurrentFragment() = getCurrentVisibleFragment(fm, containerView)

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getFragmentManager(): FragmentManager = fm

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getContainerView(): FrameLayout = containerView

    private fun getBackStackCount(): Int = fm.backStackEntryCount

    /**
     * Transactions like Add, Replace can be specified.
     */
    enum class TransactionType {
        REPLACE,
        ADD
    }

    abstract class Navigation {
        /**
         * Set the first selected fragment. Defaults to the first Id from the navigation fragments.
         *
         */
        open val selectedFragmentId: Int = -1

        /**
         * Specifies the animation to run when bottom navigation selection is changed.
         * @see Animation
         */
        open val fragmentNavigationTransition: Animation = Animation.None

        /**
         * @see ViewRetention
         */
        open val fragmentViewRetentionType: ViewRetention = ViewRetention.RECREATE

        /**
         * Implement this interface on child fragments to get notified
         * about selection or re-selection.
         */
        interface Callbacks {
            fun onSelected() {}
            fun onReselected() {}
        }

        /**
         * The class has predefined set of animations or you can create a new one by instantiating.
         */
        open class Animation(@AnimRes @AnimatorRes val enter: Int, @AnimRes @AnimatorRes val exit: Int) {
            object None : Animation(-1,-1)
            object Fade : Animation(R.anim.navigator_fade_in, R.anim.navigator_fade_out)
            object Slide : Animation(-1,-1) // we will use a custom one
        }

        enum class Transition {
            NONE,
            FADE,
            SLIDE
        }

        /**
         * Determines the fragment's view retention mode.
         * @see RECREATE
         * @see RETAIN
         */
        enum class ViewRetention {
            /**
             * The fragment view will be destroyed once the current selection fragment has be changed in the container.
             *
             * The fragment will through all of the necessary lifecycle it has to.
             */
            RECREATE,

            /**
             * The fragment view will not be destroyed once the current selection fragment is changed.
             *
             * This is done via hiding the fragment in the container. Since it does not replace the underlying fragment
             * the old fragment will not go through the lifecycle changes. Hence no [onPause], [onStop], [onDestroyView]
             * & so on will be called.
             *
             * The only way to rely on view state change is to listen [ViewStateFragment.onViewStateChanged].
             */
            RETAIN
        }
    }

    abstract class TabNavigation : Navigation() {
        /**
         * A list of [Fragment] class. The list order correspond to the order of
         * [TabItem] in the [TabLayout].
         */
        abstract val tabNavigationFragments: List<KClass<out Fragment>>

        /**
         * The TabLayout View Id.
         */
        abstract val tabLayoutId: Int

        open fun onTabNavigationSelectionChanged(position: Int) {}
    }

    abstract class BottomNavigation : Navigation() {
        /**
         * A map of the Id of bottom navigation menu resource to [Fragment] class.
         */
        abstract val bottomNavigationFragments: Map<Int, KClass<out Fragment>>

        /**
         * The Navigation View Id.
         */
        abstract val bottomNavigationViewId: Int

        open fun onBottomNavigationSelectionChanged(@IdRes selectedId: Int) {}
    }

    internal lateinit var owner: Any // Will be used to query if installed in Activity or Fragment.
    internal var savedInstanceState: Bundle? = null // Just for restoring state in other parts of library module, really missing package-private feature in Kotlin.
    class Builder internal constructor(
        private val fragmentManager: FragmentManager,
        private val savedInstanceState: Bundle?
    ) {
        private lateinit var navigator: Navigator
        private lateinit var owner: Any

        fun initialize(containerView: FrameLayout) : Navigator {
            navigator = Navigator(fragmentManager, containerView)
            navigator.owner = owner
            navigator.savedInstanceState = savedInstanceState
            navigator.restoreState(savedInstanceState)
            return navigator
        }

        internal fun set(activity: FragmentActivity) {
            owner = activity
            activity.application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivitySaveInstanceState(act: Activity, outState: Bundle) {
                    if (activity === act && ::navigator.isInitialized) {
                        navigator.onSaveInstance(outState)
                        act.application.unregisterActivityLifecycleCallbacks(this)
                    }
                }
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivityDestroyed(activity: Activity) {}
            })
        }

        internal fun set(fragment: Fragment) {
            owner = fragment
            fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentSaveInstanceState(fm: FragmentManager, frag: Fragment, outState: Bundle) {
                    if (fragment === frag && ::navigator.isInitialized) {
                        navigator.onSaveInstance(outState)
                        fm.unregisterFragmentLifecycleCallbacks(this)
                    }
                    super.onFragmentSaveInstanceState(fm, frag, outState)
                }
            }, false)
        }
    }

    companion object {
        /**
         * Returns a builder for creating an instance of Navigator.
         */
        fun with(activity: FragmentActivity, savedInstanceState: Bundle?): Builder {
            return Builder(activity.supportFragmentManager, savedInstanceState).apply { set(activity) }
        }

        /**
         * Returns a builder for creating an instance of Navigator.
         */
        fun with(fragment: Fragment, savedInstanceState: Bundle?): Builder {
            return Builder(fragment.childFragmentManager, savedInstanceState).apply { set(fragment) }
        }

        internal fun getCurrentVisibleFragment(fm: FragmentManager, containerView: FrameLayout): Fragment? {
            val fragment = fm.findFragmentById(containerView.id)
            if (fragment != null) {
                if (fragment.isVisible) return fragment
                // Reverse because if there are two or more visible fragments then the last
                // one in container will be the one visible (interactive) to user.
                fm.fragments.reversed().forEach { frag -> if (frag.isVisible) return frag }
            }
            return null
        }

        internal fun getFragmentTagName(clazz: FragClazz): String = clazz.java.simpleName + FRAGMENT_SUFFIX

        internal fun createArguments(args: BaseArgs?) = Bundle().apply {
            if (args != null)
                putParcelable(ValueFragment.ARGUMENTS, args)
        }

        private const val FRAGMENT_SUFFIX = "_navigator"
    }
}