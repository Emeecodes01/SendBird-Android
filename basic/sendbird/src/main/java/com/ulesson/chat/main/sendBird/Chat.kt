package com.ulesson.chat.main.sendBird

import android.content.Intent
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
import com.ulesson.chat.groupchannel.GroupChannelListFragment
import com.ulesson.chat.groupchannel.GroupChatActivity
import com.ulesson.chat.groupchannel.GroupChatFragment
import com.ulesson.chat.groupchannel.GroupChatFragment.GROUP_CHAT_TAG
import com.ulesson.chat.main.allChat.PagerFragment
import com.ulesson.chat.main.model.UserData
import com.ulesson.chat.main.model.UserGroup
import com.ulesson.chat.network.ChannelWorker
import com.ulesson.chat.network.userModel.ConnectUserRequest
import com.ulesson.chat.utils.StringUtils.Companion.isActive
import com.ulesson.chat.utils.StringUtils.Companion.toMutableMap
import java.util.*

class Chat {

    /**
     * Create chat between 2 users, each user has a UserData object which contains their userid, nickname and access token
     */

    fun createChat(activity: FragmentActivity?, hostUserData: UserData, otherUserData: UserData, toFinish: Boolean, questionMap: HashMap<String, Any?>, channelUrl: (String) -> Unit, chatCreated: () -> Unit, tutorActions: TutorActions) {

        questionMap["active"] = "true"

        createGroupChat(hostUserData, otherUserData.id, questionMap) { groupChannel, error ->

            if (error == null) {

                if (groupChannel.data.isActive()) {

                    channelUrl(groupChannel.url)

                    gotoChat(groupChannel, activity, toFinish, object : TutorActions {

                        override fun showTutorProfile(members: List<Member>) {
                            tutorActions.showTutorProfile(members)
                        }

                        override fun showTutorRating(questionMap: MutableMap<String, Any?>) {
                            tutorActions.showTutorRating(questionMap)
                        }
                    }, object : ChatActions {
                        override fun chatReceived() {
                            chatCreated()
                        }

                    })
                } else {

                    updateGroupChat(groupChannel.url, groupChannel.data, questionMap, activity) { updatedGroupChannel ->

                        updatedGroupChannel?.url?.let {
                            channelUrl(it)

                            gotoChat(updatedGroupChannel, activity, toFinish, object : TutorActions {
                                override fun showTutorProfile(members: List<Member>) {
                                    tutorActions.showTutorProfile(members)
                                }

                                override fun showTutorRating(questionMap: MutableMap<String, Any?>) {
                                    tutorActions.showTutorRating(questionMap)
                                }
                            }, object : ChatActions {
                                override fun chatReceived() {
                                    chatCreated()
                                }

                            })
                        }
                    }
                }
            }

        }
    }

    private fun gotoChat(groupChannel: GroupChannel, activity: FragmentActivity?, fromActivity: Boolean, tutorActions: TutorActions, chatActions: ChatActions) {

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

                })

                val intent = Intent(activity.baseContext, GroupChatActivity::class.java)
                intent.putExtra("channelUrl", groupChannel.url)
                activity.startActivity(intent)

            } else {

                val fragment = GroupChatFragment.newInstance(groupChannel.url, true, fromActivity, object : TutorActions {

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

                })

                if (!it.supportFragmentManager.isDestroyed && !fragment.isAdded) {
                    it.supportFragmentManager.beginTransaction().add(android.R.id.content, fragment, GROUP_CHAT_TAG)
                            .commitAllowingStateLoss()
                }

            }

        }

    }

    private fun createGroupChat(hostUserData: UserData, otherId: String, questionMap: MutableMap<String, Any?>?, groupChannelCreateHandler: GroupChannelCreateHandler) {

        val userIdList = listOf(hostUserData.id, otherId)

        GroupChannel.createChannelWithUserIds(userIdList, false, "${hostUserData.id} and $otherId Chat", "", questionMap.toString(), "") { groupChannel, error ->

            if (error == null) {
                groupChannelCreateHandler.onResult(groupChannel, error)
                return@createChannelWithUserIds
            } else {
                Connect().refreshActivity({

                    createGroupChat(hostUserData, otherId, questionMap, groupChannelCreateHandler)
                }, {
                    val connectUserRequest = ConnectUserRequest(hostUserData.id, hostUserData.nickname, "")
                    User().connectUser(connectUserRequest, "", {
                        hostUserData.accessToken = it.access_token
                        createGroupChat(hostUserData, otherId, questionMap, groupChannelCreateHandler)
                    }, {

                    }, {
                        it?.let {}
                    })
                })
            }

        }

    }

    fun updateGroupChat(channelUrl: String, channelData: String, updateMap: MutableMap<String, Any?>, activity: FragmentActivity?, updatedGroupChannel: (GroupChannel?) -> Unit) {

        val groupChannelParams = GroupChannelParams()
        val map = channelData.toMutableMap() + updateMap

        groupChannelParams.setData(map.toString())

        val userGroup = UserGroup(channelUrl, groupChannelParams)
        oneTimeWork(activity, userGroup) {
            updatedGroupChannel(it)
        }
    }

    private fun oneTimeWork(activity: FragmentActivity?, userGroup: UserGroup, updatedGroupChannel: (GroupChannel?) -> Unit) {
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

    fun showAllChat(activity: FragmentActivity?, layoutId: Int, hostUserData: UserData) {

        val fragment: Fragment = PagerFragment()

        if (activity != null && !activity.supportFragmentManager.isDestroyed) {
            val manager: FragmentManager = activity.supportFragmentManager
            manager.beginTransaction()
                    .add(layoutId, fragment)
                    .addToBackStack(fragment.tag)
                    .commit()
        }

    }

    fun showChatList(activity: AppCompatActivity?, layoutId: Int, hostUserData: UserData, tutorActions: TutorActions, chatActions: ChatActions) {

        val fragment: Fragment = GroupChannelListFragment.newInstance(hostUserData, object : TutorActions {
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

        })

        if (activity != null && !activity.supportFragmentManager.isDestroyed) {
            val manager: FragmentManager = activity.supportFragmentManager

            manager.beginTransaction()
                    .add(layoutId, fragment)
                    .commitAllowingStateLoss()
        }

    }

}