package com.ulesson.chat.main.sendBird

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.gson.Gson
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannel.GroupChannelCreateHandler
import com.sendbird.android.GroupChannelParams
import com.sendbird.android.Member
import com.sendbird.android.SendBird
import com.sendbird.android.SendBirdException
import com.sendbird.syncmanager.ChannelCollection
import com.sendbird.syncmanager.SendBirdSyncManager
import com.sendbird.syncmanager.handler.ChannelCollectionHandler
import com.sendbird.syncmanager.handler.CompletionHandler
import com.ulesson.chat.groupchannel.GroupChannelListFragment
import com.ulesson.chat.groupchannel.GroupChatActivity
import com.ulesson.chat.groupchannel.GroupChatFragment
import com.ulesson.chat.groupchannel.GroupChatFragment.GROUP_CHAT_TAG
import com.ulesson.chat.main.ConnectionManager
import com.ulesson.chat.main.SyncManagerUtils
import com.ulesson.chat.main.allChat.GroupAllChatListFragment
import com.ulesson.chat.main.allChat.PagerFragment
import com.ulesson.chat.main.model.Question
import com.ulesson.chat.main.model.UserData
import com.ulesson.chat.main.model.UserGroup
import com.ulesson.chat.network.ChannelWorker
import com.ulesson.chat.utils.PreferenceUtils
import com.ulesson.chat.utils.StringUtils.Companion.activeChatType
import com.ulesson.chat.utils.StringUtils.Companion.pendingChatType
import com.ulesson.chat.utils.StringUtils.Companion.toMutableMap
import com.ulesson.chat.utils.TimerUtils
import java.io.File
import java.util.*

class Chat {

    /**
     * Create chat between 2 users, each user has a UserData object which contains their userid, nickname and access token
     */

    fun createChat(
        activity: FragmentActivity?,
        hostUserData: UserData,
        otherUserData: UserData,
        toFinish: Boolean,
        questionMap: HashMap<String, Any?>,
        channelUrl: (String) -> Unit,
        chatActions: ChatActions,
        tutorActions: TutorActions
    ) {

        if (questionMap["newVersion"] == "true") {
            questionMap["active"] = "pending"
        } else {
            questionMap["active"] = "true"
        }

        questionMap["inSession"] = "false"

        createGroupChat(
            hostUserData,
            otherUserData.id,
            questionMap,
            activity,
            chatActions
        ) { groupChannel, error ->

            if (error == null) {

                channelUrl(groupChannel.url)

                gotoChat(groupChannel.url, activity, "tutorDefault", toFinish, true, object : TutorActions {

                    override fun showTutorProfile(members: List<Member>) {
                        tutorActions.showTutorProfile(members)
                    }

                    override fun showTutorRating(questionMap: MutableMap<String, Any?>) {
                        tutorActions.showTutorRating(questionMap)
                    }
                }, object : ChatActions {
                    override fun chatReceived() {
                        chatActions.chatReceived()
                    }

                    override fun showDummyChat(question: Question) {
                        chatActions.showDummyChat(question)
                    }

                    override fun getPendingQuestions() {
                        chatActions.getPendingQuestions()
                    }

                })

            }

        }
    }

    fun gotoChat(
        groupChannelUrl: String,
        activity: FragmentActivity?,
        customType: String,
        fromActivity: Boolean,
        createChat: Boolean,
        tutorActions: TutorActions,
        chatActions: ChatActions
    ) {

        SyncManagerUtils.setup(
            PreferenceUtils.getContext(),
            PreferenceUtils.getUserId()
        ) {
            SendBirdSyncManager.getInstance().resumeSync()
            activity?.let {

                if (fromActivity) {

                    GroupChatActivity.setActions(object : TutorActions {

                        override fun showTutorProfile(members: List<Member>) {
                            tutorActions.showTutorProfile(members)
                        }

                        override fun showTutorRating(questionMap: MutableMap<String, Any?>) {
                            tutorActions.showTutorRating(questionMap)
                        }
                    }, object : ChatActions {
                        override fun chatReceived() {
                            chatActions.chatReceived()
                        }

                        override fun showDummyChat(question: Question) {
                            chatActions.showDummyChat(question)
                        }

                        override fun getPendingQuestions() {
                            chatActions.getPendingQuestions()
                        }

                    }, createChat)

                    val intent = Intent(activity.baseContext, GroupChatActivity::class.java)
                    intent.putExtra("channelUrl", groupChannelUrl)
                    intent.putExtra("customType", customType)
                    activity.startActivity(intent)

                } else {

                    val fragment = GroupChatFragment.newInstance(
                        groupChannelUrl,
                        createChat,
                        customType,
                        fromActivity,
                        object : TutorActions {

                            override fun showTutorProfile(members: List<Member>) {
                                tutorActions.showTutorProfile(members)
                            }

                            override fun showTutorRating(questionMap: MutableMap<String, Any?>) {
                                tutorActions.showTutorRating(questionMap)
                            }
                        },
                        object : ChatActions {
                            override fun chatReceived() {
                                chatActions.chatReceived()
                            }

                            override fun showDummyChat(question: Question) {
                                chatActions.showDummyChat(question)
                            }

                            override fun getPendingQuestions() {
                                chatActions.getPendingQuestions()
                            }

                        })

                    if (!it.supportFragmentManager.isDestroyed && !fragment.isAdded) {
                        it.supportFragmentManager.beginTransaction()
                            .add(android.R.id.content, fragment, GROUP_CHAT_TAG)
                            .commitAllowingStateLoss()
                    }

                }

            }
        }

    }

    fun startCountDownTimer(channelUrl: String) {

        GroupChannel.getChannel(channelUrl) { groupChannel: GroupChannel, e: SendBirdException? ->

            if (e == null){
                groupChannel.refresh {

                    if (it == null){
                        val questionMap: MutableMap<String, Any?> = groupChannel.data.toMutableMap()

                        TimerUtils().getTime(
                            channelUrl,
                            getChatDuration(questionMap),
                            true,
                            getStartTime(questionMap),
                            {

                            }) { }
                    }

                }
            }

        }

    }

    private fun checkPendingChat(chatActions: ChatActions, pendingChats: PendingChats) {

        var pendingChatCount = 0
        var activeChatCount = 0

        val mChannelCollectionHandler = ChannelCollectionHandler { _, list, _ ->
            list.forEach {
                if (it.data.pendingChatType()) {
                    pendingChatCount++
                }
                if (it.data.activeChatType()) {
                    activeChatCount++
                }
            }
        }

        val mChannelCollection: ChannelCollection?
        val query = GroupChannel.createMyGroupChannelListQuery()
        mChannelCollection = ChannelCollection(query)
        mChannelCollection.setCollectionHandler(mChannelCollectionHandler)
        mChannelCollection.fetch(CompletionHandler {
            pendingChats.chatPending(pendingChatCount, activeChatCount, chatActions)
        })
    }

    private fun createGroupChat(
        hostUserData: UserData,
        otherId: String,
        questionMap: MutableMap<String, Any?>?,
        activity: FragmentActivity?,
        chatActions: ChatActions,
        groupChannelCreateHandler: GroupChannelCreateHandler
    ) {

        syncAllChat(activity)

        val userIdList = listOf(hostUserData.id, otherId)
        val operatorId = listOf(hostUserData.id)

        val gsonString = Gson().toJson(questionMap)

        val params = GroupChannelParams()
            .setPublic(true)
            .setEphemeral(false)
            .setDistinct(false)
            .addUserIds(userIdList)
            .setOperatorUserIds(operatorId)
            .setData(gsonString)

        if (questionMap?.get("newVersion") == "true") {
            params.setName("${hostUserData.id} Chat")
        } else {
            params.setName("${hostUserData.id} and $otherId Chat")
        }

        checkPendingChat(chatActions, object : PendingChats {
            override fun chatPending(
                pendingCount: Int,
                activeCount: Int,
                chatActions: ChatActions
            ) {
                if (pendingCount == 0 && activeCount == 0) {
                    GroupChannel.createChannel(params) { groupChannel, error ->

                        if (error == null) {
                            groupChannelCreateHandler.onResult(groupChannel, error)
                        } else {
                            Connect().refreshActivity({
                                activity?.let {
                                    Toast.makeText(
                                        it.baseContext,
                                        "Please check your internet and retry your question",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }, {
                            })
                        }

                    }
                } else if (pendingCount > 0) {

                    activity?.let {
                        Toast.makeText(
                            it.baseContext,
                            "You have a pending question, please wait for it to be accepted by a Tutor",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    chatActions.chatReceived()

                } else if (activeCount > 0) {

                    activity?.let {
                        Toast.makeText(
                            it.baseContext,
                            "You have an active chat with a Tutor, please finish it before asking another question",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    chatActions.chatReceived()
                }
            }

        })

    }

    fun updateGroupChat(
        channelUrl: String,
        channelData: String,
        updateMap: MutableMap<String, Any?>,
        activity: FragmentActivity?,
        updatedGroupChannel: (GroupChannel?) -> Unit
    ) {

        val groupChannelParams = GroupChannelParams()
        val map = channelData.toMutableMap() + updateMap

        if (map["newVersion"] == null) {
            groupChannelParams.setData(map.toString())
        } else {
            groupChannelParams.setData(Gson().toJson(map))
        }

        val userGroup = UserGroup(channelUrl, groupChannelParams)
        oneTimeWork(activity, userGroup) {
            updatedGroupChannel(it)
        }
    }

    fun deleteGroupChannel(channelUrl: String, deleted: () -> Unit, error: () -> Unit) {
        GroupChannel.getChannel(channelUrl) { groupChannel, error ->
            if (error == null){
                groupChannel?.delete {
                    if (it == null) {
                        deleted()
                    } else {
                        error()
                    }
                }
            }

        }
    }

    private fun syncAllChat(activity: FragmentActivity?) {

        SyncManagerUtils.setup(
            PreferenceUtils.getContext(),
            PreferenceUtils.getUserId()
        ) {
            SendBirdSyncManager.getInstance().resumeSync()

            val calendar = GregorianCalendar(TimeZone.getTimeZone("GMT+1"))

            val currentHour = calendar.get(Calendar.HOUR)
            val currentMinutes = calendar.get(Calendar.MINUTE)
            val currentSeconds = calendar.get(Calendar.SECOND)

            val currentTime = (currentHour * 3600) + (currentMinutes * 60) + currentSeconds

            val timeMap = PreferenceUtils.getEndTime()

            val mChannelCollectionHandler = ChannelCollectionHandler { _, list, _ ->

                list.forEach { channel ->

                    val chatDuration = getChatDuration(channel.data.toMutableMap())

                    val endHour = currentHour + ((currentMinutes + chatDuration) / 60)
                    val endMinutes = (currentMinutes + chatDuration) % 60
                    val endTime = (endHour * 3600) + (endMinutes * 60) + (currentSeconds)

                    timeMap?.get(channel.url)?.let {

                        if (it !in currentTime until endTime) {

                            val activeMap = mutableMapOf<String, Any?>()

                            activeMap["active"] = "past"
                            activeMap["inSession"] = "false"

                            updateGroupChat(channel.url, channel.data, activeMap, activity) {
                                it?.url?.let {
                                    TimerUtils().removeChannelData(it)
                                }
                            }
                        }

                    }

                }

            }

            val mChannelCollection: ChannelCollection?
            val query = GroupChannel.createMyGroupChannelListQuery()
            mChannelCollection = ChannelCollection(query)
            mChannelCollection.setCollectionHandler(mChannelCollectionHandler)
            mChannelCollection.fetch(CompletionHandler {
            })

        }
    }

    private fun getChatDuration(questionMap: MutableMap<String, Any?>): Int {
        var chatDuration = 0
        try {
            val chatDurationString = questionMap["chatDuration"] as String?
            if (chatDurationString != null) {
                chatDuration = chatDurationString.toInt()
            }
        } catch (ignore: Exception) {
        }
        return chatDuration
    }

    private fun getStartTime(questionMap: MutableMap<String, Any?>): String {
        var startTime = "0"
        try {
            val startTimeString = questionMap["startTime"] as String?
            if (startTimeString != null) {
                startTime = startTimeString
            }
        } catch (ignore: Exception) {
        }
        return startTime
    }

    fun copyFile(src: File, dest: File) {
        src.copyTo(dest, true)
    }

    private fun oneTimeWork(
        activity: FragmentActivity?,
        userGroup: UserGroup,
        updatedGroupChannel: (GroupChannel?) -> Unit
    ) {
        val userGroupString = Gson().toJson(userGroup)
        val data = Data.Builder().putString("userGroup", userGroupString).build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(ChannelWorker::class.java)
            .setInputData(data)
            .build()

        activity?.let {
            WorkManager.getInstance(it).enqueue(oneTimeWorkRequest)
            ChannelWorker.getChannel { channel ->
                if (channel == null) {
                    val anotherRequest = OneTimeWorkRequest.Builder(ChannelWorker::class.java)
                        .setInputData(data)
                        .build()
                    WorkManager.getInstance(it).enqueue(anotherRequest)
                } else {
                    updatedGroupChannel(channel)
                }
            }

        }
    }

    /**
     * Show all the chat list of a user, pass in the data of the user you want to show
     */

    fun showAllChat(
        activity: FragmentActivity?,
        layoutId: Int,
        hostUserData: UserData,
        tutorActions: TutorActions,
        chatActions: ChatActions,
        newVersion: Boolean
    ) {

        syncAllChat(activity)

        val fragment: Fragment = PagerFragment.newInstance(tutorActions, chatActions, newVersion)

        if (activity != null && !activity.supportFragmentManager.isDestroyed) {
            val manager: FragmentManager = activity.supportFragmentManager
            manager.beginTransaction()
                .add(layoutId, fragment)
                .addToBackStack(fragment.tag)
                .commit()

        }
    }

    fun showChatList(
        activity: AppCompatActivity?,
        layoutId: Int,
        hostUserData: UserData,
        tutorActions: TutorActions,
        chatActions: ChatActions,
        newVersion: Boolean
    ) {

        syncAllChat(activity)

        val fragment: Fragment =
            GroupChannelListFragment.newInstance(hostUserData, object : TutorActions {
                override fun showTutorProfile(members: List<Member>) {
                    tutorActions.showTutorProfile(members)
                }

                override fun showTutorRating(questionMap: MutableMap<String, Any?>) {
                    tutorActions.showTutorRating(questionMap)
                }
            }, object : ChatActions {
                override fun chatReceived() {
                    chatActions.chatReceived()
                }

                override fun showDummyChat(question: Question) {
                    chatActions.showDummyChat(question)
                }

                override fun getPendingQuestions() {
                    chatActions.getPendingQuestions()
                }

            }, newVersion)

        if (activity != null && !activity.supportFragmentManager.isDestroyed) {
            val manager: FragmentManager = activity.supportFragmentManager

            manager.beginTransaction()
                .add(layoutId, fragment)
                .commitAllowingStateLoss()
        }

    }

    fun setPendingQuestions(pendingQuestions: String) {
        PreferenceUtils.setPendingQuestions(pendingQuestions)
        GroupAllChatListFragment().updateQuestion()
    }

    fun logOut() {
        ConnectionManager.logout {
            //do nothing
            SendBird.unregisterPushTokenAllForCurrentUser {}
            Log.i(Chat::class.java.simpleName, "SENDBIRD LOGGED...")
        }
    }

}