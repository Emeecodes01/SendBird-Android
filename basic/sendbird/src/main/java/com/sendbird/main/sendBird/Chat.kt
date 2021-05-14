package com.sendbird.main.sendBird

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannel.GroupChannelCreateHandler
import com.sendbird.android.GroupChannelParams
import com.sendbird.groupchannel.GroupChannelListFragment
import com.sendbird.groupchannel.GroupChatFragment
import com.sendbird.main.allChat.PagerFragment
import com.sendbird.network.createUser.ConnectUserRequest

class Chat {

    /**
     * Create chat between 2 users, each user has a UserData object which contains their userid, nickname and access token
     */

    fun createChat(activity: FragmentActivity, hostUserData: UserData, otherUserData: UserData, channelUrl: (String) -> Unit, channelError: (String) -> Unit) {

        createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, error ->

            groupChannel?.url?.let {
                channelUrl(it)
                val fragment = GroupChatFragment.newInstance(groupChannel.url)
                activity.supportFragmentManager.beginTransaction()
                        .add(android.R.id.content, fragment)
                        .addToBackStack(fragment.tag)
                        .commitAllowingStateLoss()
            }
            error?.message?.let {
                channelError(it)
                Connect().refreshChannel {
                    createChat(activity, hostUserData, otherUserData, channelUrl, channelError)
                }
            }

        }

    }

    fun createChatWithQuestion(activity: FragmentActivity, hostUserData: UserData, otherUserData: UserData) {

        createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->
            val fragment = GroupChatFragment.newInstance(groupChannel.url)
            activity.supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, fragment)
                    .addToBackStack(fragment.tag)
                    .commitAllowingStateLoss()
        }

    }

    private fun createGroupChat(hostId: String, otherId: String, groupChannelCreateHandler: GroupChannelCreateHandler) {
        val userIdList = listOf(hostId, otherId)
        GroupChannel.createChannelWithUserIds(userIdList, true, "$hostId and $otherId Chat", "", "active", "", GroupChannelCreateHandler { groupChannel, e ->
            groupChannelCreateHandler.onResult(groupChannel, e)
        })
    }


    fun updateGroupChat(channelUrl: String, groupChannelUpdateHandler: GroupChannel.GroupChannelUpdateHandler) {

        GroupChannel.getChannel(channelUrl) { groupChannel, e ->

            val groupChannelParams = GroupChannelParams()
            groupChannelParams.setData("past")
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

    fun showChatList(activity: AppCompatActivity?, layoutId: Int, hostUserData: UserData) {

        val fragment: Fragment = GroupChannelListFragment.newInstance(true, hostUserData)

        if (activity != null && !fragment.isAdded) {

            val manager: FragmentManager = activity.supportFragmentManager

            manager.beginTransaction()
                    .add(layoutId, fragment)
                    .commitAllowingStateLoss()
        }

    }

}