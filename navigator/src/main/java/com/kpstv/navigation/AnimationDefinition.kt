package com.kpstv.navigation

import android.graphics.Rect
import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
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
     * @param enter A transition to be applied on Fragment B. Set -1 to omit.
     * @param exit A transition to be applied on Fragment A. Set -1 to omit.
     */
    data class Shared(
        val elements: Map<View, String>,
        @TransitionRes val enter: Int = R.transition.navigator_transition_fade,
        @TransitionRes val exit: Int = R.transition.navigator_transition_fade
    ) : NavAnimation()

    /** TODO: Find a better name for parameters.
     * Custom set of animations to play. Supported resource types are "anim", "animator" & "transition".
     *
     * Consider a scenario of "Fragment A" navigating to "Fragment B".
     *
     * @param enter Animation resource to be played on Fragment B.
     * @param exit Animation resource to be played on Fragment A.
     * @param popEnter Animation resource to be played on Fragment A when returning from Fragment B.
     * @param popExit Animation resource to be played on Fragment B when returning to Fragment A.
     */
    open class Custom(
        val enter: Int = 0,
        val exit: Int = 0,
        val popEnter: Int = 0,
        val popExit: Int = 0,
    ) : NavAnimation()
}