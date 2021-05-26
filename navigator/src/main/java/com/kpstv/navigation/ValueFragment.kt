package com.kpstv.navigation

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.navigation.internals.ViewStateFragment
import com.kpstv.navigation.internals.toIdentifier

/**
 * A base fragment to extend from in order to use [FragmentNavigator] effectively.
 * Child fragments of the host should extend from this class, this way
 * the fragment backStack can be effectively managed & going back is as
 * easy as calling [goBack].
 *
 * In order to pass arguments extend any class from [BaseArgs] & pass it as
 * parameter to [FragmentNavigator.show] call. Use [getKeyArgs] to retrieve
 * them.
 *
 * @see onBackPressed
 * @see BaseArgs
 */
open class ValueFragment(@LayoutRes id: Int) : ViewStateFragment(id) {
    constructor() : this(0)

    companion object {
        fun createArgKey(args: BaseArgs): String = createArgKey(args::class.qualifiedName!!)
        fun createArgKey(identifier: String): String = "$VALUE_ARGUMENT:$identifier"

        private const val VALUE_ARGUMENT = "com.kpstv.navigator:keyed_args"
    }

    /**
     * Tells [FragmentNavigator] to forcefully invoke [onBackPressed] on this fragment event though
     * [FragmentNavigator.canGoBack] returns true.
     *
     * If True then [onBackPressed] will be called regardless of any behavior. It is necessary
     * that you should return False (sometime in future) when your conditions are satisfied,
     * otherwise there will be unexpected side effects.
     *
     * This API is exposed for very edge case use only. It is not designed to always use
     * with [onBackPressed]. In extreme cases when [FragmentNavigator] fails
     * to manage back press behaviors this API should be used.
     */
    open val forceBackPress = false

    /**
     * Checks if the fragment has any arguments passed during [FragmentNavigator.show] call.
     */
    inline fun<reified T : BaseArgs> hasKeyArgs(): Boolean {
        return arguments?.containsKey(createArgKey(T::class.qualifiedName!!)) ?: false
    }

    /**
     * Parse the typed arguments from the bundle. It is best practice to check [hasKeyArgs]
     * & then proceed with this call.
     *
     * @throws NullPointerException When it does not exist.
     */
    inline fun <reified T : BaseArgs> getKeyArgs(): T {
        return arguments?.getParcelable<T>(createArgKey(T::class.qualifiedName!!)) as T
    }

    /**
     * Remove the typed arguments from the bundle.
     */
    inline fun <reified T: BaseArgs> clearArgs() {
        arguments?.remove(createArgKey(T::class.qualifiedName!!))
    }

    /**
     * @see FragmentNavigator.goBack
     */
    fun goBack() {
        getParentNavigator().goBack()
    }

    /**
     * Get Navigator associated with the context of the container view in which this fragment
     * is inflated.
     */
    fun getParentNavigator(): FragmentNavigator {
        return if (parentFragment != null) {
            (requireParentFragment() as FragmentNavigator.Transmitter).getNavigator()
        } else {
            requireContext().findFragmentTransmitter().getNavigator()
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
     * press if [FragmentNavigator] decides to remove this fragment from the stack it will first call
     * this method to know the result. Upon True, the event has been consumed by the
     * [ValueFragment] & [FragmentNavigator] will not pop this fragment.
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
        if (savedInstanceState != null) {
            simpleNavigator.restoreState(this.toIdentifier(), savedInstanceState)
        } else {
            simpleNavigator.restoreState(this.toIdentifier(), simpleNavigateState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (isSimpleNavigatorInitialized()) {
            simpleNavigator.saveState(this.toIdentifier(), outState)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        simpleNavigator.restoreState(this.toIdentifier(), simpleNavigateState)
        super.onStop()
    }

    // Will be resolved by friend path mechanism by individual navigation modules
    internal var bottomNavigationState: Bundle? = null
    internal var tabNavigationState: Bundle? = null

    private var simpleNavigateState = Bundle()
    private lateinit var simpleNavigator: SimpleNavigator

    private fun isSimpleNavigatorInitialized(): Boolean = ::simpleNavigator.isInitialized

    private fun Context.findFragmentTransmitter(): FragmentNavigator.Transmitter {
        if (this is FragmentActivity && this is FragmentNavigator.Transmitter) return this
        if (this is ContextWrapper) {
            val baseContext = this.baseContext
            if (baseContext is FragmentNavigator.Transmitter) return baseContext
            baseContext.findFragmentTransmitter()
        }
        throw NotImplementedError("Parent has not implemented \"FragmentNavigator.Transmitter\" interface.")
    }
}