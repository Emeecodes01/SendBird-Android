package com.sendbird.utils

import android.widget.ImageView

data class Theme(
        val subjectName: Int,
        val colorPrimary: Int,
        val colorPrimaryDark: Int,
        val colorSecondary: Int,
        val overlayColor: Int,
        val plainSubjectIcon: Int,
        val dashboardBtnBackground: Int,
        val videoPlayIcon: Int,
        val videoPauseIcon: Int,
        val chapterDetailBg: BackgroundComponent,
        val pathColor: Int,
        val mileStoneTextdrawable: Int,
        val mileStonedrawable: Int,
        val journeyBackgroundComponent: List<BackgroundComponent>,
        val analysisBigBg: Int,
        val analysisSmallBg: Int,
        val practiceBeginnerDrawableResId: Int,
        val practiceProDrawableResId: Int,
        val practiceMasterDrawableResId: Int,
        val filterFillIconResId: Int,
        val filterNoFillIconResId: Int,
        val expandedIconResId: Int,
        val expandedIconBackResId: Int,
        val collapsedIconResId: Int,
        val playPauseSelectorResId: Int,
        val lockIconResId: Int,
        val lockIconResIdSmall: Int,
        val testNowBtnResId: Int,
        val nextLessonResId: Int,
        val bigBannerBg: Int,
        val listBannerBg: Int
)


/**
 * CAUTION: either use fraction or absolute values. Mixing these 2 will cause issue
 */
data class BackgroundComponent(
        val drawableResourceId: Int,

        // same dimen as in design
        val width: Int,
        val height: Int,

        // ratio of position and screen dimension
        val frPositionX: Float = 0f,
        val frPositionY: Float = 0f,

        val positionX: Int = -1,
        val positionY: Int = -1,

        var repeatTimes: Int = 1,
        val frSeparationX: Float = 0f,
        val frSeparationY: Float = 0f,

        val separationX: Int = -1,
        val separationY: Int = -1,

        val scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER
) {

    fun calculateRepeat(totalViewHeight: Int, screenHeight: Int): BackgroundComponent {
        var frY: Float
        var i = 1
        while (true) {
            frY = (frPositionY + frSeparationY * i) * screenHeight
            if (totalViewHeight < frY)
                break
            i++
        }
        repeatTimes = i
        return this
    }

    companion object {
        // horizontal positions
        const val POS_START = 0f
        const val POS_END = 1f
        const val POS_HOR_CENTER = .5f

        // vertical positions
        const val POS_TOP = 0f
        const val POS_BOTTOM = 1f
        const val POS_VER_CENTER = .5f

        fun List<BackgroundComponent>.calculateRepeat(
                totalViewHeight: Int,
                screenHeight: Int
        ): List<BackgroundComponent> {
            val list = ArrayList<BackgroundComponent>()

            for (item in this) {
                list.add(item.copy(repeatTimes = -1).calculateRepeat(totalViewHeight, screenHeight))
            }

            return list
        }
    }
}