package com.kpstv.navigation

import android.graphics.Rect
import android.view.View

/**
 * A generic base animation class which will be used for animating fragment transaction.
 * Use [AnimationDefinition] to specify a animation.
 */
open class NavAnimation

class AnimationDefinition {
    object None : NavAnimation()
    class Fade : NavAnimation()
    class SlideInLeft : NavAnimation()
    class SlideInRight : NavAnimation()
    data class CircularReveal(
        /**
         * This will wait for the fragment to wait for until its view is drawn.
         *
         * Default will wait for the next navigation fragment in the [Navigator.navigateTo].
         */
        val forFragment: FragClazz? = null,
        /**
         * Start from the coordinates specified by the View. Default is from Center.
         *
         * See: [View.getLocalVisibleRect] or [View.getGlobalVisibleRect]
         */
        val fromTarget: Rect? = null
    ) : NavAnimation()
    data class Shared(
        /**
         * A map of "from [View]" of Fragment A to the "Transition name of [View]" of Fragment B.
         *
         * You must set a unique `android:transitionName` on the [View] of Fragment A to make sure
         * return shared transition works perfectly.
         */
        val elements: Map<View, String>
    ) : NavAnimation()
}