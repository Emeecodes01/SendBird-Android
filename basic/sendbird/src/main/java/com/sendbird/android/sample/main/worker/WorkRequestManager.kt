package com.sendbird.android.sample.main.worker

import android.content.Context
import androidx.work.*
import com.sendbird.android.sample.main.service.EndChatService
import java.util.concurrent.TimeUnit

object WorkRequestManager {

    fun enQueueWork(context: Context, questionId: Int, channelUrl: String, initialDelay: Long) {
        val workRequest = OneTimeWorkRequestBuilder<EndChatSessionWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("$questionId")
            .setInputData(
                workDataOf(
                EndChatService.END_CHAT_SERVICE_QUESTION_ID to  questionId,
                EndChatService.END_CHAT_SERVICE_CHANNEL_URL to channelUrl)
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                1,
                TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork("$questionId", ExistingWorkPolicy.KEEP, workRequest)
    }
}