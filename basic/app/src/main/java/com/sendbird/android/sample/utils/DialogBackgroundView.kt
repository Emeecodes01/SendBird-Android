package com.sendbird.android.sample.utils

import android.animation.*
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.PathParser
import com.sendbird.android.sample.R
import kotlin.math.max

class DialogBackgroundView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    // path data
    private val pathData =
        "M108.695 655.791C356.562 675.209 437.551 495.554 436.974 311.143C436.076 24.0533 110.318 -61.5805 -35.2249 43.5488C-185.445 152.686 -96.0858 640.002 108.695 655.791Z"
    private val bgHeight = 322f
    private val bgWidth = 360f

    private val startYFromBottom = context.dip(0).toFloat()
    private val endYFromBottom: Float by lazy { (height + context.dip(105)).toFloat() }

    private val scaleFactorAsPerWidth: Float by lazy { width / bgWidth }
    private val aspectRatio = bgWidth / bgHeight
    private val scaleFactorAsPerHeight: Float
        get() {
            return currentYPosFromBottom / bgHeight
        }

    fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    // original bg paths
    private val originalPath: Path = PathParser.createPathFromPathData(pathData)

    // bg color resources
    private val colorResource = R.color.color_white

    // temp variable to be used for transformation
    private val tempPath: Path = Path()
    private val tm: Matrix = Matrix()

    // bg respective path transformation matrices
    private val pathTM: Matrix
        get() = tm.apply {
            reset()
            tm.postScale(
                max(scaleFactorAsPerHeight * aspectRatio, scaleFactorAsPerWidth),
                scaleFactorAsPerHeight
            )
            tm.postTranslate(0f, currentYPos)
        }

    // bg respective transformed paths
    private val bg1Path: Path
        get() = tempPath.apply {
            reset()
            addPath(originalPath)
            transform(pathTM)
        }

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, colorResource)
    }

    // bg respective current pos from top calculated using pos from bottom
    private val currentYPos: Float get() = getYFromTop(currentYPosFromBottom)

    // bg respective current pos from bottom
    private var currentYPosFromBottom: Float = startYFromBottom

    private val visibleAreaPath: Path by lazy {
        Path().apply { addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW) }
    }

    private val tempPath2 = Path()

    private fun getYFromTop(yFromBottom: Float): Float = height - yFromBottom

    override fun onDraw(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= 23) {
            tempPath2.apply { reset(); set(bg1Path) }
            canvas.drawPath(
                tempPath2.apply { op(tempPath2, visibleAreaPath, Path.Op.INTERSECT) },
                paint
            )
        } else {
            canvas.drawPath(bg1Path, paint)
        }
    }

    private fun createAnimator(startY: Float, endY: Float, animateDuration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(startY, endY).apply {
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                currentYPosFromBottom = value
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    currentYPosFromBottom = startY
                }

                override fun onAnimationEnd(animation: Animator?) {
                    currentYPosFromBottom = endY
                }

                override fun onAnimationCancel(animation: Animator?) {
                    currentYPosFromBottom = endY
                }
            })
            this.interpolator = LinearInterpolator()
            duration = animateDuration
        }
    }

    fun startAnimation(endColorResId: Int, duration: Long = 800L, startDelay: Long = 0L) {
        visibility = VISIBLE
        val scaleAnimator = createAnimator(startYFromBottom, endYFromBottom, duration)
        scaleAnimator.startDelay = startDelay

        val startColor = ContextCompat.getColor(context, R.color.color_white)
        val endColor = ContextCompat.getColor(context, endColorResId)

        val colorAnimator =
            ObjectAnimator.ofObject(paint, "color", ArgbEvaluator(), startColor, endColor)
        colorAnimator.duration = scaleAnimator.duration
        colorAnimator.startDelay = scaleAnimator.startDelay

        scaleAnimator.start()
        colorAnimator.start()
    }
}