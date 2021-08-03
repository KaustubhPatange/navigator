@file:OptIn(ExperimentalAnimationApi::class)

package com.kpstv.navigation.compose.sample

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.kpstv.navigation.compose.ComposeTransition
import com.kpstv.navigation.compose.NavigatorTransition

val SlideWithFadeRight get() = SlideWithFadeRightTransition.key
val SlideWithFadeLeft get() = SlideWithFadeLeftTransition.key

internal val SlideWithFadeRightTransition = object : NavigatorTransition() {
    override val forwardTransition: EnterTransition = fadeIn(animationSpec = tween(300)) +
            expandVertically(expandFrom = Alignment.CenterVertically, animationSpec = tween(300)) +
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))

    override val backwardTransition: ExitTransition = fadeOut(animationSpec = tween(300)) +
            shrinkVertically(shrinkTowards = Alignment.CenterVertically, animationSpec = tween(300)) +
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
}

internal val SlideWithFadeLeftTransition = object : NavigatorTransition() {
    override val forwardTransition: EnterTransition = fadeIn(animationSpec = tween(300)) +
            expandVertically(expandFrom = Alignment.CenterVertically, animationSpec = tween(300)) +
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300))

    override val backwardTransition: ExitTransition = fadeOut(animationSpec = tween(300)) +
            shrinkVertically(shrinkTowards = Alignment.CenterVertically, animationSpec = tween(300)) +
            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
}