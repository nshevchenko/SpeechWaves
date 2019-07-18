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

    private val colorsList = mutableListOf(
        ContextCompat.getColor(context, R.color.blue_layer1_primary),
        ContextCompat.getColor(context, R.color.blue_layer2_primary),
        ContextCompat.getColor(context, R.color.blue_layer3_primary)
    )

    private val blueColor = ContextCompat.getColor(context, R.color.busuu_blue)

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
    private var smallerRadius = 0F
    private var angles = mutableListOf<Int>()
    private var deltas = mutableListOf<Float>()

    private var tempRadius = 0F
    private var angleSum = 0F
    private var angle = 0
    private var angleRad = 0.0
    private var halfAngle = 0.0
    private var oval = RectF()
    private var delta = 0F
    private var angleOffset = 0F
    private var averageAngle = 0
    private var dominantIndex = 0
    private var minHeight = 1F
    private var dominantHeight = 20F
    private var dominantMultiplayer = 0F

    private var bm = BitmapFactory.decodeResource(resources, R.drawable.stripes)
    private var shader: BitmapShader = BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)

    private val wavePaint: Paint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            strokeWidth = getContext().resources.getDimension(R.dimen.spacing2)
            setStyle(Paint.Style.FILL)
        }

    private val waveShader: Paint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            shader = this@SpeechWaves.shader
            setStyle(Paint.Style.STROKE)
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
        waveAnimator = ValueAnimator.ofFloat(0f, 10f, 0f).apply {
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
        colorsAnimator.animate(darker = true)
    }

    private fun recalculateShape() {
        calculateAngles()
        calculateArcHeights()
    }

    private fun calculateArcHeights() {
        deltas.clear()

        dominantIndex = random.nextInt(angles.size - 2) + 1
        dominantMultiplayer = random.nextInt(3) + 1F
        dominantHeight = minHeight * voiceAmplitude * 2

        if (voiceAmplitude > 1)
            dominantHeight = Math.min(voiceAmplitude * dominantMultiplayer, 20F)

        for (i in 0..angles.size) {
            deltas.add(if (voiceAmplitude > 1) minHeight else 0F)
        }
        deltas[dominantIndex - 1] = (dominantHeight / 4)
        deltas[dominantIndex + 1] = (dominantHeight / 4)
        deltas[dominantIndex] = dominantHeight
    }

    private fun calculateAngles() {
        angles.clear()
        averageAngle = random.nextInt(20) + 40
        while (angles.sum() < FULL_ROTATION - averageAngle) {
            angles.add(averageAngle + random.nextInt(20) - 5)
        }
        angles.add(averageAngle)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (layerN in LAYERS_COUNT downTo 1) {
            angleSum = 0F
            tempRadius = radius
            smallerRadius = radius
            if (layerN > 1) {
                tempRadius *= (layerN - 1) / 3F + 1
                smallerRadius *= (layerN - 2) / 3F + 1
            }
            if (layerN != 1) {
                drawBlueCircle(canvas, colorsList[layerN - 1], tempRadius)
            }
            drawOneLayerOfArc(layerN, canvas)
        }
        drawCircle(canvas)
    }

    private fun drawOneLayerOfArc(
        layerN: Int,
        canvas: Canvas
    ) {
        for (i in 0 until angles.size) {
            path.reset()
            canvas.save()

            angle = angles[i]
            delta = (deltas[i])

            angleRad = angle * Math.PI / 180F
            halfAngle = angleRad / 2
            angleOffset = tempRadius * Math.sin(-halfAngle).toFloat()

            oval.set(
                center.x + angleOffset,
                center.y - tempRadius - (delta * waveRadiusOffset),
                center.x - angleOffset,
                center.y + delta
            )
            canvas.rotate(angleSum, center.x, center.y)
            path.addOval(oval, Path.Direction.CW)

            angleSum += angle / 2
            if (i < angles.size - 1) {
                angleSum += angles[i + 1] / 2
            }
            wavePaint.color = colorsList[layerN - 1]
            canvas.drawPath(path, wavePaint)
            if (layerN == 1) {
                canvas.drawPath(path, waveStrokePaint)
                canvas.drawPath(path, waveShader)
            }
            canvas.restore()
        }
    }

    private fun drawCircle(canvas: Canvas) {
        circlePaint.color = ContextCompat.getColor(context, android.R.color.white)
        canvas.drawCircle(
            center.x,
            center.y,
            radius + 1,
            circlePaint
        )
    }

    private fun drawBlueCircle(
        canvas: Canvas,
        color: Int,
        layerRadius: Float
    ) {
        circlePaint.color = color
//        circlePaint.shader = RadialGradient(
//            center.x,
//            center.y,
//            layerRadius,
//            intArrayOf(Color.BLACK, Color.RED),
//            floatArrayOf(1 -  (layerRadius / smallerRadius), 1F),
//            TileMode.CLAMP
//        )

        canvas.drawCircle(
            center.x,
            center.y,
            layerRadius,
            circlePaint
        )
    }

    fun start() {
        audioMeter.start(this)
    }
}
