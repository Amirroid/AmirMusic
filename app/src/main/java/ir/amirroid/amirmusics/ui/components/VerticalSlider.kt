package ir.amirroid.amirmusics.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp


@Composable
fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    context: Context
) {
    val color = MaterialTheme.colorScheme.surfaceVariant.copy(0.7f)
    val activeColor = MaterialTheme.colorScheme.primary
    var height: Float = 0f
    val handleCenter = remember {
        mutableStateOf(0f)
    }
    var maxHeight = 0f
    var minHeight = 0f
    Canvas(
        modifier = Modifier
            .height(200.dp)
            .width(32.dp)
            .onGloballyPositioned {
                handleCenter.value = it.positionInWindow().y
                minHeight = it.positionInWindow().y
                maxHeight = minHeight + it.size.height
            }
            .then(modifier)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    handleCenter.value =
                        ((change.position.y) + handleCenter.value).coerceIn(
                            minHeight,
                            maxHeight
                        )
                    Log.d(
                        "TAGVARTICAL",
                        "VerticalSlider: ${handleCenter.value} __ ${change.position.y} __ $maxHeight __ $height"
                    )
                    val newProgress =
                        ((handleCenter.value - minHeight) / height)
                            .coerceIn(0f, 1f)
                    Log.d("TAGVARTICAL", "VerticalSlider: ${1 - newProgress}")
                    onValueChange
                        .invoke(
                            1 - newProgress
                        )
                    change.consume()
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    handleCenter.value =
                        (it.y + handleCenter.value).coerceIn(minHeight, maxHeight)
                    Log.d(
                        "TAGVARTICAL",
                        "VerticalSlider: ${handleCenter.value} __ ${it.y} __ $maxHeight __ ${height}"
                    )
                    val newProgress =
                        ((handleCenter.value - minHeight) / height)
                            .coerceIn(0f, 1f)
                    Log.d("TAGVARTICAL", "VerticalSlider: ${1 - newProgress}")
                    onValueChange
                        .invoke(
                            1 - newProgress
                        )
                }
            }
    ) {
        height = size.height
        val width = size.width
        val radius = width / 2
        drawLine(
            color,
            start = Offset(radius - 2.dp(context), 0f),
            end = Offset(radius - 2.dp(context), height),
            strokeWidth = 4.dp(context),
            cap = StrokeCap.Round
        )
        drawLine(
            activeColor,
            start = Offset(radius - 2.dp(context), height),
            end = Offset(
                radius - 2.dp(context),
                if (value == 0f) height else height - (height * (value))
            ),
            strokeWidth = 4.dp(context),
            cap = StrokeCap.Round
        )
        drawPoints(
            listOf(
                Offset(
                    radius - 2.dp(context),
                    if (value == 0f) height else height - (height * (value))
                )
            ),
            PointMode.Points,
            color = activeColor,
            strokeWidth = radius,
            cap = StrokeCap.Round
        )
    }
}