package com.sendbird.android.sample.main.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.sendbird.android.sample.main.reciever.EndChatReceiver
import com.sendbird.android.sample.main.service.EndChatService
import com.sendbird.android.sample.main.worker.WorkRequestManager

class ScheduleManager(private val sessionStoreManager: SessionStoreManager) {

    private val context: Context by lazy { sessionStoreManager.context }

    var questionId: Int? = null
    var channelUrl: String? = null

    companion object {
        fun getInstance(sessionStoreManager: SessionStoreManager):
                ScheduleManager {
            return ScheduleManager(sessionStoreManager)
        }
    }


    fun scheduleEndChat(futureTime: Long) {
        WorkRequestManager.enQueueWork(context, questionId ?: -1, channelUrl ?: "", futureTime)
    }

}