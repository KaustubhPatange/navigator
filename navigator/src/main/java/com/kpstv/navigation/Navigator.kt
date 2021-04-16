package com.kpstv.navigation

import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.kpstv.navigation.internals.NavigatorCircularTransform
import com.kpstv.navigation.internals.prepareForSharedTransition
import kotlin.reflect.KClass

internal typealias FragClazz = KClass<out Fragment>

class Navigator(private val fm: FragmentManager, private val containerView: FrameLayout) {

    data class NavOptions(
        val clazz: FragClazz,
        val args: BaseArgs? = null,
        val type: TransactionType = TransactionType.REPLACE,
        val animation: NavAnimation = AnimationDefinition.None,
        val addToBackStack: Boolean = false,
        val popUpToThis: Boolean = false
    )

    private var primaryFragClass: FragClazz? = null
    private var hasPrimaryFragment: Boolean = false

    private val navigatorTransitionManager = NavigatorCircularTransform(fm, containerView)

    /**
     * Sets the default fragment as the host. The [FragmentManager.popBackStack] will be called recursively
     * with proper [ValueFragment.onBackPressed] till it finds the primary fragment.
     *
     * It will first check if the fragment exists in the backStack otherwise it will create a new one.
     *
     * In short it should be the last fragment in the host so that back press will finish the activity.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun setPrimaryFragment(clazz: FragClazz) {
        this.primaryFragClass = clazz
    }

    /**
     * A fragment transaction.
     *
     * See: [NavOptions]
     */
    fun navigateTo(navOptions: NavOptions) = with(navOptions) options@{
        val newFragment = clazz.java.getConstructor().newInstance()
        val tagName = if (newFragment is ValueFragment && newFragment.backStackName != null) {
            newFragment.backStackName
        } else getFragmentTagName(clazz)

        if (animation is AnimationDefinition.CircularReveal) {
            val oldPayload = animation as? AnimationDefinition.CircularReveal
            val payload = AnimationDefinition.CircularReveal(
                forFragment = oldPayload?.forFragment ?: clazz,
                fromTarget = oldPayload?.fromTarget
            )
            navigatorTransitionManager.circularTransform(payload, popUpToThis)
        }
        val bundle = Bundle().apply {
            if (args != null)
                putParcelable(ValueFragment.ARGUMENTS, args)
        }
        // Enqueue a popUpTo operation
        if (popUpToThis && fm.backStackEntryCount > 1) {
            val to = fm.getBackStackEntryAt(fm.backStackEntryCount - 2).name
            fm.popBackStack(to, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        // Remove duplicate backStack entry name & add it again if exist.
        // Useful when fragment is navigating to self.
        var innerAddToBackStack = false
        for (i in 0 until fm.backStackEntryCount) {
            val record = fm.getBackStackEntryAt(i)
            if (record.name == tagName) {
                fm.popBackStack(tagName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                innerAddToBackStack = true
                break
            }
        }
        fm.commit {
            if (animation is AnimationDefinition.Fade)
                setCustomAnimations(R.anim.navigator_fade_in, R.anim.navigator_fade_out, R.anim.navigator_fade_in, R.anim.navigator_fade_out)
            if (animation is AnimationDefinition.SlideInRight)
                setCustomAnimations(R.anim.navigator_slide_in, R.anim.navigator_fade_out, R.anim.navigator_fade_in, R.anim.navigator_slide_out)
            if (animation is AnimationDefinition.SlideInLeft)
                setCustomAnimations(R.anim.navigator_slide_out, R.anim.navigator_fade_out, R.anim.navigator_fade_in, R.anim.navigator_slide_in)
            if (animation is AnimationDefinition.Shared)
                prepareForSharedTransition(fm, this@options)

            val sameFragment = fm.findFragmentByTag(tagName)
            if (sameFragment != null && sameFragment::class != clazz) {
                // (maybe) should popUp it's childFragmentManager in this case.
                show(sameFragment)
            } else {
                newFragment.arguments = bundle
                when (type) {
                    TransactionType.REPLACE -> replace(containerView.id, newFragment, tagName)
                    TransactionType.ADD -> add(containerView.id, newFragment, tagName)
                }
            }
            // Cannot add to back stack when popUpTo is true
            if (!popUpToThis && (addToBackStack || innerAddToBackStack)) addToBackStack(tagName)
        }
    }

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
            if (fragment is ValueFragment && fragment.forceBackPress) {
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
                fm.fragments.any { it::class.simpleName == primaryFragClass?.simpleName }
        }
        if (!canGoBack() && clazz != null && !hasPrimaryFragment) {
            // Create primary fragment
            navigateTo(NavOptions(clazz, animation = AnimationDefinition.Fade()))
            return false
        }
        val currentFragment = getCurrentFragment()

        val shouldPopStack = if (currentFragment is ValueFragment) {
            !currentFragment.onBackPressed()
        } else {
            true
        }
        if (shouldPopStack) {
            fm.popBackStackImmediate()
        }
        return shouldPopStack
    }

    /**
     * Returns the current fragment class.
     */
    fun getCurrentFragmentClass(): FragClazz? =
        fm.findFragmentById(containerView.id)?.let { it::class }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getFragmentManager(): FragmentManager = fm

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getContainerView(): FrameLayout = containerView

    private fun getBackStackCount(): Int = fm.backStackEntryCount

    private fun getCurrentFragment() = fm.findFragmentById(containerView.id)

    private fun getFragmentTagName(clazz: FragClazz): String =
        clazz.java.simpleName + FRAGMENT_SUFFIX

    enum class TransactionType {
        REPLACE,
        ADD
    }

    abstract class BottomNavigation {
        abstract val bottomNavigationViewId: Int
        abstract val bottomNavigationFragments: Map<Int, KClass<out Fragment>>

        /**
         * Default selection will be the first Id of [bottomNavigationFragments].
         */
        open val selectedBottomNavigationId: Int = -1
        open fun onBottomNavigationSelectionChanged(@IdRes selectedId: Int) {}

        /**
         * Implement this interface on child fragments to get notified
         * about selection or re-selection.
         */
        interface Callbacks {
            fun onSelected() {}
            fun onReselected() {}
        }
    }

    companion object {
        private const val FRAGMENT_SUFFIX = "_navigator"
    }
}