package com.example.speechwaves

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.util.*

class SpeechWaves @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    style: Int = 0
) : View(context, attr, style) {

    private var center = PointF(0f, 0f)
    private var points: Int = 9
    private var radius = context.resources.getDimension(R.dimen.radius)
    private var angles = mutableListOf<Int>()

    private val wavePaint: Paint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            color = ContextCompat.getColor(getContext(), R.color.busuu_blue_lite)
            strokeWidth = getContext().resources.getDimension(R.dimen.spacing2)
            setStyle(Paint.Style.FILL)
        }

    private val circlePaint: Paint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            color = ContextCompat.getColor(getContext(), android.R.color.white)
            strokeWidth = getContext().resources.getDimension(R.dimen.spacing2)
            setStyle(Paint.Style.FILL)
        }

    init {
        val valueAnim = ValueAnimator.ofInt(0, 50)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.set(w / 2f, h / 2f)
        calculateAngles()
    }

    private fun calculateAngles() {
        val random = Random()
        val randoms = mutableListOf<Int>()
        for (i in 0..points) {
            randoms.add(random.nextInt(320) + 40)
        }
        randoms.sort()
        angles.add(randoms[0])
        for (i in 1 until points) {
            angles.add(randoms[i] - randoms[i - 1])
        }
        angles.add(360 - angles.sum())
        angles.shuffle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawStar(canvas)
        drawCircle(canvas)
    }

    private fun drawStar(canvas: Canvas) {
        val path = Path()
        var angleSum = 0
        for (i in 0..points) {
            val angle = angles[i]
            val angleRad = angle * Math.PI / 180F
            val halfAngle = angleRad / 2

            path.reset()
            canvas.save()
            val oval = RectF()

            var delta = Random().nextInt(50) * Random().nextInt(5)
            val angleOffset = radius * Math.sin(-halfAngle)
            var left = center.x + angleOffset
            var right = center.x - angleOffset

            var bottom = center.y + delta - 50
            var top = center.y - radius - delta - 50

            canvas.rotate(angleSum.toFloat(), center.x, center.y)

            oval.set(left.toFloat(), top, right.toFloat(), bottom)
            path.addOval(oval, Path.Direction.CW)

            angleSum += angle / 2
            if (i < points) {
                angleSum += angles[i + 1] / 2
            }
            canvas.drawPath(path, wavePaint)
            canvas.restore()
        }
    }

    private fun drawCircle(canvas: Canvas) {
        canvas.drawCircle(
            center.x,
            center.y,
            radius,
            circlePaint
        )
    }
}
