package com.sendbird.android.sample.utils

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.*
import com.sendbird.android.sample.R
import kotlinx.android.synthetic.main.bottom_sheet_generic_dialog.*
import kotlinx.android.synthetic.main.bottom_sheet_generic_dialog.view.*
import java.util.concurrent.TimeUnit


class GenericDialog : BaseBottomSheetDialog() {
    private var imageResId: Int? = null

    private var titleResId: Int? = null
    private var title: String? = null

    private var messageResID: Int? = null
    private var message: String? = null

    private var positiveTextResId: Int? = null
    private var negativeTextResId: Int? = null

    private var upload: Boolean = false

    private var positiveBackgroundResId: Int? = null
    private var negativeBackgroundResId: Int? = null

    private var positiveBtnCallback: (() -> Unit)? = null
    private var negativeBtnCallback: (() -> Unit)? = null

    private var cameraCallback: (() -> Unit)? = null
    private var galleryCallback: (() -> Unit)? = null

    private var countDownTimeMillis: Long = 0L
    private var countDownTimerCallback: (() -> Unit)? = null

    private var dismissCallback: (() -> Unit)? = null

    private var timeUntilFinished: Long = 0
    private var themeColor: Int = 0
    private var countDownTimer: CountDownTimer? = null

    private var subscribeNowCallback: (() -> Unit)? = null

    var preventDismissCallback: Boolean = false

    private val SHOW_BADGE = "show_badge"

    val CLICK_TIME_INTERVAL = 600

    fun setImage(resId: Int): GenericDialog {
        this.imageResId = resId
        return this
    }

    fun setTitle(title: String): GenericDialog {
        this.title = title
        return this
    }

    fun setTitle(stringResId: Int): GenericDialog {
        this.titleResId = stringResId
        return this
    }

    fun setMessage(message: String): GenericDialog {
        this.message = message
        return this
    }

    fun setMessage(stringResId: Int): GenericDialog {
        this.messageResID = stringResId
        return this
    }

    fun setPositiveButton(
            stringResId: Int,
            backgroundResId: Int,
            callback: () -> Unit
    ): GenericDialog {
        this.positiveTextResId = stringResId
        this.positiveBackgroundResId = backgroundResId
        this.positiveBtnCallback = callback
        return this
    }

    fun setNegativeButton(
            stringResId: Int,
            backgroundResId: Int? = null,
            callback: () -> Unit
    ): GenericDialog {
        this.negativeTextResId = stringResId
        this.negativeBackgroundResId = backgroundResId
        this.negativeBtnCallback = callback
        return this
    }

    fun setUploadFile(
            upload: Boolean,
            cameraCallback: () -> Unit,
            galleryCallback: () -> Unit
    ): GenericDialog {
        this.upload = upload
        this.cameraCallback = cameraCallback
        this.galleryCallback = galleryCallback
        return this
    }

    fun setDismiss(dismissCallback: () -> Unit): GenericDialog {
        this.dismissCallback = dismissCallback
        return this
    }

    fun dismissCountdownTimer() {
        countDownTimerCallback = null
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun setCountdownTimer(countDownTimeMillis: Long, onCountDownFinish: () -> Unit): GenericDialog {
        this.countDownTimeMillis = countDownTimeMillis
        this.countDownTimerCallback = onCountDownFinish

        countDownTimer = object : CountDownTimer(countDownTimeMillis, 1000) {
            override fun onFinish() {
                if (!isVisible)
                    return
                timeUntilFinished = 0
                setTime(timeUntilFinished)
                countDownTimerCallback?.invoke()
                countDownTimer = null
                countDownTimerCallback = null
            }

            override fun onTick(millisUntilFinished: Long) {
                timeUntilFinished = millisUntilFinished
                setTime(timeUntilFinished)
            }
        }
        return this
    }

    fun setColorTheme(themeColor: Int): GenericDialog {
        this.themeColor = themeColor
        return this
    }

    private fun setTime(timeToSetInMillisecond: Long) {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeToSetInMillisecond)
        try {
            tv_timer.text = resources.getQuantityString(
                    R.plurals.seconds,
                    seconds.toInt(),
                    seconds
            )
        } catch (e: Exception) {
        }
    }

    fun startTimer() {
        countDownTimer?.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (titleResId != null)
            title = resources.getString(titleResId!!)

        if (messageResID != null)
            message = resources.getString(messageResID!!)

        if (message == null) {
            message = "message"
        }
        if (title == null) {
            title = "title"
        }
        if (positiveTextResId == null) {
            positiveTextResId = R.string.next
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        countDownTimer?.cancel()
        if (!preventDismissCallback)
            dismissCallback?.invoke()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? =
            inflater.inflate(R.layout.bottom_sheet_generic_dialog, container, false)

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (imageResId != null) {
            iv_image.setImageResource(imageResId!!)
        } else {
            iv_image.visibility = View.GONE

        }

        tv_title.text = title
        tv_message.text = message

        bt_positive.setText(positiveTextResId!!)
        bt_positive.setBackgroundResource(
                positiveBackgroundResId ?: R.drawable.bg_btn_onboarding_next
        )
        bt_positive.setClickListener { positiveBtnCallback?.invoke() }
        bt_camera.setClickListener { cameraCallback?.invoke() }
        bt_gallery.setClickListener { galleryCallback?.invoke() }

        if (upload) {
            bt_camera.isVisible = true
            bt_gallery.isVisible = true
            bt_positive.visibility = View.INVISIBLE
        } else {
            bt_camera.isVisible = false
            bt_gallery.isVisible = false
        }

        if (negativeTextResId != null) {
            bt_negative.setText(negativeTextResId!!)
            if (negativeBackgroundResId != null)
                bt_negative.setBackgroundResource(negativeBackgroundResId!!)
            bt_negative.setClickListener { negativeBtnCallback?.invoke() }
        } else if (countDownTimer != null) {
            tv_timer.isVisible = true

            if (themeColor != 0)
                tv_timer.setTextColor(themeColor)

            bt_negative.visibility = View.INVISIBLE
            setTime(timeUntilFinished)
        } else {
            bt_negative.isVisible = false
        }

    }

    override fun animationEnded() {
        if (isAdded && isVisible)
            if (countDownTimer != null) {
                tv_timer.visibility = View.VISIBLE
                setTime(countDownTimeMillis)
                startTimer()
            }
    }

    override fun getBackgroundColorView(view: View): View = view.v_background_color

    override fun getVioletBackgroundView(view: View): View = view.v_background_violet

    override fun getOrangeBackgroundView(view: View): View = view.v_background_orange

    override fun getMainView(view: View): View = view.cl_main

    override fun getDialogBackgroundView(view: View): DialogBackgroundView =
            view.dbv_background_view

    override fun getImageBadgeView(view: View): ImageView = view.iv_badge

    override fun getImageBadgeBackView(view: View): ImageView? = view.iv_badge_back

    override fun getKonfettiView(view: View) = view

    override fun getSubjectBackgroundImageView(view: View): ImageView = view.iv_subject_background

    override fun getCongratulationsView(view: View): View = view.cl_congratulations

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun View.setMargin(
            topMargin: Int? = null,
            endMargin: Int? = null,
            bottomMargin: Int? = null,
            startMargin: Int? = null
    ) {
        val viewLayoutParam = layoutParams
        if (viewLayoutParam is RelativeLayout.LayoutParams) {
            if (topMargin != null) viewLayoutParam.topMargin = topMargin
            if (startMargin != null) viewLayoutParam.marginStart = startMargin
            if (bottomMargin != null) viewLayoutParam.bottomMargin = bottomMargin
            if (endMargin != null) viewLayoutParam.marginEnd = endMargin
        } else if (viewLayoutParam is ConstraintLayout.LayoutParams) {
            if (topMargin != null) viewLayoutParam.topMargin = topMargin
            if (startMargin != null) viewLayoutParam.marginStart = startMargin
            if (bottomMargin != null) viewLayoutParam.bottomMargin = bottomMargin
            if (endMargin != null) viewLayoutParam.marginEnd = endMargin
        } else if (viewLayoutParam is LinearLayout.LayoutParams) {
            if (topMargin != null) viewLayoutParam.topMargin = topMargin
            if (startMargin != null) viewLayoutParam.marginStart = startMargin
            if (bottomMargin != null) viewLayoutParam.bottomMargin = bottomMargin
            if (endMargin != null) viewLayoutParam.marginEnd = endMargin
        } else if (viewLayoutParam is FrameLayout.LayoutParams) {
            if (topMargin != null) viewLayoutParam.topMargin = topMargin
            if (startMargin != null) viewLayoutParam.marginStart = startMargin
            if (bottomMargin != null) viewLayoutParam.bottomMargin = bottomMargin
            if (endMargin != null) viewLayoutParam.marginEnd = endMargin
        } else {
//            Timber.e("couldn't identify the parent")
        }
//        layoutParams = viewLayoutParam
    }

    var lastClickTime = 0L
    var notchHeight: Int = -1


    fun View.setClickListener(clickListener: View.OnClickListener) {
        setOnClickListener(object : View.OnClickListener {

            override fun onClick(v: View?) {
                val now = System.currentTimeMillis()
                if ((now - lastClickTime) < CLICK_TIME_INTERVAL) {
                    return
                }
                lastClickTime = now
                clickListener.onClick(v)
            }
        })
    }


    fun newInstance(subjectThemeKey: String) = GenericDialog().apply {
        this.subjectThemeKey = subjectThemeKey
    }

    fun newInstance() = GenericDialog()

    private fun getDummyInstance(subjectThemeKey: String): GenericDialog {
        val genericDialog = newInstance(subjectThemeKey)
        genericDialog
                .setTitle(R.string.on_boarding_fragment_first_main)
                .setMessage(R.string.on_boarding_fragment_third_sub)
                .setPositiveButton(R.string.next, R.drawable.bg_btn_onboarding_next) {
                    genericDialog.startBadgeLayoutAnimation()
                }
        return genericDialog
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

}