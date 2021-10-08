package com.kpstv.navigation.compose.sample

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.kpstv.navigation.compose.ComposeTransition
import com.kpstv.navigation.compose.NavigatorTransition
import com.kpstv.navigation.compose.TransitionKey

val SlideWithFadeRight get() = SlideWithFadeRightTransition.key
val SlideWithFadeLeft get() = SlideWithFadeLeftTransition.key

internal val SlideWithFadeRightTransition = object : NavigatorTransition() {
    override val key: TransitionKey = TransitionKey("com.kpstv.navigation.compose.sample:SlideWithFadeRightTransition")
    override val forwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer {
            alpha = progress
            scaleX = progress
            scaleY = progress
            translationX = width * (1 - progress)
        })
    }
    override val backwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer {
            alpha = 1 - progress
            scaleX = 1 - progress
            scaleY = 1 - progress
            translationX = width * progress
        })
    }
}

internal val SlideWithFadeLeftTransition = object : NavigatorTransition() {
    override val key: TransitionKey = TransitionKey("com.kpstv.navigation.compose.sample:SlideWithFadeLeftTransition")
    override val forwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer {
            alpha = progress
            scaleX = progress
            scaleY = progress
            translationX = width * (1 - progress) * (-1)
        })
    }
    override val backwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer {
            alpha = 1 - progress
            scaleX = 1 - progress
            scaleY = 1 - progress
            translationX =  (-1) * width * (progress)
        })
    }
}