package com.kpstv.navigation

import android.graphics.Rect
import android.view.View
import androidx.annotation.TransitionRes

/**
 * A generic base animation class which will be used for animating fragment transaction.
 * Use [AnimationDefinition] to specify a animation.
 */
open class NavAnimation

class AnimationDefinition {
    object None : NavAnimation()
    object Fade : Custom(R.anim.navigator_fade_in, R.anim.navigator_fade_out, R.anim.navigator_fade_in, R.anim.navigator_fade_out)
    object SlideInRight : Custom(R.anim.navigator_slide_in_right, R.anim.navigator_fade_out, R.anim.navigator_fade_in, R.anim.navigator_slide_out_right)
    object SlideInLeft : Custom(R.anim.navigator_slide_in_left, R.anim.navigator_fade_out, R.anim.navigator_fade_in, R.anim.navigator_slide_out_left)

    /**
     * Runs a custom circular reveal animation.
     *
     * @param forFragment A fragment to wait for until it's view is drawn. Default is the [Navigator.NavOptions.clazz].
     * @param fromTarget Start from the coordinates specified by the View or manually by [Rect]. Default is from the Center.
     *
     * @see [View.getLocalVisibleRect]
     * @see [View.getGlobalVisibleRect]
     */
    data class CircularReveal(
        val forFragment: FragClazz? = null,
        val fromTarget: Rect? = null
    ) : NavAnimation()

    /**
     * Equivalent to shared animations.
     *
     * You must set a unique `android:transitionName` on the [View] of Fragment A to make sure
     * return shared transition works perfectly.
     *
     * @param elements A map of "from [View]" of Fragment A to the "Transition name of [View]" of Fragment B.
     * @param destinationEntering A transition to be applied on Fragment B. Set -1 to omit.
     * @param currentExiting A transition to be applied on Fragment A. Set -1 to omit.
     */
    data class Shared(
        val elements: Map<View, String>,
        @TransitionRes val destinationEntering: Int = R.transition.navigator_transition_fade,
        @TransitionRes val currentExiting: Int = R.transition.navigator_transition_fade
    ) : NavAnimation()

    /**
     * Custom set of animations to play. Supported resource types are "anim", "animator" & "transition".
     *
     * @param destinationEntering Animation that'll be played on the destination fragment when navigating from the current fragment to it.
     * @param currentExiting Animation that'll be played on the current fragment when exiting so as to navigate to the destination fragment.
     * @param currentReturning Animation that'll be played on the current fragment when returning from the destination fragment.
     *                         Default will be the reverse of [currentExiting].
     * @param destinationExiting Animation that'll be played on the destination fragment when returning to the previous (current) fragment.
     *                           Default will be the reverse of [destinationEntering].
     */
    open class Custom(
        val destinationEntering: Int, /* enter */
        val currentExiting: Int, /* exit */
        val currentReturning: Int = 0, /* popEnter */
        val destinationExiting: Int = 0, /* popExit */
    ) : NavAnimation()
}
