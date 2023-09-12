package com.kpstv.navigation.compose.sample.ui.componenets

import android.view.MotionEvent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.kpstv.navigation.compose.ComposeNavigator
import com.kpstv.navigation.compose.DialogRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

class BottomSheetScope<T : DialogRoute> internal constructor(
    private val coroutineContext: CoroutineContext,
    private val dialogScope: ComposeNavigator.DialogScope<T>,
    private val sheetClose: suspend () -> Unit
) {
    fun dismiss() {
        CoroutineScope(coroutineContext).launch {
            sheetClose()
            dialogScope.dismiss()
        }
    }
}

/**
 * A bottom sheet component to display Modal Bottom Sheet.
 *
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class, InternalComposeApi::class)
@Composable
fun <T : DialogRoute> BottomSheet(
    dialogScope: ComposeNavigator.DialogScope<T>,
    content: @Composable BottomSheetScope<T>.() -> Unit,
) {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val coroutineContext = currentComposer.applyCoroutineContext

        val boxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

        val swipeableState = rememberSwipeableState(false)

        val bottomSheetScope = BottomSheetScope(
            coroutineContext = coroutineContext,
            dialogScope = dialogScope,
            sheetClose = { swipeableState.animateTo(false, tween()) }
        )

        val sheetSizePx = remember { mutableStateOf(0f) }
        val anchors = mapOf(
            boxHeightPx to false,
            (1f + boxHeightPx - sheetSizePx.value) to true // if 1f is removed it throws a runtime exception.
        )

        LaunchedEffect(Unit) effect@{
            swipeableState.animateTo(
                targetValue = true,
                anim = tween()
            )
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter pointer@{ event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    bottomSheetScope.dismiss()
                }
                return@pointer true
            })
        Layout(
            modifier = Modifier
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.5f) },
                    orientation = Orientation.Vertical
                ),
            content = {
                content(bottomSheetScope)
            }
        ) { measurables, constraints ->
            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }
            sheetSizePx.value = placeables.maxOf { it.height.toFloat() }
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEach { placeable ->
                    placeable.placeRelative(0, 0)
                }
            }
        }
    }
}