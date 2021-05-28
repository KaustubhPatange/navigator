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
import androidx.lifecycle.*
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
class FragmentNavigator internal constructor(private val fm: FragmentManager, private val containerView: FrameLayout) : Navigator {

    /**
     * @param args Pass arguments extended from [BaseArgs].
     * @param transaction See [TransactionType].
     * @param animation See [NavAnimation].
     * @param remember Remembers the fragment transaction so that [goBack] can navigate back to this (current) fragment on back press or by calling manually from the new fragment. Equivalent to addToBackStack in a fragment transaction.
     * @param historyOptions Manipulate history during navigating to [Fragment] by specifying any one of [HistoryOptions].
     */
    data class NavOptions(
        val args: BaseArgs? = null,
        val transaction: TransactionType = TransactionType.REPLACE,
        val animation: NavAnimation = AnimationDefinition.None,
        val remember: Boolean = false,
        val historyOptions: HistoryOptions = HistoryOptions.None,
    )

    /**
     * The host must implement this interface to propagate Navigator
     * to the child fragments. This ensures the correct behavior of
     * back press.
     */
    interface Transmitter {
        fun getNavigator(): FragmentNavigator
    }

    private val navigatorTransitionManager = NavigatorCircularTransform(fm, containerView)
    private val history = HistoryImpl(fm)

    private val simpleNavigator = SimpleNavigator(containerView.context, fm)

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
        // Maintaining single instance.
        val innerAddToBackStack = if (singleTop) {
            history.clearUpTo(clazz, all = true)
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
            setPrimaryNavigationFragment(newFragment)
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
    fun show(clazz: DialogFragClazz, args: BaseArgs? = null, onDismissListener: DialogDismissListener? = null) {
        simpleNavigator.show(clazz, args, onDismissListener)
    }

    /**
     * @see History
     */
    fun getHistory(): History = history

    /**
     * Determines if "it can go back" aka backStack is empty or not.
     *
     * The call is recursive through child [Fragment]s that uses [FragmentNavigator].
     * By collecting information throughput it will return True or False.
     *
     * @return True means it is safe to [goBack].
     */
    @Suppress("RedundantIf")
    override fun canGoBack(): Boolean {
        val count = getBackStackCount() // or history count if that's something should be done.
        if (count == 0) {
            val fragment = getCurrentFragment() ?: return false
            if (fragment is DialogFragment) return true
            if (fragment is Transmitter && fragment.getNavigator().canGoBack()) return true
            if (fragment is ValueFragment && fragment.forceBackPress) return true
            return false
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
     * parent [FragmentNavigator] to go back, hence [goBack] will return false
     * & no entry will be removed from the current [FragmentManager].
     *
     * @return True if the entry is removed.
     */
    override fun goBack(): Boolean {
        val currentFragment = getCurrentFragment()

        // Dialog fragment
        if (currentFragment is DialogFragment) {
            return simpleNavigator.pop()
        }

        if (currentFragment is Transmitter && currentFragment.getNavigator().canGoBack()) {
            return currentFragment.getNavigator().goBack()
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

    internal fun onSaveInstance(bundle: Bundle) {
        val save = Bundle()
        history.onSaveState(owner.toIdentifier(), save)
        simpleNavigator.saveState(owner.toIdentifier(), save)
        bundle.putBundle("$NAVIGATOR_STATE:${owner.toIdentifier()}", save)
    }

    internal fun restoreState(bundle: Bundle?) {
        val save = bundle?.getBundle("$NAVIGATOR_STATE:${owner.toIdentifier()}") ?: return
        history.onRestoreState(owner.toIdentifier(), save)
        simpleNavigator.restoreState(owner.toIdentifier(), save)
    }

    /**
     * Returns the current visible fragment from the [FragmentManager].
     *
     * It could be [DialogFragment] if currently being shown or [Fragment] from [containerView]
     */
    fun getCurrentFragment(): Fragment? {
        return simpleNavigator.getCurrentDialogFragment() ?: getCurrentVisibleFragment(fm, containerView)
    }

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

    // friend path mechanism will help here.
    internal lateinit var owner: Any // Will be used to query if installed in Activity or Fragment.
    internal var savedInstanceState: Bundle? = null // Just for restoring state in other parts of library module eg: bottom/tab.

    private lateinit var stateViewModel: StateViewModel

    class Builder internal constructor(
        private val fragmentManager: FragmentManager,
        private val savedInstanceState: Bundle?
    ) {
        private lateinit var navigator: FragmentNavigator
        private lateinit var owner: Any
        private lateinit var stateViewModelKey: String

        fun initialize(containerView: FrameLayout) : FragmentNavigator {
            this.stateViewModelKey = "navigator_${containerView.id}"
            navigator = FragmentNavigator(fragmentManager, containerView)
            navigator.owner = owner
            navigator.stateViewModel = ViewModelProvider(owner as ViewModelStoreOwner, StateViewModel.Factory()).get(SAVE_STATE_MODEL, StateViewModel::class.java)
            navigator.savedInstanceState = savedInstanceState
            if (savedInstanceState != null) {
                navigator.restoreState(savedInstanceState)
            } else {
                val bundle = navigator.stateViewModel.getHistory(stateViewModelKey)
                if (bundle != null && !bundle.isEmpty) {
                    navigator.restoreState(bundle)
                }
            }
            return navigator
        }

        internal fun set(activity: FragmentActivity) {
            owner = activity
            activity.application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivitySaveInstanceState(act: Activity, outState: Bundle) {
                    if (activity === act && ::navigator.isInitialized) {
                        navigator.onSaveInstance(outState)
                        val bundle = Bundle().apply {
                            navigator.onSaveInstance(this)
                        }
                        navigator.stateViewModel.putHistory(stateViewModelKey, bundle)
                    }
                }
                override fun onActivityDestroyed(act: Activity) {
                    if (act === act) act.application.unregisterActivityLifecycleCallbacks(this)
                }
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
            })
        }

        internal fun set(fragment: Fragment, parentFragmentManager: FragmentManager) {
            owner = fragment
            parentFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentSaveInstanceState(fm: FragmentManager, frag: Fragment, outState: Bundle) {
                    if (fragment === frag && ::navigator.isInitialized) {
                        navigator.onSaveInstance(outState)
                    }
                    super.onFragmentSaveInstanceState(fm, frag, outState)
                }
                override fun onFragmentStopped(fm: FragmentManager, frag: Fragment) {
                    if (fragment === frag && ::navigator.isInitialized) {
                        val bundle = Bundle()
                        navigator.onSaveInstance(bundle)
                        navigator.stateViewModel.putHistory(stateViewModelKey, bundle)
                    }
                    super.onFragmentStopped(fm, frag)
                }
                override fun onFragmentDestroyed(fm: FragmentManager, frag: Fragment) {
                    if (frag === owner) {
                        fm.unregisterFragmentLifecycleCallbacks(this)
                    }
                    super.onFragmentDestroyed(fm, frag)
                }
            }, false)
        }

        companion object {
            private const val SAVE_STATE_MODEL = "com.kpstv.navigation:save_state_viewModel"
        }
    }

    companion object {
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
            if (args != null) {
                putParcelable(ValueFragment.createArgKey(args), args)
            }
        }

        private const val FRAGMENT_SUFFIX = "_navigator"
        private const val NAVIGATOR_STATE = "com.kpstv.navigator:state"
    }
}