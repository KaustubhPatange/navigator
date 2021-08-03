@file:OptIn(ExperimentalAnimationApi::class)

package com.kpstv.navigation.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

//TODO: Fix transitions & change comments, Each transition is with respect to one composable

public val None: TransitionKey get() = NoneTransition.key

internal val NoneTransition: NavigatorTransition = object : NavigatorTransition() {
    override val forwardTransition: EnterTransition = EnterTransition.None
    override val backwardTransition: ExitTransition = ExitTransition.None
}

/**
 * A Fade transition
 */
public val Fade: TransitionKey get() = FadeTransition.key

internal val FadeTransition: NavigatorTransition = object : NavigatorTransition() {
    override val forwardTransition: EnterTransition = fadeIn(animationSpec = tween(300))
    override val backwardTransition: ExitTransition = fadeOut(animationSpec = tween(300))
}

/**
 * Slide right transition.
 *
 * Slide in right transition will be applied to the "target" composable where as Slide out right
 * transition will be applied to the "current" composable.
 */
public val SlideRight: TransitionKey get() = SlideRightTransition.key

internal val SlideRightTransition: NavigatorTransition = object : NavigatorTransition() {
    override val forwardTransition: EnterTransition =
        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
    override val backwardTransition: ExitTransition =
        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
}

/**
 * Slide left transition.
 *
 * Slide in left transition will be applied to the "target" composable where as Slide out left
 * transition will be applied to the "current" composable.
 */
public val SlideLeft: TransitionKey get() = SlideLeftTransition.key

internal val SlideLeftTransition: NavigatorTransition = object : NavigatorTransition() {
    override val forwardTransition: EnterTransition =
        slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300))
    override val backwardTransition: ExitTransition =
        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
}