package com.example.speechwaves

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
    private var anglesRad = mutableListOf<Int>()

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
            setStyle(Paint.Style.STROKE)
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.set(w / 2f, h / 2f)
        calculateAngles()
    }

    private fun calculateAngles() {
        val min = 10
        val max = 360 / points + min
        var sum = 0
        for (i in 0 until points) {
            val angle = Random().nextInt(max) + min
            anglesRad.add(angle)
            sum += angle
        }
        anglesRad.add(360 - sum)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawStar(canvas)
//        drawCircle(canvas)
    }

    private fun drawStar(canvas: Canvas) {

        val path = Path()
        val angle = 360.0 / points
        val angleRad = 2 * Math.PI / points
        val startAngleLeftInRadians = -angleRad / 2
        val startAngleRightInRadians = angleRad / 2

        for (i in 0..points) {
            val angle = anglesRad[i]
            path.reset()
            canvas.save()
            val oval = RectF()

            var delta = Random().nextInt(50) * Random().nextInt(5)
            var left = center.x + (radius * Math.sin(startAngleLeftInRadians))
            var right = center.x + (radius * Math.sin(startAngleRightInRadians))
            var bottom = center.y + delta - 60
            var top = center.y - radius - delta - 60

            canvas.rotate(angle.toFloat() * i, center.x, center.y)

            oval.set(left.toFloat(), top, right.toFloat(), bottom)
            path.addOval(oval, Path.Direction.CW)

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
