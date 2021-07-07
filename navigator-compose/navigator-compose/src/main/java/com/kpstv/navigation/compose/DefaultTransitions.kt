package com.kpstv.navigation.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

internal val NoneTransition: NavigatorTransition = object : NavigatorTransition() {
    override val forwardTransition: ComposeTransition = ComposeTransition { modifier, _, _, _ -> modifier }
    override val backwardTransition: ComposeTransition = ComposeTransition { modifier, _, _, _ -> modifier }
}

internal val FadeTransition: NavigatorTransition = object : NavigatorTransition() {
    override val forwardTransition: ComposeTransition = ComposeTransition { modifier, _, _, progress ->
        modifier.then(Modifier.graphicsLayer { alpha = progress }) // fade-in
    }
    override val backwardTransition: ComposeTransition = ComposeTransition { modifier, _, _, progress ->
        modifier.then(Modifier.graphicsLayer { alpha = 1 - progress }) // fade-out
    }
}

internal val SlideRightTransition: NavigatorTransition = object : NavigatorTransition() {
    override val forwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer { this.translationX = width + (-1) * width * progress }) // slide-in-right
    }
    override val backwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer { this.translationX = width * (progress) }) // slide-out-right
    }
}

internal val SlideLeftTransition: NavigatorTransition = object : NavigatorTransition() {
    override val forwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer { this.translationX = width * (1 - progress) * (-1) }) // slide-in-left
    }
    override val backwardTransition: ComposeTransition = ComposeTransition { modifier, width, _, progress ->
        modifier.then(Modifier.graphicsLayer { this.translationX = (-1) * width * (progress) }) // slide-out-left
    }
}