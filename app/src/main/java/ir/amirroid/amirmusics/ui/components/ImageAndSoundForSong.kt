package ir.amirroid.amirmusics.ui.components

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ir.amirroid.amirmusics.R
import ir.amirroid.amirmusics.utils.dp
import ir.amirroid.amirmusics.utils.minWidthRadius
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@Composable
fun ImageAndSoundForSong(
    image: Any?,
    context: Context,
    soundLevel: Float,
    onSoundLevelChanged: (Float) -> Unit,
    shape: Shape = CircleShape,
) {
    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp
    Box(modifier = Modifier.size(width.dp * .74f)) {
        AsyncImage(
            ImageRequest.Builder(context)
                .data(image)
                .crossfade(true)
                .error(R.drawable.img)
                .placeholder(R.drawable.img)
                .crossfade(200)
                .build(), contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(shape),
            contentScale = ContentScale.Crop
        )
        SoundChanger(progress = soundLevel, context = context) {
            onSoundLevelChanged.invoke(
                abs(if (it == 1f) 1f else if (it == 0f) 0f else 1 - it).coerceIn(
                    0f,
                    1f
                )
            )
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BoxScope.SoundChanger(
    progress: Float,
    context: Context,
    onProgressChanged: (Float) -> Unit
) {
    val color = MaterialTheme.colorScheme.surfaceVariant.copy(0.7f)
    val activeColor = MaterialTheme.colorScheme.primary
//    val thumbColor = MaterialTheme.colorScheme.onPrimary
    var div2 = 0f
    var position = Offset.Zero

    var handleCenter by remember {
        mutableStateOf(Offset.Zero)
    }
    var ceneter = Offset.Zero
    Canvas(
        modifier = Modifier
            .fillMaxHeight(0.5f)
            .fillMaxWidth()
            .onGloballyPositioned {
                position = it.positionInWindow()
                handleCenter = it.positionInWindow()
            }
            .align(Alignment.BottomCenter)
            .pointerInteropFilter { event ->
                handleCenter += Offset(event.x, event.y)
                var progressN =
                    getRotationAngle(handleCenter, Offset(position.x + div2, position.y))
                        .coerceIn(
                            0.0,
                            180.0
                        )
                        .toFloat()
                Log.d("TAGPROGRESS", "SoundChanger: $progressN")
                if (progressN >= 180) {
                    progressN = if (handleCenter.x.minWidthRadius(context)) {
                        0f
                    } else {
                        1f
                    }
                } else {
                    progressN /= 180f
                }

                onProgressChanged.invoke(
                    progressN
                        .coerceIn(0f, 1f)
                )
                true
            }
            .draggable(rememberDraggableState {}, Orientation.Vertical)
    ) {
        div2 = size.width.div(2)
        val circleSize = Size(size.width, size.height * 2)
        drawArc(
            color,
            0f,
            180f,
            false,
            size = circleSize,
            style = Stroke(4.dp(context), cap = StrokeCap.Round),
            topLeft = Offset(0f, -size.height)
        )
        drawArc(
            activeColor,
            180f,
            -(progress * 180),
            false,
            size = circleSize,
            style = Stroke(6.dp(context), cap = StrokeCap.Round),
            topLeft = Offset(0f, -size.height)
        )
        val beta = (-180 * progress + 180) * (PI / 180f).toFloat()
        val rx = cos(beta) * div2
        val ry = sin(beta) * div2
        drawPoints(
            listOf(Offset(div2 + rx, ry)),
            pointMode = PointMode.Points,
            color = activeColor,
            strokeWidth = 18.dp(context),
            cap = StrokeCap.Round
        )
    }
}


private fun getRotationAngle(currentPosition: Offset, center: Offset): Double {
    val (dx, dy) = currentPosition - center
    val theta = atan2(dy, dx).toDouble()

    var angle = Math.toDegrees(theta)

    if (angle < 0) {
        angle += 360.0
    }
    return angle
}

// rounded corner

//        val size = size.width
//        val path = android.graphics.Path()
//        path.moveTo(4.dp(context), size * .68f)
//        path.lineTo(4.dp(context), size - 12.dp(context))
//        path.quadTo(4.dp(context), size - 12.dp(context), 12.dp(context), size - 4.dp(context))
//        path.lineTo(size - 12.dp(context), size - 4.dp(context))
//        path.quadTo(
//            size - 12.dp(context),
//            size - 4.dp(context),
//            size - 4.dp(context),
//            size - 12.dp(context)
//        )
//        path.lineTo(size - 4.dp(context), size * .68f)
//        measurable.setPath(path.asComposePath(), false)
//        measurable.getSegment(
//            0f,
//            measurable.length * progress,
//            progressPath.value,
//            true
//        )
//        drawPath(path = path.asComposePath(), color = color, style = Stroke(4.dp(context), cap = StrokeCap.Round))
//        drawPath(path = progressPath.value, color = activeColor, style = Stroke(6.dp(context), cap = StrokeCap.Round))
//        drawCircle(thumbColor, 8.dp(context), center = Offset(4.dp(context), size * .68f))