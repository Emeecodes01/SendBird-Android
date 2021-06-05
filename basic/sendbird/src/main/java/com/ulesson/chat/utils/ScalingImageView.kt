package com.ulesson.chat.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView

/**
 * Created by Rahul Rawat on 8/8/19.
 */

class ScalingImageView : AppCompatImageView {
    private var mAdjustViewBounds: Boolean = false
    private var mMaxWidth: Int = 0
    private var mMaxHeight: Int = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override //  getAdjustViewBounds() was added in api level 16, so for backwards compatibility sake...
    fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        super.setAdjustViewBounds(adjustViewBounds)
        mAdjustViewBounds = adjustViewBounds
    }

    override fun setMaxWidth(maxWidth: Int) {
        super.setMaxWidth(maxWidth)
        mMaxWidth = maxWidth
    }

    override fun setMaxHeight(maxHeight: Int) {
        super.setMaxHeight(maxHeight)
        mMaxHeight = maxHeight
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mDrawable = drawable
        var mDrawableWidth = mDrawable?.intrinsicWidth ?: -1
        var mDrawableHeight = mDrawable?.intrinsicHeight ?: -1

        // ------------
        var w: Int
        var h: Int

        // Desired aspect ratio of the view's contents (not including padding)
        var desiredAspect = 0.0f

        // We are allowed to change the view's width
        var resizeWidth = false

        // We are allowed to change the view's height
        var resizeHeight = false

        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val actualWidth = MeasureSpec.getSize(widthMeasureSpec)
        val actualHeight = MeasureSpec.getSize(heightMeasureSpec)

        if (mDrawable == null) {
            // If no drawable, its intrinsic size is 0.
            mDrawableWidth = -1
            mDrawableHeight = -1
            h = 0
            w = h
        } else {
            w = mDrawableWidth
            h = mDrawableHeight
            if (w <= 0) w = 1
            if (h <= 0) h = 1

            // We are supposed to adjust view bounds to match the aspect
            // ratio of our drawable. See if that is possible.
            desiredAspect = w.toFloat() / h.toFloat()

            if (mAdjustViewBounds) {
                // Original Android code setting whether to resizeHeight / width.
                /* resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
                   resizeHeight = heightSpecMode != MeasureSpec.EXACTLY; */
                // Modified code which resizes no matter what MeasureSpec is set to. Works better with fill_parent/match_parent.
                val actualAspect = actualWidth.toFloat() / actualHeight.toFloat()
                if (actualAspect > desiredAspect)
                    resizeWidth = true
                else if (actualAspect < desiredAspect) resizeHeight = true
            }
        }

        val pleft = paddingLeft
        val pright = paddingRight
        val ptop = paddingTop
        val pbottom = paddingBottom

        var widthSize: Int
        var heightSize: Int

        if (resizeWidth || resizeHeight) {
            /* If we get here, it means we want to resize to match the
                drawables aspect ratio, and we have the freedom to change at
                least one dimension.
            */

            // Get the max possible width given our constraints
            widthSize = resolveAdjustedSize(w + pleft + pright, mMaxWidth, widthMeasureSpec)

            // Get the max possible height given our constraints
            heightSize = resolveAdjustedSize(h + ptop + pbottom, mMaxHeight, heightMeasureSpec)

            if (desiredAspect != 0.0f) {
                // See what our actual aspect ratio is
                val actualAspect =
                        (widthSize - pleft - pright).toFloat() / (heightSize - ptop - pbottom)

                if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {

                    var done = false

                    // Try adjusting width to be proportional to height
                    if (resizeWidth) {
                        val newWidth = (desiredAspect * (heightSize - ptop - pbottom)).toInt() +
                                pleft + pright
                        if (newWidth <= widthSize) {
                            widthSize = newWidth
                            done = true
                        }
                    }

                    // Try adjusting height to be proportional to width
                    if (!done && resizeHeight) {
                        val newHeight = ((widthSize - pleft - pright) / desiredAspect).toInt() +
                                ptop + pbottom
                        if (newHeight <= heightSize) {
                            heightSize = newHeight
                        }
                    }
                }
            }
        } else {
            /* We are either don't want to preserve the drawables aspect ratio,
               or we are not allowed to change view dimensions. Just measure in
               the normal way.
            */
            w += pleft + pright
            h += ptop + pbottom

            w = Math.max(w, suggestedMinimumWidth)
            h = Math.max(h, suggestedMinimumHeight)

            widthSize = resolveSizeAndState(w, widthMeasureSpec, 0)
            heightSize = resolveSizeAndState(h, heightMeasureSpec, 0)
        }

        Log.i(
                "F&C",
                "Setting dimen $widthSize x $heightSize for $id Adjusted w/h:$resizeWidth/$resizeHeight"
        )
        setMeasuredDimension(widthSize, heightSize)
    }

    private fun resolveAdjustedSize(
            desiredSize: Int,
            maxSize: Int,
            measureSpec: Int
    ): Int {
        var result = desiredSize
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        when (specMode) {
            MeasureSpec.UNSPECIFIED ->
                /* Parent says we can be as big as we want. Just don't be larger
                   than max size imposed on ourselves.
                */
                result = Math.min(desiredSize, maxSize)
            MeasureSpec.AT_MOST ->
                // Parent says we can be as big as we want, up to specSize.
                // Don't be larger than specSize, and don't be larger than
                // the max size imposed on ourselves.
                result = Math.min(Math.min(desiredSize, specSize), maxSize)
            MeasureSpec.EXACTLY ->
                // No choice. Do what we are told.
                result = specSize
        }
        return result
    }
}
