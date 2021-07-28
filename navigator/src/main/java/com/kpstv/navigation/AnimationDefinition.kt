@file:Suppress("unused")

package com.kpstv.navigation

import android.graphics.Rect
import android.os.Parcelable
import android.view.View
import androidx.annotation.TransitionRes
import kotlinx.android.parcel.Parcelize

/**
 * A generic base animation class which will be used for animating fragment transaction.
 * Use [AnimationDefinition] to specify a animation.
 */
open class NavAnimation internal constructor()

/**
 * Some default sets of animations Navigator provides out of the box.
 *
 * @see <a href="https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials#navigation-with-animation">Navigation with animation</a>
 */
class AnimationDefinition private constructor() {
    object None : NavAnimation()
    object Fade : Custom(R.anim.navigator_fade_in, R.anim.navigator_fade_out, R.anim.navigator_fade_in, R.anim.navigator_fade_out)
    object Zoom : Custom(R.animator.navigator_scale_in_visible, R.animator.navigator_scale_out_gone, R.animator.navigator_scale_out_visible, R.animator.navigator_scale_in_gone)
    object SlideInRight : Custom(R.anim.navigator_slide_in_right, R.anim.navigator_fade_out, R.anim.navigator_fade_in, R.anim.navigator_slide_out_right)
    object SlideInLeft : Custom(R.anim.navigator_slide_in_left, R.anim.navigator_fade_out, R.anim.navigator_fade_in, R.anim.navigator_slide_out_left)
    object SlideInBottom : Custom(R.anim.navigator_slide_in_bottom, R.anim.navigator_fade_out, R.anim.navigator_fade_in, R.anim.navigator_slide_out_bottom)
    object SlideInTop : Custom(R.anim.navigator_slide_in_top, R.anim.navigator_fade_out, R.anim.navigator_fade_in, R.anim.navigator_slide_out_top)

    /**
     * Runs a custom circular reveal animation.
     *
     * @param fromTarget Start from the coordinates specified by the View or manually by [Rect]. Default is from the Center.
     *
     * @see [View.getLocalVisibleRect]
     * @see [View.getGlobalVisibleRect]
     */
    @Parcelize
    data class CircularReveal(
        val fromTarget: Rect? = null,
    ) : NavAnimation(), Parcelable

    /**
     * Equivalent to shared animations.
     *
     * You must set a unique `android:transitionName` on the [View] of Fragment A to make sure
     * exit shared transition works perfectly.
     *
     * @param elements A map of "from [View]" of Fragment A to the "Transition name of [View]" of Fragment B.
     * @param sharedElementEntering Set the enter transition for the shared element(s).
     * @param sharedElementExiting Set the exit transition for the shared element(s).
     * @param destinationEntering A transition to be applied on Fragment B. Set -1 to omit.
     * @param currentExiting A transition to be applied on Fragment A. Set -1 to omit.
     */
    data class Shared(
        val elements: Map<View, String>,
        @TransitionRes val sharedElementEntering: Int = R.transition.navigator_change_transform,
        @TransitionRes val sharedElementExiting: Int = R.transition.navigator_change_transform,
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
