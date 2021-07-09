package com.sendbird.android.sample.main.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.gson.Gson
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import com.sendbird.android.sample.main.worker.EndChatSessionWorker
import com.sendbird.android.sample.main.worker.WorkRequestManager
import com.sendbird.android.sample.network.NetworkRequest
import com.sendbird.android.sample.utils.isBooleanString
import com.sendbird.android.sample.utils.toMutableMap
import java.text.SimpleDateFormat
import java.util.*

class EndChatService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val questionId = intent?.getIntExtra(END_CHAT_SERVICE_QUESTION_ID, 0) ?: 0
        val channelUrl = intent?.getStringExtra(END_CHAT_SERVICE_CHANNEL_URL) ?: ""

        //yyyy-MM-dd HH:mm:ss
        val dateMillis = System.currentTimeMillis()
        val date = Date(dateMillis)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val dateString = simpleDateFormat.format(date)


        GroupChannel.getChannel(channelUrl) { channel, sendBirdException ->

            if (sendBirdException != null) {
                sendBirdException.printStackTrace()
                return@getChannel
            }


            NetworkRequest().endChat(questionId, dateString, channel.url,
                success = {
                    //progressBar3?.visibility = View.GONE
                    val questionDetailsMap = channel.data.toMutableMap()
                    val temp = questionDetailsMap["active"] ?: ""

                    //questionDetailsMap["active"] = false
                    val strData = if (temp.isBooleanString()) {
                        //v1
                        questionDetailsMap["active"] = false.toString()
                        questionDetailsMap.toString()
                    } else {
                        //v2
                        questionDetailsMap["active"] = "past"
                        val gson = Gson()
                        gson.toJson(questionDetailsMap)
                    }
                    //val strData = questionDetailsMap.toString()

                    channel.updateChannel(channel.name, channel.coverUrl, strData) { _, e ->
                        if (e != null) {
                            e.printStackTrace()
                            return@updateChannel
                        }
                    }
                },

                error = {
                    Log.e(EndChatService::class.java.simpleName, it)

                    WorkRequestManager.enQueueWork(applicationContext, questionId, channel.url, 0)
                })
        }

        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        //Toast.makeText(applicationContext, "--- END CHAT SERVICE RUNNING ----", Toast.LENGTH_SHORT).show()
    }


    companion object {
        const val END_CHAT_SERVICE_QUESTION_ID = "question_id"
        const val END_CHAT_SERVICE_CHANNEL_URL = "channel_url"
    }

}