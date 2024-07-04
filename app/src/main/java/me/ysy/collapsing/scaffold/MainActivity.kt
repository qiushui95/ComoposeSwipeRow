@file:OptIn(ExperimentalFoundationApi::class)

package me.ysy.collapsing.scaffold

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import nbe.someone.code.swipe.row.DragAnchors
import nbe.someone.code.swipe.row.SwipeRow
import nbe.someone.code.swipe.row.SwipeRowState
import nbe.someone.code.swipe.row.rememberSwipeRowState
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CPMainPage()
        }
    }
}

private sealed class MainEvent(val dragAnchors: DragAnchors) {
    data object CloseAll : MainEvent(DragAnchors.Center)

    data object OpenStart : MainEvent(DragAnchors.Start)

    data object OpenEnd : MainEvent(DragAnchors.End)
}

private val LocalMainEventFlow = staticCompositionLocalOf {
    MutableSharedFlow<MainEvent>()
}

private val colorList = listOf(
    Color(0xFF007BFF), // 蓝色
    Color(0xFF673AB7), // 深紫色
    Color(0xFF3F51B5), // 靛蓝色
    Color(0xFF009688), // 蓝绿色
    Color(0xFF4CAF50), // 绿色
    Color(0xFF8BC34A), // 浅绿色
    Color(0xFFCDDC39), // 青柠色
    Color(0xFFFFC107), // 琥珀色
    Color(0xFFFF9800), // 橙色
    Color(0xFF795548), // 棕色
)

@Composable
private fun rememberRandomColor(key: String): Color = remember(key) {
    colorList[Random.nextInt(0, 10)]
}

@Composable
private fun CPSwipeRow(tag: Any) {
    val centerBgColor = rememberRandomColor("${tag}centerBgColor")
    val startBgColor1 = rememberRandomColor("${tag}startBgColor1")
    val endBgColor1 = rememberRandomColor("${tag}endBgColor1")
    val endBgColor2 = rememberRandomColor("${tag}endBgColor2")
    key(tag) {
        val state = rememberSwipeRowState()

        val eventFlow = LocalMainEventFlow.current

        LaunchedEffect(Unit) {
            eventFlow
                .onEach { state.animateTo(it.dragAnchors) }
                .launchIn(this)
        }

        SwipeRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = state,
            startContent = { CPStartContent(state, startBgColor1) },
            centerContent = { CPCenterContent(state, centerBgColor) },
            endContent = { CPEndContent(state, endBgColor1, endBgColor2) },
        )
    }
}

@Composable
private fun CPStartContent(state: SwipeRowState, bgColor: Color) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .clickable { state.animateTo(scope, DragAnchors.End) }
            .background(bgColor)
            .fillMaxHeight()
            .width(45.dp),
        contentAlignment = Alignment.Center,
    ) {
        CPText("Open\nEnd")
    }
}

@Composable
private fun CPCenterContent(state: SwipeRowState, bgColor: Color) {
    val anchorState = remember(state.offsetInfoState) {
        derivedStateOf { "anchor:${state.offsetInfoState.value.anchor.javaClass.simpleName}" }
    }

    val offsetState = remember(state.offsetInfoState) {
        derivedStateOf { "offset:${state.offsetInfoState.value.offset}" }
    }

    val totalState = remember(state.offsetInfoState) {
        derivedStateOf { "total:${state.offsetInfoState.value.total}" }
    }

    Column(
        modifier = Modifier
            .background(bgColor)
            .fillMaxWidth()
            .height(80.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CPCenterText(anchorState)
        CPCenterText(offsetState)
        CPCenterText(totalState)
    }
}

@Composable
private fun CPCenterText(state: State<String>) {
    CPText(text = state.value)
}

@Composable
private fun CPText(text: String) {
    Text(text = text, color = Color.White)
}

@Composable
private fun CPEndContent(state: SwipeRowState, bgColor1: Color, bgColor2: Color) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Row(modifier = Modifier.fillMaxHeight()) {
        Box(
            modifier = Modifier
                .clickable {
                    state.animateTo(scope, DragAnchors.Start) {
                        Toast
                            .makeText(context, "11", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .background(bgColor1)
                .fillMaxHeight()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            CPText("Open\nStart")
        }
        Box(
            modifier = Modifier
                .clickable { state.animateTo(scope, DragAnchors.Center) }
                .background(bgColor2)
                .fillMaxHeight()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            CPText("Close\nEnd")
        }
    }
}

@Composable
private fun CPMainPage() {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(6) {
            CPSwipeRow(it)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            CPButton("Open Start", MainEvent.OpenStart)
            CPButton("Close All", MainEvent.CloseAll)
            CPButton("Open End", MainEvent.OpenEnd)
        }
    }
}

@Composable
private fun CPButton(text: String, event: MainEvent) {
    val eventFlow = LocalMainEventFlow.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .clickable { scope.launch { eventFlow.emit(event) } }
            .background(Color.Gray)
            .padding(8.dp),
    ) {
        CPText(text)
    }
}
