/*
 * Copyright 2019 Keiju Matsumoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.keijumt.passwordview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.FAKE_BOLD_TEXT_FLAG
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.interpolator.view.animation.FastOutLinearInInterpolator

internal class CircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val outLinePaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val fillCirclePaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val fillAndStrokeCirclePaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
    }

    private val textPaint = TextPaint(FAKE_BOLD_TEXT_FLAG or ANTI_ALIAS_FLAG).apply {
        textSize = 50f
        color = Color.BLACK
    }

    private var radius = 16f

    private var animator: ValueAnimator? = null

    private var inputAndRemoveAnimationDuration = 200L

    private var progress = 0.0f
        set(value) {
            field = value
            postInvalidateOnAnimation()
        }

    private var isTextMode = false

    var text: String? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val textWidth = textPaint.measureText("0").toInt()
        val textHeight = (Math.abs(textPaint.ascent()) + textPaint.descent()).toInt()
        val width = ((radius * 2) + (outLinePaint.strokeWidth)).toInt()
        val height = ((radius * 2) + (outLinePaint.strokeWidth)).toInt()
        setMeasuredDimension(maxOf(width, textWidth), maxOf(height, textHeight))
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        val halfOutLineStrokeWidth = outLinePaint.strokeWidth / 2

        if (!isTextMode || text == null) {
            // fill circle
            canvas.drawCircle(
                width.toFloat() / 2,
                height.toFloat() / 2,
                lerp(radius - halfOutLineStrokeWidth, 0f, progress),
                fillCirclePaint
            )

            // outline circle
            canvas.drawCircle(
                width.toFloat() / 2,
                height.toFloat() / 2,
                lerp(radius, 0f, progress),
                outLinePaint
            )

            // fill and stroke circle
            canvas.drawCircle(
                width.toFloat() / 2,
                height.toFloat() / 2,
                lerp(0f, radius + halfOutLineStrokeWidth, progress),
                fillAndStrokeCirclePaint
            )
        } else {
            val baseX = (width / 2 - textPaint.measureText(text) / 2)
            val baseY = (height - textPaint.descent())
            canvas.drawText(text!!, baseX, baseY, textPaint)
        }
    }

    fun animateAndInvoke(onEnd: ((CircleView) -> Unit)? = null) {
        if (animator != null) {
            return
        }

        val newProgress = if (progress == 0f) 1f else 0f
        animator = ValueAnimator.ofFloat(progress, newProgress).apply {
            duration = inputAndRemoveAnimationDuration
            addUpdateListener {
                progress = it.animatedValue as Float
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    animator = null
                    onEnd?.invoke(this@CircleView)
                }
            })
            interpolator = FastOutLinearInInterpolator()
        }
        animator?.start()
    }

    fun setRadius(radius: Float) {
        this.radius = radius
        invalidate()
    }

    fun setTextColor(color: Int) {
        textPaint.color = color
        invalidate()
    }

    fun setTextMode(isTextMode: Boolean) {
        this.isTextMode = isTextMode
        invalidate()
    }

    fun setTextSize(textSize: Int) {
        textPaint.textSize = textSize.toFloat()
        invalidate()
    }

    fun setFillCircleColor(color: Int) {
        fillCirclePaint.color = color
        postInvalidateOnAnimation()
    }

    fun setOutLineColor(color: Int) {
        outLinePaint.color = color
        postInvalidateOnAnimation()
    }

    fun setFillAndStrokeCircleColor(color: Int) {
        fillAndStrokeCirclePaint.color = color
        postInvalidateOnAnimation()
    }

    fun setOutlineStrokeWidth(strokeWidth: Float) {
        outLinePaint.strokeWidth = strokeWidth
    }

    fun isAnimating(): Boolean = animator != null

    fun getFillAndStrokeCircleColor(): Int = fillAndStrokeCirclePaint.color

    fun getFillCircleColor(): Int = fillCirclePaint.color

    fun getOutLineColor(): Int = outLinePaint.color

    fun setInputAndRemoveAnimationDuration(duration: Long) {
        inputAndRemoveAnimationDuration = duration
    }

    /*
     * Linearly interpolate between two values.
     */
    private fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }
}