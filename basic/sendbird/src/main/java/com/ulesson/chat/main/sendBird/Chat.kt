package com.ulesson.chat.main.sendBird

import android.content.Intent
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
import com.sendbird.syncmanager.ChannelCollection
import com.sendbird.syncmanager.handler.ChannelCollectionHandler
import com.sendbird.syncmanager.handler.CompletionHandler
import com.ulesson.chat.groupchannel.GroupChannelListFragment
import com.ulesson.chat.groupchannel.GroupChatActivity
import com.ulesson.chat.groupchannel.GroupChatFragment
import com.ulesson.chat.groupchannel.GroupChatFragment.GROUP_CHAT_TAG
import com.ulesson.chat.main.allChat.GroupAllChatListFragment
import com.ulesson.chat.main.allChat.PagerFragment
import com.ulesson.chat.main.model.Question
import com.ulesson.chat.main.model.UserData
import com.ulesson.chat.main.model.UserGroup
import com.ulesson.chat.network.ChannelWorker
import com.ulesson.chat.utils.PreferenceUtils
import com.ulesson.chat.utils.StringUtils.Companion.pendingChatType
import com.ulesson.chat.utils.StringUtils.Companion.toMutableMap
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

        createGroupChat(
            hostUserData,
            otherUserData.id,
            questionMap,
            activity

        ) { groupChannel, error ->

            if (error == null) {

                if (groupChannel.data.pendingChatType()) {

                    channelUrl(groupChannel.url)

                    gotoChat(groupChannel.url, activity, toFinish, true, object : TutorActions {

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
                } else {

                    updateGroupChat(
                        groupChannel.url,
                        groupChannel.data,
                        questionMap,
                        activity
                    ) { updatedGroupChannel ->

                        updatedGroupChannel?.url?.let {
                            channelUrl(it)

                            gotoChat(
                                updatedGroupChannel.url,
                                activity,
                                true,
                                toFinish,
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
                        }
                    }
                }
            }

        }
    }

    fun gotoChat(
        groupChannelUrl: String,
        activity: FragmentActivity?,
        fromActivity: Boolean,
        createChat: Boolean,
        tutorActions: TutorActions,
        chatActions: ChatActions
    ) {

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
                activity.startActivity(intent)

            } else {

                val fragment = GroupChatFragment.newInstance(
                    groupChannelUrl,
                    createChat,
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

    private fun checkPendingChat(activity: FragmentActivity?, pendingChats: PendingChats) {

        var pendingChatCount: Int = 0

        val mChannelCollectionHandler = ChannelCollectionHandler { _, list, _ ->
            list.forEach { channel ->
                channel.refresh {}
            }
            list.forEach {
                if (it.data.pendingChatType()) {
                    pendingChatCount++
                }
            }
        }

        val mChannelCollection: ChannelCollection?
        val query = GroupChannel.createMyGroupChannelListQuery()
        mChannelCollection = ChannelCollection(query)
        mChannelCollection.setCollectionHandler(mChannelCollectionHandler)
        mChannelCollection.fetch(CompletionHandler {
            pendingChats.chatPending(pendingChatCount)
        })
    }

    private fun createGroupChat(
        hostUserData: UserData,
        otherId: String,
        questionMap: MutableMap<String, Any?>?,
        activity: FragmentActivity?,
        groupChannelCreateHandler: GroupChannelCreateHandler
    ) {

        val userIdList = listOf(hostUserData.id, otherId)

        val gsonString = Gson().toJson(questionMap)

        val params = GroupChannelParams()
            .setPublic(true)
            .setEphemeral(false)
            .setDistinct(false)
            .addUserIds(userIdList)
            .setData(gsonString)

        if (questionMap?.get("newVersion") == "true") {
            params.setName("${hostUserData.id} Chat")
        } else {
            params.setName("${hostUserData.id} and $otherId Chat")
        }

        checkPendingChat(activity, object : PendingChats {
            override fun chatPending(pendingCount: Int) {
                if (pendingCount == 0) {
                    GroupChannel.createChannel(params) { groupChannel, error ->

                        if (error == null) {
                            groupChannelCreateHandler.onResult(groupChannel, error)
                        } else {
                            Connect().refreshActivity({
                                createGroupChat(
                                    hostUserData,
                                    otherId,
                                    questionMap,
                                    activity,
                                    groupChannelCreateHandler
                                )
                            }, {

//                    val connectUserRequest =
//                        ConnectUserRequest(hostUserData.id, hostUserData.nickname, "")
//                    User().connectUser(connectUserRequest, "", {
//                        hostUserData.accessToken = it.access_token
//                        createGroupChat(
//                            hostUserData,
//                            otherId,
//                            questionMap,
//                            groupChannelCreateHandler
//                        )
//                    }, {
//
//                    }, {
//                        it?.let {}
//                    })
                            })
                        }

                    }
                } else {
                    activity?.let {
                        Toast.makeText(
                            it.baseContext,
                            "You currently have a pending chat, please wait for it to be accepted",
                            Toast.LENGTH_LONG
                        ).show()
                    }
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

        groupChannelParams.setData(Gson().toJson(map))

        val userGroup = UserGroup(channelUrl, groupChannelParams)
        oneTimeWork(activity, userGroup) {
            updatedGroupChannel(it)
        }
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

}