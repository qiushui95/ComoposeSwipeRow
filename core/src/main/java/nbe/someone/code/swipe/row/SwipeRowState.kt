package nbe.someone.code.swipe.row

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
public class SwipeRowState(internal val draggableState: AnchoredDraggableState<DragAnchors>) {
    public val dragAnchorsState: State<DragAnchors> by lazy {
        derivedStateOf { draggableState.currentValue }
    }

    internal val startWidthState: MutableIntState by lazy {
        mutableIntStateOf(0)
    }

    internal val centerWidthState: MutableIntState by lazy {
        mutableIntStateOf(0)
    }

    internal val endWidthState: MutableIntState by lazy {
        mutableIntStateOf(0)
    }

    public val swipeOffsetState: FloatState by lazy {
        derivedStateOf { checkOffset(draggableState.offset) }.asFloatState()
    }

    public val offsetInfoState: State<SwipeOffsetInfo> by lazy {
        derivedStateOf {
            val anchor = dragAnchorsState.value

            val offset = swipeOffsetState.floatValue

            val total = when (anchor) {
                DragAnchors.Start -> startWidthState
                DragAnchors.Center -> centerWidthState
                DragAnchors.End -> endWidthState
            }.intValue

            SwipeOffsetInfo(anchor, offset.toInt(), total)
        }
    }

    public constructor(initialValue: DragAnchors) : this(
        AnchoredDraggableState(initialValue = initialValue),
    )

    private fun checkOffset(offset: Float): Float {
        if (offset.isNaN()) return 0f

        return offset.coerceIn(-endWidthState.intValue * 1f, startWidthState.intValue * 1f)
    }

    public fun snapTo(scope: CoroutineScope, anchor: DragAnchors) {
        scope.launch { snapTo(anchor) }
    }

    public suspend fun snapTo(anchor: DragAnchors) {
        draggableState.snapTo(anchor)
    }

    public fun animateTo(
        scope: CoroutineScope,
        anchor: DragAnchors,
        onEnd: suspend () -> Unit = { },
    ) {
        scope.launch {
            launch { animateTo(anchor) }.join()
            onEnd()
        }
    }

    public suspend fun animateTo(anchor: DragAnchors) {
        draggableState.animateTo(anchor)
    }
}
