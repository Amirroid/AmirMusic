package ir.amirroid.amirmusics.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.audiofx.Visualizer
import android.view.View

@SuppressLint("ViewConstructor")
class VisualizerView(
    context: Context,
    color: Int
) : View(
    context
) {
    private val gap = 2.dp(context)
    private val stroke = 3.dp(context)
    private var byteArray: ByteArray? = null
    private var lines = emptyList<Pair<Float, Float>>()
    private var visualizer: Visualizer? = null

    private val paint: Paint = Paint().apply {
        this.color = color
        this.style = Paint.Style.FILL
        this.isAntiAlias = true
    }

    fun listen(audioSessionId: Int) {
        releaseVisualizer()
        visualizer = Visualizer(audioSessionId).apply {
            this.enabled = false
            captureSize = Visualizer.getCaptureSizeRange()[1]
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(p0: Visualizer?, waveform: ByteArray?, p2: Int) {
                    if (waveform != null) {
                        byteArray = waveform.copyOf()
                        invalidate()
                    }
                }

                override fun onFftDataCapture(p0: Visualizer?, p1: ByteArray?, p2: Int) = Unit

            }, Visualizer.getMaxCaptureRate(), true, false)
            this.enabled = true
        }
    }

    fun releaseVisualizer() {
        if (visualizer != null) {
            visualizer!!.release()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (byteArray != null) {
            lines = calculateOfLines(width.toFloat())
            drawVisualizer(canvas)
        }
        super.onDraw(canvas)
    }

    private fun drawVisualizer(canvas: Canvas) {
        val dataInLines = byteArray!!.size / lines.size
        var sum = 0
        var barCount = 0
        var dataCount = 0
        byteArray!!.forEach {
            sum += it
            dataCount++
            if (dataCount == dataInLines && barCount < lines.size) {
                drawLineForVisualizer(canvas, sum / dataInLines, lines[barCount])

                sum = 0
                dataCount = 0
                barCount++
            }
        }
    }

    private fun drawLineForVisualizer(canvas: Canvas, source: Int, board: Pair<Float, Float>) {
        val normalizedData = source + 128
        val heightLine = canvas.height * (normalizedData / 256f)
        val topBar = (canvas.height - heightLine) / 2f
        val rect = RectF(board.first, topBar, board.second, canvas.height - topBar)
        canvas.drawRoundRect(rect, 100f, 100f, paint)
    }


    private fun calculateOfLines(widthCanvas: Float): List<Pair<Float, Float>> {
        val firstLine = ((widthCanvas / 2 - stroke / 2) % (stroke + gap)).toInt().toFloat()
        val result = mutableListOf<Pair<Float, Float>>()
        var left = firstLine
        var right = left.plus(stroke)
        do {
            result.add(Pair(left, right))
            left = right + gap
            right = left + stroke
        } while (right <= widthCanvas)
        return result
    }
}


fun Int.dp(context: Context) = context.resources.displayMetrics.density * this