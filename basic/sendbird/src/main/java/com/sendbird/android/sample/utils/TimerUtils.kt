package com.sendbird.android.sample.utils

import android.os.CountDownTimer
import java.util.*

class TimerUtils {

    private val calendar = GregorianCalendar(TimeZone.getTimeZone("GMT+1"))

    fun timer(seconds : Long, onTick: (Long) -> Unit, finished: () -> Unit) {
        object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(l: Long) {
                onTick(l/1000)
            }

            override fun onFinish() {
                finished()
            }
        }.start()
    }

    fun getTime(channelUrl: String, countDownTime: (Int) -> Unit, timeOut : () -> Unit) {

        val countTime = 2

        val currentHour = calendar.get(Calendar.HOUR)
        val currentMinutes = calendar.get(Calendar.MINUTE)
        val currentSeconds = calendar.get(Calendar.SECOND)

        val currentTime = (currentHour * 3600) + (currentMinutes * 60) + currentSeconds

        if (PreferenceUtils.getEndTime()?.get(channelUrl) == null) {

            val endHour = currentHour + ((currentMinutes + countTime) / 60)
            val endMinutes = (currentMinutes + countTime) % 60
            val endTime = (endHour * 3600) + (endMinutes * 60) + (currentSeconds)

            val endTimeMap = hashMapOf(channelUrl to endTime)
            PreferenceUtils.setEndTime(endTimeMap)

            countDownTime(endTime - currentTime)

        } else {

            PreferenceUtils.getEndTime()?.get(channelUrl)?.let {

                if (it > currentTime) {
                    countDownTime(it - currentTime)
                }else{
                    timeOut()
                }
            }

        }

    }

}