package com.example.speechwaves

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import androidx.core.content.ContextCompat


class ColorsAnimator(
    context: Context,
    private val callback: ColorsUpdateCallback
) {

    private var color0Animator: ValueAnimator? = null
    private var color1Animator: ValueAnimator? = null
    private var color2Animator: ValueAnimator? = null

    val layerColors = listOf(
        ContextCompat.getColor(context, R.color.blue_layer1_secondary),
        ContextCompat.getColor(context, R.color.blue_layer2_secondary),
        ContextCompat.getColor(context, R.color.blue_layer3_secondary),
        ContextCompat.getColor(context, android.R.color.white)
    )

    private val layerColorsDarker = listOf(
        ContextCompat.getColor(context, R.color.blue_layer1_primary),
        ContextCompat.getColor(context, R.color.blue_layer2_primary),
        ContextCompat.getColor(context, R.color.blue_layer3_primary)
    )

    fun animate(darker: Boolean, currentColor: MutableList<Int>, finishedAnimation: () -> Unit) {
        color0Animator = ValueAnimator.ofInt(
            currentColor[0],
            if (darker) layerColorsDarker[0] else layerColors[0]
        ).apply {
            addUpdateListener {
                callback.onColors0Update(it.animatedValue as Int)
            }
            addListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(p0: Animator?) {
                    finishedAnimation()
                    finishAnimators()
                }
            })
            customiseAnimator()
        }
        color1Animator = ValueAnimator.ofInt(
            currentColor[1],
            if (darker) layerColorsDarker[1] else layerColors[1]
        ).apply {
            addUpdateListener {
                callback.onColors1Update(it.animatedValue as Int)
            }
            customiseAnimator()
        }
        color2Animator = ValueAnimator.ofInt(
            currentColor[2],
            if (darker) layerColorsDarker[2] else layerColors[2]
        ).apply {
            addUpdateListener {
                callback.onColors2Update(it.animatedValue as Int)
            }
            customiseAnimator()
        }
    }

    private fun finishAnimators() {
        color0Animator?.cancel()
        color1Animator?.cancel()
        color2Animator?.cancel()
    }

    private fun ValueAnimator.customiseAnimator() {
        setEvaluator(ArgbEvaluator())
        duration = 100L
        start()
    }
}

interface ColorsUpdateCallback {
    fun onColors0Update(color: Int)
    fun onColors1Update(color: Int)
    fun onColors2Update(color: Int)
}
