package com.sendbird.android.sample.main.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sendbird.android.sample.main.service.EndChatService

class EndChatReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val callingIntent = Intent(context, EndChatService::class.java).apply {
            putExtra(
                EndChatService.END_CHAT_SERVICE_CHANNEL_URL,
                intent?.getStringExtra(EndChatService.END_CHAT_SERVICE_CHANNEL_URL)
            )
            putExtra(
                EndChatService.END_CHAT_SERVICE_QUESTION_ID,
                intent?.getIntExtra(EndChatService.END_CHAT_SERVICE_QUESTION_ID, 0)
            )
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(callingIntent)
        } else {
            context?.startService(callingIntent)
        }

    }
}