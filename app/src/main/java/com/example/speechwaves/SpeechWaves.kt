package com.example.speechwaves

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Shader.TileMode
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnRepeat
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.math.sin


private const val LAYERS_COUNT = 3
private const val FULL_ROTATION = 360

class SpeechWaves @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    style: Int = 0
) : View(context, attr, style), GetAmplitudeCallback, ColorsUpdateCallback {

    private var voiceAmplitude = 0F

    private val audioMeter: AudioMeter = AudioMeter()
    private val colorsAnimator = ColorsAnimator(context, this)

    private var colorsList = mutableListOf(
        ContextCompat.getColor(context, R.color.busuu_blue),
        ContextCompat.getColor(context, R.color.blue_layer2_primary),
        ContextCompat.getColor(context, R.color.blue_layer3_primary)
    )

    private val blueColor = ContextCompat.getColor(context, R.color.blue_layer1_primary)

    private val random = Random()
    private var waveAnimator: ValueAnimator? = null
    private var waveRadiusOffset = 0f
        set(value) {
            field = value
            postInvalidateOnAnimation()
        }

    private val path = Path()
    private var center = PointF(0f, 0f)
    private var radius = context.resources.getDimension(R.dimen.radius)
    private var futureRadius = 0F
    private var angles = mutableListOf<Int>()
    private var deltas = mutableListOf<Float>()

    private var tempRadius = 0F
    private var rotationAngle = 0F
    private var angle = 0
    private var angleRad = 0.0
    private var halfAngle = 0.0
    private var oval = RectF()
    private var delta = 0F
    private var angleOffset = 0F
    private var averageAngle = 0
    private var dominantIndex = 0
    private var minHeight = 1.3F
    private var dominantHeight = 20F
    private var dominantMultiplayer = 0F
    private var colorsAnimating = false
    private var colorsGettingDarker = false

    private val wavePaint: Paint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            strokeWidth = getContext().resources.getDimension(R.dimen.spacing2)
            setStyle(Paint.Style.FILL)
        }

    private val waveStrokePaint: Paint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            color = blueColor
            strokeWidth = getContext().resources.getDimension(R.dimen.spacing1)
            setStyle(Paint.Style.STROKE)
        }

    private val circlePaint: Paint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            strokeWidth = getContext().resources.getDimension(R.dimen.spacing2)
            setStyle(Paint.Style.FILL)
        }

    override fun onColors0Update(color: Int) {
        colorsList[0] = color
    }

    override fun onColors1Update(color: Int) {
        colorsList[1] = color
    }

    override fun onColors2Update(color: Int) {
        colorsList[2] = color
    }

    override fun onAmplitudeUpdate(amplitude: Int) {
        this.voiceAmplitude = amplitude / 1000F
        animateColors(voiceAmplitude > 1.5F)
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
    }

    fun start() {
        audioMeter.start(this)
        colorsList = colorsAnimator.layerColors.toMutableList()
        startWaveRadiusAnimation()
    }

    fun stop() {
        waveAnimator?.cancel()
        waveAnimator = null
    }

    private fun animateColors(darker: Boolean) {
        if (!colorsAnimating) {
            if (darker != colorsGettingDarker) {
                colorsAnimating = true
                colorsGettingDarker = darker
                colorsAnimator.animate(darker, colorsList) { colorsAnimating = false }
            }
        }
    }

    private fun startWaveRadiusAnimation() {
        waveAnimator = ValueAnimator.ofFloat(0f, 10f, 0f).apply {
            addUpdateListener {
                waveRadiusOffset = it.animatedValue as Float
            }
            duration = 270L
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

    private fun calculateArcHeights() {
        deltas.clear()

        dominantIndex = random.nextInt(angles.size - 2) + 1
        dominantMultiplayer = 3F
        dominantHeight = minHeight * voiceAmplitude * 2

        if (voiceAmplitude > 1)
            dominantHeight = Math.min(voiceAmplitude * dominantMultiplayer * 2, 20F)

        for (i in 0..angles.size) {
            deltas.add(if (voiceAmplitude > 1) minHeight else 0F)
        }
        deltas[dominantIndex - 1] = (dominantHeight / 2)
        deltas[dominantIndex + 1] = (dominantHeight / 2)
        deltas[dominantIndex] = dominantHeight
    }

    private fun calculateAngles() {
        angles.clear()
        averageAngle = random.nextInt(20) + 40
        while (angles.sum() < FULL_ROTATION - averageAngle) {
            angles.add(averageAngle + random.nextInt(30) - 10)
        }
        angles.add(averageAngle)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        for (layerN in 3 downTo 1) {
            canvas.save()
            tempRadius = radius
            futureRadius = radius
            if (layerN > 1) {
                tempRadius *= (layerN - 1) / 5F + 1
                futureRadius *= (layerN - 2) / 5F + 1
            }
            drawOneLayerOfArc(layerN, canvas)
            if (layerN != 1) {
                drawBlueCircle(canvas, tempRadius, layerN)
            }
            canvas.restore()
        }
        drawCircle(canvas)
    }

    private fun drawOneLayerOfArc(
        layerN: Int,
        canvas: Canvas
    ) {
        for (i in 0 until angles.size) {
            path.reset()

            angle = angles[i]
            delta = (deltas[i])

            angleRad = angle * Math.PI / 180F
            halfAngle = angleRad / 2
            angleOffset = tempRadius * sin(-halfAngle).toFloat()

            oval.set(
                center.x + angleOffset,
                center.y - tempRadius - (delta * waveRadiusOffset) * (layerN) / 3F,
                center.x - angleOffset,
                center.y + delta
            )
            rotationAngle = angle / 2F
            if (i < angles.size - 1) {
                rotationAngle += angles[i + 1] / 2
            }
            canvas.rotate(rotationAngle, center.x, center.y)
            path.addOval(oval, Path.Direction.CW)

            wavePaint.color = colorsList[layerN - 1]
            canvas.drawPath(path, wavePaint)
            if (layerN == 1) {
                canvas.drawPath(path, waveStrokePaint)
            }
        }
    }

    private fun drawCircle(canvas: Canvas) {
        circlePaint.color = ContextCompat.getColor(context, android.R.color.white)
        circlePaint.shader = null
        canvas.drawCircle(
            center.x,
            center.y,
            radius + 1,
            circlePaint
        )
    }

    private fun drawBlueCircle(
        canvas: Canvas,
        layerRadius: Float,
        layerN: Int
    ) {
        circlePaint.shader = RadialGradient(
            center.x,
            center.y,
            layerRadius,
            intArrayOf(colorsList[layerN - 1], colorsList[layerN - 2], colorsList[layerN - 1]),
            floatArrayOf(0F, futureRadius / layerRadius, 1F),
            TileMode.CLAMP
        )

        canvas.drawCircle(
            center.x,
            center.y,
            layerRadius,
            circlePaint
        )
    }
}
