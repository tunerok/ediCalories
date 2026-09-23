package com.example.edicalories.ui.today

import android.view.ViewConfiguration
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.edicalories.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max

private val SWIPE_THRESHOLD = 72.dp

@Composable
fun DaySwipeContainer(
    enabled: Boolean,
    selectedEpochDay: Long,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    modifier: Modifier = Modifier,
    previous: @Composable () -> Unit,
    current: @Composable () -> Unit,
    next: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val minDistanceThreshold = remember(density) { with(density) { SWIPE_THRESHOLD.toPx() } }
    val minFlingVelocity = remember(context) {
        ViewConfiguration.get(context).scaledMinimumFlingVelocity.toFloat()
    }
    val onPreviousDayState = rememberUpdatedState(onPreviousDay)
    val onNextDayState = rememberUpdatedState(onNextDay)
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var appliedEpochDay by remember { mutableLongStateOf(selectedEpochDay) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    val previousDayLabel = stringResource(R.string.previous_day)
    val nextDayLabel = stringResource(R.string.next_day)

    if (selectedEpochDay != appliedEpochDay) {
        dragOffset = 0f
        appliedEpochDay = selectedEpochDay
    }

    LaunchedEffect(selectedEpochDay) {
        settleJob?.cancel()
        settleJob = null
    }

    LaunchedEffect(enabled) {
        if (!enabled) {
            settleJob?.cancel()
            settleJob = null
            dragOffset = 0f
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(previousDayLabel) {
                        onPreviousDayState.value()
                        true
                    },
                    CustomAccessibilityAction(nextDayLabel) {
                        onNextDayState.value()
                        true
                    },
                )
            },
    ) {
        val pageWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val thresholdPx = max(minDistanceThreshold, pageWidthPx * 0.25f)
        val draggableState = rememberDraggableState { delta ->
            dragOffset = (dragOffset + delta).coerceIn(-pageWidthPx, pageWidthPx)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    enabled = enabled,
                    startDragImmediately = false,
                    onDragStarted = {
                        settleJob?.cancel()
                    },
                    onDragStopped = { velocity ->
                        val offset = dragOffset
                        val goNext = shouldGoNextDay(
                            offset,
                            velocity,
                            thresholdPx,
                            minFlingVelocity,
                        )
                        val goPrevious = shouldGoPreviousDay(
                            offset,
                            velocity,
                            thresholdPx,
                            minFlingVelocity,
                        )
                        val target = when {
                            goNext -> -pageWidthPx
                            goPrevious -> pageWidthPx
                            else -> 0f
                        }
                        settleJob = scope.launch {
                            animate(
                                initialValue = dragOffset,
                                targetValue = target,
                                initialVelocity = velocity,
                                animationSpec = spring(),
                            ) { value, _ ->
                                dragOffset = value
                            }
                            if (target < 0f) {
                                onNextDayState.value()
                            } else if (target > 0f) {
                                onPreviousDayState.value()
                            }
                        }
                    },
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationX = -pageWidthPx + dragOffset },
            ) {
                previous()
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationX = dragOffset },
            ) {
                current()
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationX = pageWidthPx + dragOffset },
            ) {
                next()
            }
        }
    }
}

private fun shouldGoNextDay(
    offset: Float,
    velocity: Float,
    thresholdPx: Float,
    minFlingVelocity: Float,
): Boolean {
    val byDistance = offset <= -thresholdPx
    val byFling = velocity <= -minFlingVelocity && offset <= 0f
    return byDistance || byFling
}

private fun shouldGoPreviousDay(
    offset: Float,
    velocity: Float,
    thresholdPx: Float,
    minFlingVelocity: Float,
): Boolean {
    val byDistance = offset >= thresholdPx
    val byFling = velocity >= minFlingVelocity && offset >= 0f
    return byDistance || byFling
}
