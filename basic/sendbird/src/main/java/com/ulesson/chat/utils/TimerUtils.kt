package com.ulesson.chat.utils

import android.content.Context
import android.os.CountDownTimer
import android.os.SystemClock
import android.widget.Chronometer
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
        countDownTime: (Int) -> Unit,
        timeOut: () -> Unit
    ) {

        if (isChannelCreate && PreferenceUtils.getEndTime()?.get(channelUrl) == -1) {
            PreferenceUtils.setEndTime(hashMapOf(channelUrl to null))
        }

        val currentHour = calendar.get(Calendar.HOUR)
        val currentMinutes = calendar.get(Calendar.MINUTE)
        val currentSeconds = calendar.get(Calendar.SECOND)

        val currentTime = (currentHour * 3600) + (currentMinutes * 60) + currentSeconds

        val endHour = currentHour + ((currentMinutes + chatDuration) / 60)
        val endMinutes = (currentMinutes + chatDuration) % 60
        val endTime = (endHour * 3600) + (endMinutes * 60) + (currentSeconds)

        when {
            PreferenceUtils.getEndTime()?.get(channelUrl) == null -> {

                val endTimeMap = hashMapOf(channelUrl to endTime)
                PreferenceUtils.setEndTime(endTimeMap)

                countDownTime(endTime - currentTime)

            }
            PreferenceUtils.getEndTime()?.get(channelUrl) == -1 -> {
                timeOut()
            }
            else -> {

                PreferenceUtils.getEndTime()?.get(channelUrl)?.let {

                    if (it in (currentTime + 1) until endTime) {
                        countDownTime(it - currentTime)
                    } else {
                        PreferenceUtils.setEndTime(hashMapOf(channelUrl to -1))
                        timeOut()
                    }
                }

            }
        }

    }

    fun updateChannelData(channelUrl: String) {
        PreferenceUtils.setEndTime(hashMapOf(channelUrl to null))
    }

}