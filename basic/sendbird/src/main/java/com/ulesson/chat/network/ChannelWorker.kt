package com.ulesson.chat.network

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBirdException
import com.ulesson.chat.main.model.UserGroup

class ChannelWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {

        private var channelCallBack: ((GroupChannel?) -> Unit?)? = null

        fun getChannel(channelCallBack: (GroupChannel?) -> Unit) {
            this.channelCallBack = channelCallBack
        }
    }

    override fun doWork(): Result {

        val userGroupString = inputData.getString("userGroup")
        val (channelUrl, groupChannelParams) = Gson().fromJson(
            userGroupString,
            UserGroup::class.java
        )

        GroupChannel.getChannel(channelUrl) { groupChannel: GroupChannel, e: SendBirdException? ->
            groupChannel.updateChannel(groupChannelParams) { updatedGroupChannel: GroupChannel?, error: SendBirdException? ->
                channelCallBack?.let {
                    it(updatedGroupChannel)
                }
            }
        }

        return Result.success()

    }

}