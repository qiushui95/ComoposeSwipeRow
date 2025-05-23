package nbe.someone.code.swipe.row

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

@Composable
public fun rememberSwipeRowState(
    // 初始锚点位置
    initialValue: DragAnchors = DragAnchors.Center,
): SwipeRowState = remember(initialValue) {
    val draggableState = AnchoredDraggableState(initialValue = initialValue)

    SwipeRowState(draggableState)
}

private fun SubcomposeMeasureScope.measure(
    constraints: Constraints,
    slotName: String,
    content: (@Composable () -> Unit)?,
    widthState: MutableIntState,
): List<Placeable> {
    if (content == null) return emptyList()

    val placeableList = subcompose(slotName, content).map {
        it.measure(constraints)
    }

    widthState.intValue = placeableList.maxOfOrNull { it.width } ?: 0

    return placeableList
}

private fun getDraggableAnchors(
    state: SwipeRowState,
    startContent: (@Composable () -> Unit)?,
    endContent: (@Composable () -> Unit)?,
): DraggableAnchors<DragAnchors>? {
    return when {
        startContent != null && endContent != null -> DraggableAnchors {
            DragAnchors.Start at state.startWidthState.intValue * 1f
            DragAnchors.Center at 0f
            DragAnchors.End at state.endWidthState.intValue * -1f
        }

        startContent != null -> DraggableAnchors {
            DragAnchors.Start at state.startWidthState.intValue * 1f
            DragAnchors.Center at 0f
        }

        endContent != null -> DraggableAnchors {
            DragAnchors.Center at 0f
            DragAnchors.End at state.endWidthState.intValue * -1f
        }

        else -> null
    }
}

@Composable
public fun SwipeRow(
    modifier: Modifier = Modifier,
    state: SwipeRowState = rememberSwipeRowState(),
    startContent: (@Composable () -> Unit)? = null,
    endContent: (@Composable () -> Unit)? = null,
    centerContent: @Composable () -> Unit,
) {
    val draggableAnchors = getDraggableAnchors(state, startContent, endContent)

    if (draggableAnchors == null) {
        Box(modifier = modifier) { centerContent() }
        return
    }

    state.draggableState.updateAnchors(draggableAnchors)

    SubcomposeLayout(
        modifier = modifier
            .clipToBounds()
            .anchoredDraggable(state.draggableState, orientation = Orientation.Horizontal),
    ) { constraints ->

        val minConstraints = constraints.copy(minHeight = 0)

        val centerPlaceableList = measure(
            constraints = minConstraints,
            slotName = "center",
            content = centerContent,
            widthState = state.centerWidthState,
        )

        val centerHeight = centerPlaceableList.maxOfOrNull { it.height } ?: 0

        val hiddenConstraints = minConstraints.copy(minWidth = 0, maxHeight = centerHeight)

        val startPlaceableList = measure(
            constraints = hiddenConstraints,
            slotName = "start",
            content = startContent,
            widthState = state.startWidthState,
        )

        val endPlaceableList = measure(
            constraints = hiddenConstraints,
            slotName = "end",
            content = endContent,
            widthState = state.endWidthState,
        )

        layout(constraints.maxWidth, centerHeight) {
            val offsetX = state.swipeOffsetState.floatValue.roundToInt()

            if (startPlaceableList.isNotEmpty()) {
                val startX = state.startWidthState.intValue * -1 + offsetX

                for (placeable in startPlaceableList) {
                    placeable.place(startX, 0)
                }
            }

            if (endPlaceableList.isNotEmpty()) {
                val endX = state.centerWidthState.intValue + offsetX

                for (placeable in endPlaceableList) {
                    placeable.place(endX, 0)
                }
            }

            for (placeable in centerPlaceableList) {
                placeable.place(offsetX, 0)
            }
        }
    }
}
