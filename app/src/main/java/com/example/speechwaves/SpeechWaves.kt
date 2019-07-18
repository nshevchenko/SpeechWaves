package com.example.speechwaves

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnRepeat
import androidx.core.content.ContextCompat
import java.util.*

private const val LAYERS_COUNT = 3
private const val FULL_ROTATION = 360

class SpeechWaves @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    style: Int = 0
) : View(context, attr, style), GetAmplitudeCallback {

    private var voiceAmplitude = 0F

    private val audioMeter: AudioMeter = AudioMeter()

    private val layerColors = listOf(
        ContextCompat.getColor(context, R.color.busuu_blue),
        ContextCompat.getColor(context, R.color.busuu_blue_lite),
        ContextCompat.getColor(context, R.color.busuu_blue_xlite)
    )

    private val blueColor = ContextCompat.getColor(context, R.color.busuu_blue)
    private val blueLightColor = ContextCompat.getColor(context, R.color.busuu_blue_xlite)

    private val random = Random()
    private var waveAnimator: ValueAnimator? = null
    private var waveRadiusOffset = 0f
        set(value) {
            field = value
            postInvalidateOnAnimation()
        }

    private var center = PointF(0f, 0f)
    private var radius = context.resources.getDimension(R.dimen.radius)
    private var circleRadius = context.resources.getDimension(R.dimen.circle_offset)
    private var tempRadius = 0F
    private var angleSum = 0F

    private val points = mutableListOf<Int>()
    private var angles = mutableListOf<Int>()
    private var deltas = mutableListOf<Float>()

    private var angle = 0
    private var angleRad = 0.0
    private var halfAngle = 0.0
    private var oval = RectF()
    private var delta = 0F
    private var angleOffset = 0.0
    private var left = 0.0
    private var right = 0.0
    private var bottom = 0F
    private var top = 0F

    private val wavePaint: Paint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            strokeWidth = getContext().resources.getDimension(R.dimen.spacing2)
            setStyle(Paint.Style.FILL)
        }

    private val circlePaint: Paint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            color = ContextCompat.getColor(getContext(), R.color.busuu_blue_lite)
            strokeWidth = getContext().resources.getDimension(R.dimen.spacing2)
            setStyle(Paint.Style.FILL)
        }

    override fun onAmplitudeUpdate(amplitude: Int) {
        System.out.println(amplitude / 2000F)
        this.voiceAmplitude = Math.min(amplitude / 1000F, 20F)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (center.x == 0F) {
            center.set(w / 2f, h / 2f)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        recalculateShape()
        waveRadiusOffset = 1f
        waveAnimator = ValueAnimator.ofFloat(0f, 5f, 0f).apply {
            addUpdateListener {
                waveRadiusOffset = it.animatedValue as Float
            }
            duration = 100L
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            doOnRepeat {
                recalculateShape()
            }
            start()
        }
    }

    private fun recalculateShape() {
        calculateAngles()
        calculateArcHeights()
    }

    private var dominantIndex = 0
    private var minHeight = 2F
    private var dominantHeight = 20F
    private var subDominantLeftIndex = 0
    private var subDominantRightIndex = 0

    private fun calculateArcHeights() {
        deltas.clear()

        dominantIndex = random.nextInt(angles.size - 2) + 1
        dominantHeight = minHeight * voiceAmplitude

        for (i in 0..angles.size) {
            deltas.add(minHeight)
        }
        subDominantLeftIndex = dominantIndex - 1
        subDominantRightIndex = dominantIndex + 1

        deltas[subDominantLeftIndex] = (dominantHeight / 4)
        deltas[subDominantRightIndex] = (dominantHeight / 4)
        deltas[dominantIndex] = dominantHeight
    }

    private var averageAngle = 0
    private fun calculateAngles() {
        angles.clear()
        averageAngle = random.nextInt(20) + 30
        while (angles.sum() < FULL_ROTATION - averageAngle) {
            angles.add(averageAngle + random.nextInt(20) - 5)
        }
        angles.add(averageAngle)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawAllArcs(canvas)
        drawBlueCircle(canvas)
        drawCircle(canvas)
    }

    private fun drawAllArcs(canvas: Canvas) {
        val path = Path()
        angleSum = 0F
        for (layerN in LAYERS_COUNT downTo 1) {
            angleSum = 0F
            for (i in 0 until angles.size) {
                drawOneLayerOfArc(layerN, path, canvas, i)
            }
        }
    }

    private fun drawOneLayerOfArc(
        layerN: Int,
        path: Path,
        canvas: Canvas,
        i: Int
    ) {
        path.reset()
        canvas.save()

        tempRadius = radius
        if (layerN > 1) {
            tempRadius *= waveRadiusOffset * (deltas[i] / 20) * (layerN - 1) / 10F + 1
        }

        angle = angles[i]
        delta = (deltas[i])

        angleRad = angle * Math.PI / 180F
        halfAngle = angleRad / 2

        angleOffset = tempRadius * Math.sin(-halfAngle)

        left = center.x + angleOffset
        right = center.x - angleOffset
        bottom = center.y + delta
        top = center.y - tempRadius - (delta * waveRadiusOffset)

        canvas.rotate(angleSum, center.x, center.y)

        oval.set(left.toFloat(), top, right.toFloat(), bottom)
        path.addOval(oval, Path.Direction.CW)

        angleSum += angle / 2
        if (i < angles.size - 1) {
            angleSum += angles[i + 1] / 2
        }
        wavePaint.color = layerColors[layerN - 1]
        canvas.drawPath(path, wavePaint)
        canvas.restore()
    }

    private fun drawCircle(canvas: Canvas) {
        circlePaint.color = ContextCompat.getColor(context, android.R.color.white)
        canvas.drawCircle(
            center.x,
            center.y,
            radius - circleRadius,
            circlePaint
        )
    }

    private fun drawBlueCircle(canvas: Canvas) {
        circlePaint.color = blueColor
        canvas.drawCircle(
            center.x,
            center.y,
            radius,
            circlePaint
        )
    }

    fun start() {
        audioMeter.start(this)
    }
}
