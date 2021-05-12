package com.kpstv.navigation

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.navigation.internals.HistoryImpl
import com.kpstv.navigation.internals.ViewStateFragment
import com.kpstv.navigation.internals.toIdentifier

/**
 * A base fragment to extend from in order to use [Navigator] effectively.
 * Child fragments of the host should extend from this class, this way
 * the fragment backStack can be effectively managed & going back is as
 * easy as calling [goBack].
 *
 * In order to pass arguments extend any class from [BaseArgs] & pass it as
 * parameter to [Navigator.show] call. Use [getKeyArgs] to retrieve
 * them.
 *
 * @see onBackPressed
 * @see BaseArgs
 */
open class ValueFragment(@LayoutRes id: Int) : ViewStateFragment(id) {
    constructor() : this(0)

    companion object {
        const val ARGUMENTS = "keyed_args"
    }

    /**
     * Tells [Navigator] to forcefully invoke [onBackPressed] on this fragment event though
     * [Navigator.canGoBack] returns true.
     *
     * If True then [onBackPressed] will be called regardless of any behavior. It is necessary
     * that you should return False (sometime in future) when your conditions are satisfied,
     * otherwise there will be unexpected side effects.
     *
     * This API is exposed for very edge case use only. It is not designed to always use
     * with [onBackPressed]. In extreme cases when [Navigator] fails
     * to manage back press behaviors this API should be used.
     */
    open val forceBackPress = false

    /**
     * Checks if the fragment has any arguments passed during [Navigator.show] call.
     */
    fun hasKeyArgs(): Boolean {
        return arguments?.containsKey(ARGUMENTS) ?: false
    }

    /**
     * Parse the typed arguments from the bundle. It is best practice to check [hasKeyArgs]
     * & then proceed with this call.
     *
     * @throws NullPointerException When it does not exist.
     */
    fun <T : BaseArgs> getKeyArgs(): T {
        return arguments?.getParcelable<T>(ARGUMENTS) as T
    }

    /**
     * @see Navigator.goBack
     */
    fun goBack() {
        getParentNavigator().goBack()
    }

    /**
     * Get Navigator associated with the context of the container view in which this fragment
     * is inflated.
     */
    fun getParentNavigator(): Navigator {
        try {
            return if (parentFragment != null) {
                (requireParentFragment() as NavigatorTransmitter).getNavigator()
            } else {
                (requireContext() as NavigatorTransmitter).getNavigator()
            }
        } catch (e: Exception) {
            throw NotImplementedError("Parent does not implement NavigatorTransmitter.")
        }
    }

    /**
     * A simplified version of Navigator that can be used to show [DialogFragment] or [BottomSheetDialogFragment].
     * The instance is available after [onViewCreated].
     */
    fun getSimpleNavigator(): SimpleNavigator {
        if (!isSimpleNavigatorInitialized()) throw IllegalAccessException("You must call it between onViewCreated() & onDestroy()")
        return simpleNavigator
    }

    /**
     * Override this to receive back press.
     *
     * The back press is propagated from the host to all of the child fragments. During back
     * press if [Navigator] decides to remove this fragment from the stack it will first call
     * this method to know the result. Upon True, the event has been consumed by the
     * [ValueFragment] & [Navigator] will not pop this fragment.
     *
     * It also handles the backpress of the child fragments as well. So it is necessary that
     * once all your conditions are satisfied you should call the super method.
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        simpleNavigator = SimpleNavigator(requireContext(), childFragmentManager)
        simpleNavigator.restoreState(this.toIdentifier(), savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (isSimpleNavigatorInitialized()) {
            simpleNavigator.saveState(this.toIdentifier(), outState)
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * Will be resolved through reflection at runtime.
     *
     * @hide
     */
    private var bottomNavigationState: Bundle? = null
    private var tabNavigationState: Bundle? = null
    private lateinit var simpleNavigator: SimpleNavigator

    private fun isSimpleNavigatorInitialized(): Boolean = ::simpleNavigator.isInitialized

    private fun clearArgs() {
        arguments?.remove(ARGUMENTS)
    }
}