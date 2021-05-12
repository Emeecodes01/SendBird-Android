package com.sendbird.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.*
import android.widget.ImageView
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.sendbird.R
import com.sendbird.utils.TextUtils.THEME_MATH
import kotlin.math.hypot

abstract class BaseBottomSheetDialog : DialogFragment() {

    private lateinit var slideDown: Animation
    private lateinit var slideUp: Animation
    private lateinit var badgeSlideUp: Animation
    private lateinit var badgeFadeIn: Animation
    private lateinit var badgeAnimationSet: AnimationSet
    private var showAppearanceAnimation = true

    private var isBadgeAnimationOnGoing: Boolean = false
    private var postBadgeAnimation: Boolean = false

    private val badgeAnimationHandler = Handler()
    private val badgeAnimationRunnable = {
        isBadgeAnimationOnGoing = false
    }

    var subjectThemeKey: String = THEME_MATH
    lateinit var theme: Theme

    private val animationListener by lazy {
        object : AnimationListenerAdapter() {
            override fun onAnimationEnd(animation: Animation?) {
                super.onAnimationEnd(animation)
                view?.run {
                    getMainView(this).visibility = View.GONE
                }
            }
        }
    }

    private var targetsLoadedCallback: (() -> Unit)? = null

    abstract fun getBackgroundColorView(view: View): View
    abstract fun getVioletBackgroundView(view: View): View
    abstract fun getOrangeBackgroundView(view: View): View
    abstract fun getMainView(view: View): View
    abstract fun getDialogBackgroundView(view: View): DialogBackgroundView?
    abstract fun getImageBadgeView(view: View): ImageView?
    abstract fun getImageBadgeBackView(view: View): ImageView?
    abstract fun getKonfettiView(view: View): View?
    abstract fun getSubjectBackgroundImageView(view: View): ImageView?
    abstract fun getCongratulationsView(view: View): View?

    fun setTargetLoaded(targetsLoadedCallback: () -> Unit): BaseBottomSheetDialog {
        this.targetsLoadedCallback = targetsLoadedCallback
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_up_bottom)
        slideDown.startOffset = 200L
        slideDown.duration = 500L

        slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_bottom_up)
        slideUp.startOffset = 200L
        slideUp.duration = slideDown.duration

        badgeSlideUp = AnimationUtils.loadAnimation(context, R.anim.slide_bottom_up)
        badgeSlideUp.duration = 1000L

        badgeFadeIn = AlphaAnimation(0F, 1F)
        badgeFadeIn.duration = 1000L

        badgeAnimationSet = AnimationSet(true)
        badgeAnimationSet.startOffset = 400L
        badgeAnimationSet.addAnimation(badgeSlideUp)
        badgeAnimationSet.addAnimation(badgeFadeIn)

    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        theme = getThemeMap()[subjectThemeKey]!!

        view.post {
            if (showAppearanceAnimation) startAnimation(view)
        }

//        val nextBt = getCongratulationsView(view)?.findViewById<TextView>(R.id.tv_next)

    }

    private val themeMap = HashMap<String, Theme>()

    fun getThemeMap(): HashMap<String, Theme> {
        if (themeMap.isEmpty()) {

            themeMap[THEME_MATH] = Theme(
                    R.string.mathematics,
                    R.color.colorMaths,
                    R.color.colorMathsDark,
                    R.color.colorBiology,
                    overlayColor = R.color.colorMaths_40,
                    plainSubjectIcon = R.drawable.ic_maths_plain,
                    dashboardBtnBackground = R.drawable.bg_btn_maths,
                    videoPlayIcon = R.drawable.ic_btn_play_maths,
                    videoPauseIcon = R.drawable.ic_btn_pause_maths,
                    chapterDetailBg = BackgroundComponent(
                            R.drawable.bg_comp_sub_maths,
                            111,
                            171,
                            BackgroundComponent.POS_END,
                            BackgroundComponent.POS_TOP
                    ),
                    pathColor = R.color.path_color_maths,
                    mileStoneTextdrawable = R.drawable.bg_comp_journey_maths_text_milestone,
                    mileStonedrawable = R.drawable.bg_comp_journey_maths_milestone,
                    journeyBackgroundComponent = listOf(
                            BackgroundComponent(
                                    R.drawable.bg_comp_journey_maths_left_rock,
                                    319,
                                    746,
                                    BackgroundComponent.POS_START,
                                    0.95F,
                                    frSeparationY = 2F,
                                    frSeparationX = 0F,
                                    scaleType = ImageView.ScaleType.FIT_START
                            ),
                            BackgroundComponent(
                                    R.drawable.bg_comp_journey_maths_divider,
                                    127,
                                    107,
                                    BackgroundComponent.POS_END,
                                    1.45F,
                                    frSeparationY = 2F,
                                    frSeparationX = 0F,
                                    scaleType = ImageView.ScaleType.FIT_END
                            ),
                            BackgroundComponent(
                                    R.drawable.bg_comp_journey_maths_set_square,
                                    108,
                                    126,
                                    BackgroundComponent.POS_END,
                                    1.3F,
                                    frSeparationY = 2F,
                                    frSeparationX = 0F,
                                    scaleType = ImageView.ScaleType.FIT_END
                            ),
                            BackgroundComponent(
                                    R.drawable.bg_comp_journey_maths_small_rock,
                                    126,
                                    168,
                                    0.2F,
                                    1.1F,
                                    frSeparationY = 2F,
                                    frSeparationX = 0F,
                                    scaleType = ImageView.ScaleType.FIT_END
                            ),
                            BackgroundComponent(
                                    R.drawable.bg_comp_journey_maths_pi,
                                    107,
                                    127,
                                    BackgroundComponent.POS_START,
                                    0.9F,
                                    frSeparationY = 2F,
                                    frSeparationX = 0F,
                                    scaleType = ImageView.ScaleType.FIT_START
                            ),
                            BackgroundComponent(
                                    R.drawable.bg_comp_journey_maths_plus,
                                    42,
                                    64,
                                    BackgroundComponent.POS_START,
                                    0.75F,
                                    frSeparationY = 2F,
                                    frSeparationX = 0F,
                                    scaleType = ImageView.ScaleType.FIT_START
                            ),
                            BackgroundComponent(
                                    R.drawable.bg_comp_journey_maths_minus,
                                    48,
                                    49,
                                    0.08F,
                                    0.65F,
                                    frSeparationY = 2F,
                                    frSeparationX = 0F,
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                            ),
                            BackgroundComponent(
                                    R.drawable.bg_comp_journey_maths_right_rock,
                                    319,
                                    675,
                                    BackgroundComponent.POS_END,
                                    0.01F,
                                    frSeparationY = 2F,
                                    frSeparationX = 0F,
                                    scaleType = ImageView.ScaleType.FIT_END
                            )
                    ),
                    analysisBigBg = R.drawable.ic_chapter_bg_maths_big,
                    analysisSmallBg = R.drawable.ic_chapter_bg_maths_small,
                    practiceBeginnerDrawableResId = R.drawable.ic_practice_beginner_mathematics,
                    practiceProDrawableResId = R.drawable.ic_practice_pro_mathematics,
                    practiceMasterDrawableResId = R.drawable.ic_practice_master_mathematics,
                    filterFillIconResId = R.drawable.ic_maths_fill,
                    filterNoFillIconResId = R.drawable.ic_maths_no_fill,
                    expandedIconResId = R.drawable.ic_maths_plain,
                    expandedIconBackResId = R.drawable.ic_maths_bg,
                    collapsedIconResId = R.drawable.ic_maths_bg,
                    playPauseSelectorResId = R.drawable.play_pause_selector_maths,
                    lockIconResId = R.drawable.ic_lock_maths,
                    lockIconResIdSmall = R.drawable.ic_lock_maths_small,
                    testNowBtnResId = R.drawable.bg_btn_test_now_quest_complete,
                    nextLessonResId = R.drawable.bg_btn_next_lesson,
                    bigBannerBg = R.drawable.ic_live_classes_big_banner_maths,
                    listBannerBg = R.drawable.ic_live_class_list_banner_maths
            )

        }
        return themeMap
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val metrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun startAnimation(view: View) {
        if (!view.isAttachedToWindow)
            return
        val endRadius = hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
        val dimCircularAnimation = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ViewAnimationUtils.createCircularReveal(
                    getBackgroundColorView(view),
                    view.width / 2,
                    view.height,
                    0F,
                    endRadius
            )
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }
        dimCircularAnimation.duration = 800L
        getBackgroundColorView(view).visibility = View.VISIBLE
        dimCircularAnimation.start()

        val violetSlideAnimation =
                ScaleAnimation(1F, 1F, 0F, 1F, Animation.RELATIVE_TO_SELF, 1F, Animation.RELATIVE_TO_SELF, 1F)
        violetSlideAnimation.interpolator = OvershootInterpolator(3F)
        violetSlideAnimation.duration = 600L
        violetSlideAnimation.startOffset = 400L

        val orangeSlideAnimation =
                ScaleAnimation(1F, 1F, 0F, 1F, Animation.RELATIVE_TO_SELF, 1F, Animation.RELATIVE_TO_SELF, 1F)
        orangeSlideAnimation.interpolator = OvershootInterpolator(1.5F)
        orangeSlideAnimation.duration = 600L
        orangeSlideAnimation.startOffset = 500L

        val whiteSlideAnimation = AnimationUtils.loadAnimation(view.context, R.anim.slide_bottom_up)
        whiteSlideAnimation.interpolator = AccelerateDecelerateInterpolator()
        whiteSlideAnimation.duration = 600L
        whiteSlideAnimation.startOffset = 600L
        whiteSlideAnimation.setAnimationListener(object : AnimationListenerAdapter() {
            override fun onAnimationEnd(animation: Animation?) {
                animationEnded()
            }
        })

        getVioletBackgroundView(view).visibility = View.VISIBLE
        getOrangeBackgroundView(view).visibility = View.VISIBLE
        getMainView(view).visibility = View.VISIBLE

        getVioletBackgroundView(view).startAnimation(violetSlideAnimation)
        getOrangeBackgroundView(view).startAnimation(orangeSlideAnimation)
        getMainView(view).startAnimation(whiteSlideAnimation)
    }

    fun startBadgeLayoutAnimation() {
        postBadgeAnimation = true
        val view = view

        if (view != null) {
            if (isBadgeAnimationOnGoing)
                return

            slideDown.setAnimationListener(animationListener)

            val scaleAnimation = ScaleAnimation(
                    0F,
                    1F,
                    0F,
                    1F,
                    Animation.ABSOLUTE,
                    getSubjectBackgroundImageView(view)!!.width.toFloat(),
                    Animation.ABSOLUTE,
                    getSubjectBackgroundImageView(view)!!.height.toFloat()
            )
            scaleAnimation.fillAfter = true
            scaleAnimation.duration = 800
            getSubjectBackgroundImageView(view)!!.startAnimation(scaleAnimation)

            getCongratulationsView(view)!!.visibility = View.VISIBLE
            getCongratulationsView(view)!!.startAnimation(slideUp)

            val mainView = getMainView(view)
            if (mainView.isVisible)
                mainView.startAnimation(slideDown)

            getSubjectBackgroundImageView(view)!!.setImageResource(theme.analysisBigBg)
            getDialogBackgroundView(view)!!.startAnimation(theme.colorPrimary, 800L)
            getImageBadgeBackView(view)?.setColorFilter(ContextCompat.getColor(context!!, theme.colorPrimaryDark), android.graphics.PorterDuff.Mode.SRC_IN)

        }
    }

    private fun postBadgeAnimationRunnable() {
        badgeAnimationHandler.removeCallbacks(badgeAnimationRunnable)
        badgeAnimationHandler.postDelayed(badgeAnimationRunnable, 6000)
    }

    open fun animationEnded() {

    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (!isAdded) {
            try {
                super.show(manager, tag)
            } catch (e: Exception) {
                val ft = manager.beginTransaction()
                ft.add(this, tag)
                ft.commitAllowingStateLoss()
            }
        }
    }
}