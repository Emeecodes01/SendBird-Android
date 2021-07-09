package com.sendbird.android.sample.main.worker

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.sendbird.android.GroupChannel
import com.sendbird.android.sample.main.service.EndChatService
import com.sendbird.android.sample.network.NetworkRequest
import com.sendbird.android.sample.utils.isBooleanString
import com.sendbird.android.sample.utils.toMutableMap
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.Exception

class EndChatSessionWorker(
    context: Context,
    private val workerParameters: WorkerParameters
) : ListenableWorker(context, workerParameters) {


    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer ->

            val questionId = workerParameters.inputData.getInt(
                EndChatService.END_CHAT_SERVICE_QUESTION_ID,
                0
            )

            val channelUrl =
                workerParameters.inputData.getString(EndChatService.END_CHAT_SERVICE_CHANNEL_URL)
                    ?: ""

            //yyyy-MM-dd HH:mm:ss
            val dateMillis = System.currentTimeMillis()
            val date = Date(dateMillis)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val dateString = simpleDateFormat.format(date)


            GroupChannel.getChannel(channelUrl) { channel, err ->
                if (err != null) {
                    err.printStackTrace()
                    completer.setException(err)
                    return@getChannel
                }

                //end chat API
                NetworkRequest().endChat(questionId, dateString, channelUrl,
                    success = {
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
//                        val strData = if (temp is String) {
//                            questionDetailsMap.toString()
//                        } else {
//                            val gson = Gson()
//                            gson.toJson(questionDetailsMap)
//                        }

                        channel.updateChannel(channel.name, channel.coverUrl, strData) { _, e ->
                            if (e != null) {
                                e.printStackTrace()
                                return@updateChannel
                            }
                        }

                        completer.set(Result.success())
                    },


                    error = { errStr ->
                        Log.e(EndChatService::class.java.simpleName, errStr)
                        completer.set(Result.retry())
                    })

            }

        }
    }


    /* override fun doWork(): Result {
         val questionId = workerParameters.inputData.getInt(
             EndChatService.END_CHAT_SERVICE_QUESTION_ID,
             0
         )
         val channelUrl =
             workerParameters.inputData.getString(EndChatService.END_CHAT_SERVICE_CHANNEL_URL) ?: ""


         //yyyy-MM-dd HH:mm:ss
         val dateMillis = System.currentTimeMillis()
         val date = Date(dateMillis)
         val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

         val dateString = simpleDateFormat.format(date)

         return try {
             GroupChannel.getChannel("channelUrl") { channel, err ->
                 if (err != null) {
                     err.printStackTrace()
                     return@getChannel
                 }

                 //end chat API
                 val response = NetworkRequest().endChat(questionId, dateString, channelUrl)

                 if (response.isSuccessful) {
                     val questionDetailsMap = channel.data.toMutableMap()

                     questionDetailsMap["active"] = false
                     val strData = questionDetailsMap.toString()

                     channel.updateChannel(channel.name, channel.coverUrl, strData) { _, e ->
                         if (e != null) {
                             e.printStackTrace()
                             return@updateChannel
                         }
                     }

                     Result.success()
                 } else {
                     Log.e(EndChatService::class.java.simpleName, response.errorBody()?.string())
                     Result.retry()
                 }

             }

             Result.success()

         } catch (e: Exception) {
             e.printStackTrace()
             Result.retry()
         }
     }*/

}