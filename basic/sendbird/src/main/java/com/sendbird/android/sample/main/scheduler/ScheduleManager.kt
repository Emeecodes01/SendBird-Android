package com.sendbird.android.sample.main.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.sendbird.android.sample.main.reciever.EndChatReceiver
import com.sendbird.android.sample.main.service.EndChatService

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
        if(!sessionStoreManager.isSessionAvaliable(questionId)) {
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, EndChatReceiver::class.java).also {
                it.putExtra(EndChatService.END_CHAT_SERVICE_QUESTION_ID, questionId)
                it.putExtra(EndChatService.END_CHAT_SERVICE_CHANNEL_URL, channelUrl)
            }.let { intent ->
                PendingIntent.getBroadcast(context, 0, intent, 0)
            }

            alarmMgr.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + futureTime,
                alarmIntent
            )
            sessionStoreManager.saveSessionId(questionId)
        }
    }

}