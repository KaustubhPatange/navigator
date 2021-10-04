package com.kpstv.navigation.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

public val None: TransitionKey get() = NoneTransition.key

internal val NoneTransition: NavigatorTransition = object : NavigatorTransition() {
    override val key: TransitionKey = TransitionKey("com.kpstv.navigation.compose:NoneTransition")
    override val forwardTransition: ComposeTransition = ComposeTransition { modifier, _, _, _ -> modifier }
    override val backwardTransition: ComposeTransition = ComposeTransition { modifier, _, _, _ -> modifier }
}

/**
 * A Fade transition
 */
public val Fade: TransitionKey get() = FadeTransition.key

internal val FadeTransition: NavigatorTransition = object : NavigatorTransition() {
    override val key: TransitionKey = TransitionKey("com.kpstv.navigation.compose:FadeTransition")
    override val forwardTransition: ComposeTransition = ComposeTransition { modifier, _, _, progress ->
        modifier.then(Modifier.graphicsLayer { alpha = progress }) // fade-in
    }
    override val backwardTransition: ComposeTransition = ComposeTransition { modifier, _, _, progress ->
        modifier.then(Modifier.graphicsLayer { alpha = 1 - progress }) // fade-out
    }
}

/**
 * Slide right transition.
 *
 * Slide in right transition will run when composable is the "target" destination (forwardTransition).
 * Slide out right will run when composable is the "current" destination (backwardTransition).
 */
public val SlideRight: TransitionKey get() = SlideRightTransition.key

internal val SlideRightTransition: NavigatorTransition = object : NavigatorTransition() {
    override val key: TransitionKey = TransitionKey("com.kpstv.navigation.compose:SlideRightTransition")
    override val forwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer { this.translationX = width + (-1) * width * progress }) // slide-in-right
    }
    override val backwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer { this.translationX = width * (progress) }) // slide-out-right
    }
}

/**
 * Slide left transition.
 *
 * Slide in left transition will run when composable is the "target" destination (forwardTransition).
 * Slide out left will run when composable is the "current" destination (backwardTransition).
 */
public val SlideLeft: TransitionKey get() = SlideLeftTransition.key

internal val SlideLeftTransition: NavigatorTransition = object : NavigatorTransition() {
    override val key: TransitionKey = TransitionKey("com.kpstv.navigation.compose:SlideLeftTransition")
    override val forwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer { this.translationX = width * (1 - progress) * (-1) }) // slide-in-left
    }
    override val backwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer { this.translationX = (-1) * width * (progress) }) // slide-out-left
    }
}