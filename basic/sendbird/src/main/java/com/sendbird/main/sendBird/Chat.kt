package com.sendbird.main.sendBird

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
import com.sendbird.utils.StringUtils.Companion.toMutableMap

class Chat {

    /**
     * Create chat between 2 users, each user has a UserData object which contains their userid, nickname and access token
     */


    fun createChat(activity: FragmentActivity, hostUserData: UserData, otherUserData: UserData, questionMap: MutableMap<String, Any?>?, channelUrl: (String) -> Unit, channelError: (String) -> Unit) {

        createGroupChat(hostUserData.id, otherUserData.id, questionMap) { groupChannel, error ->

            groupChannel?.url?.let {
                channelUrl(it)
                val fragment = GroupChatFragment.newInstance(groupChannel.url, object : TutorActions {
                    override fun showTutorProfile(members: List<Member>) {}
                    override fun showTutorRating(questionMap: MutableMap<String, Any?>) {}
                })
                activity.supportFragmentManager.beginTransaction()
                        .add(android.R.id.content, fragment)
                        .addToBackStack(fragment.tag)
                        .commitAllowingStateLoss()
            }
            error?.message?.let {
                channelError(it)
                Connect().refreshChannel {
                    createChat(activity, hostUserData, otherUserData, questionMap, channelUrl, channelError)
                }
            }

        }

    }

    private fun createGroupChat(hostId: String, otherId: String, questionMap: MutableMap<String, Any?>?, groupChannelCreateHandler: GroupChannelCreateHandler) {
        val userIdList = listOf(hostId, otherId)
        questionMap?.set("active", true)
        GroupChannel.createChannelWithUserIds(userIdList, true, "$hostId and $otherId Chat", "", questionMap.toString(), "", GroupChannelCreateHandler { groupChannel, e ->
            groupChannelCreateHandler.onResult(groupChannel, e)
        })
    }


    fun updateGroupChat(channelUrl: String, channelData: String, groupChannelUpdateHandler: GroupChannel.GroupChannelUpdateHandler) {

        GroupChannel.getChannel(channelUrl) { groupChannel, e ->

            val groupChannelParams = GroupChannelParams()
            val map = channelData.toMutableMap()
            map["active"] = false
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

    fun showChatList(activity: AppCompatActivity?, layoutId: Int, hostUserData: UserData, tutorActions: TutorActions) {

        val fragment: Fragment = GroupChannelListFragment.newInstance(hostUserData, object : TutorActions {
            override fun showTutorProfile(members: List<Member>) {
                tutorActions.showTutorProfile(members)
            }

            override fun showTutorRating(questionMap: MutableMap<String, Any?>) {
                tutorActions.showTutorRating(questionMap)
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