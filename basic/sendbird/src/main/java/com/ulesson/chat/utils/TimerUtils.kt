package com.ulesson.chat.utils

import android.content.Context
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

class TimerUtils {

    private val calendar = GregorianCalendar(TimeZone.getTimeZone("GMT+1"))

    private var countdownTimer: CountDownTimer? = null
    private var chronometer: Chronometer? = null

    fun timer(seconds: Long, onTick: (Long) -> Unit, finished: () -> Unit) {

        countdownTimer?.cancel()

        countdownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(l: Long) {
                onTick(l / 1000)
            }

            override fun onFinish() {
                finished()
            }
        }.start()
    }

    fun countTime(context: Context, time: (Long) -> Unit) {
        chronometer = Chronometer(context)
        chronometer?.stop()
        chronometer?.base = SystemClock.elapsedRealtime()
        chronometer?.start()
        chronometer?.setOnChronometerTickListener {
            time(SystemClock.elapsedRealtime() - it.base)
        }
    }

    fun getTime(
        channelUrl: String,
        chatDuration: Int,
        isChannelCreate: Boolean,
        startTime : String,
        countDownTime: (Int) -> Unit,
        timeOut: () -> Unit
    ) {

        if (isChannelCreate && PreferenceUtils.getEndTime()?.get(channelUrl) == -1) {
            PreferenceUtils.setEndTime(hashMapOf(channelUrl to null))
        }

        //val startTimeFormat = ISO8601Utils.parse(startTime, ParsePosition(0))

        val startTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
            .parse(startTime)

        Log.i(TimerUtils::class.java.simpleName, startTimeFormat.toString())

        val calendar = Calendar.getInstance()

        val currentHour = calendar.get(Calendar.HOUR)
        val currentMinutes = calendar.get(Calendar.MINUTE)
        val currentSeconds = calendar.get(Calendar.SECOND)

        val currentTime = (currentHour * 3600) + (currentMinutes * 60) + currentSeconds

        val startHour = startTimeFormat?.hours ?: 0
        val startMinutes = startTimeFormat?.minutes ?: 0
        val startSeconds = startTimeFormat?.seconds ?: 0

        val endHour = startHour + ((startMinutes + chatDuration) / 60)
        val endMinutes = (startMinutes + chatDuration) % 60
        val endTime = (endHour * 3600) + (endMinutes * 60) + (startSeconds)


        Log.i(TimerUtils::class.java.simpleName,
            "EndHour: $endHour, EndMinutes: $endMinutes, EndTime: $endTime")

        when {
            PreferenceUtils.getEndTime()?.get(channelUrl) == null -> {

                Log.i(TimerUtils::class.java.simpleName,"FIRST")

                val endTimeMap = hashMapOf(channelUrl to endTime)
                PreferenceUtils.setEndTime(endTimeMap)

                countDownTime(endTime - currentTime)

            }
            PreferenceUtils.getEndTime()?.get(channelUrl) == -1 -> {
                Log.i(TimerUtils::class.java.simpleName,"SECOND")
                timeOut()
            }
            else -> {
                PreferenceUtils.getEndTime()?.get(channelUrl)?.let {
                    if (it in currentTime until endTime+1) {
                        Log.i(TimerUtils::class.java.simpleName,"FIRST4")
                        countDownTime(it - currentTime)
                    } else {
                        Log.i(TimerUtils::class.java.simpleName,"FIRST5")
                        PreferenceUtils.setEndTime(hashMapOf(channelUrl to -1))
                        timeOut()
                    }
                }

            }
        }

    }

    fun removeChannelData(channelUrl: String) {
        PreferenceUtils.removeTime(channelUrl)
    }

}