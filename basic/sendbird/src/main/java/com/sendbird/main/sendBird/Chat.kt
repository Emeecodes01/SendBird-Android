package com.sendbird.main.sendBird

import android.R
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannel.GroupChannelCreateHandler
import com.sendbird.android.GroupChannelParams
import com.sendbird.android.Member
import com.sendbird.groupchannel.GroupChannelListFragment
import com.sendbird.groupchannel.GroupChatFragment
import com.sendbird.main.allChat.PagerFragment
import com.sendbird.main.model.UserData
import com.sendbird.network.userModel.ConnectUserRequest
import com.sendbird.utils.StringUtils.Companion.isActive
import com.sendbird.utils.StringUtils.Companion.toMutableMap

class Chat {

    /**
     * Create chat between 2 users, each user has a UserData object which contains their userid, nickname and access token
     */


    fun createChat(activity: FragmentActivity, hostUserData: UserData, otherUserData: UserData, questionMap: MutableMap<String, Any?>?, channelUrl: (String) -> Unit, updateToken : (String) -> Unit) {

        createGroupChat(hostUserData, otherUserData.id, questionMap, { groupChannel, error ->

            error?.let {

                if (groupChannel.data.isActive()) {
                    gotoChat(groupChannel, questionMap, activity)
                } else {

                    val activeMap = HashMap<String, Any>()
                    activeMap["active"] = true
                    updateGroupChat(groupChannel.url, groupChannel.data, activeMap) { updatedGroupChannel, updatedError ->

                        updatedGroupChannel?.url?.let {
                            channelUrl(it)

                            gotoChat(updatedGroupChannel, questionMap, activity)
                        }
                    }
                }

            } ?: kotlin.run {
                gotoChat(groupChannel, questionMap, activity)
            }

        }, {
            updateToken(it)
        })

    }

    private fun gotoChat(groupChannel: GroupChannel, questionMap: MutableMap<String, Any?>?, activity: FragmentActivity): Int {
        val fragment = GroupChatFragment.newInstance(groupChannel.url, questionMap.toString(), object : TutorActions {
            override fun showTutorProfile(members: List<Member>) {}
            override fun showTutorRating(questionMap: MutableMap<String, Any?>) {}
        })
        return activity.supportFragmentManager.beginTransaction()
                .add(R.id.content, fragment)
                .addToBackStack(fragment.tag)
                .commitAllowingStateLoss()
    }

    private fun createGroupChat(hostUserData: UserData, otherId: String, questionMap: MutableMap<String, Any?>?, groupChannelCreateHandler: GroupChannelCreateHandler, updateToken : (String) -> Unit) {
        val userIdList = listOf(hostUserData.id, otherId)

        GroupChannel.createChannelWithUserIds(userIdList, true, "${hostUserData.id} and $otherId Chat", "", questionMap.toString(), "") { groupChannel, error ->

            error?.let {

                Connect().refreshActivity({
                    createGroupChat(hostUserData, otherId, questionMap, groupChannelCreateHandler, updateToken)
                }, {

                    val connectUserRequest = ConnectUserRequest(hostUserData.id, hostUserData.nickname, "")
                    User().connectUser(connectUserRequest, "", {
                        hostUserData.accessToken = it.access_token
                        updateToken(it.access_token)
                        createGroupChat(hostUserData, otherId, questionMap, groupChannelCreateHandler, updateToken)
                    }, {

                    }, {
                        it?.let {
                            Log.d("okh", "$it token group")
                        }
                    })
                })

            } ?: kotlin.run {
                groupChannelCreateHandler.onResult(groupChannel, error)
            }

        }

    }


    fun updateGroupChat(channelUrl: String, channelData: String, updateMap: HashMap<String, Any>, groupChannelUpdateHandler: GroupChannel.GroupChannelUpdateHandler) {

        GroupChannel.getChannel(channelUrl) { groupChannel, e ->

            val groupChannelParams = GroupChannelParams()
            val map = channelData.toMutableMap() + updateMap.toMutableMap()
            groupChannelParams.setData(map.toString())
            groupChannel.updateChannel(groupChannelParams, groupChannelUpdateHandler)

            groupChannelUpdateHandler.onResult(groupChannel, e)
        }

    }

    /**
     * Show all the chat list of a user, pass in the data of the user you want to show
     */

    fun showAllChat(activity: FragmentActivity?, layoutId: Int, hostUserData: UserData) {

        val fragment: Fragment = PagerFragment()

        if (activity != null && !fragment.isAdded) {
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

        if (activity != null && !fragment.isAdded) {

            val manager: FragmentManager = activity.supportFragmentManager

            manager.beginTransaction()
                    .add(layoutId, fragment)
                    .commitAllowingStateLoss()
        }

    }

}