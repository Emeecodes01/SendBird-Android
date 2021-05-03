package com.sendbird.android.sample.main

import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannel.GroupChannelCreateHandler
import com.sendbird.android.SendBird
import com.sendbird.android.SendBird.ConnectHandler
import com.sendbird.android.SendBird.UserInfoUpdateHandler
import com.sendbird.android.SendBirdException
import com.sendbird.android.sample.R
import com.sendbird.android.sample.fcm.MyFirebaseMessagingService
import com.sendbird.android.sample.groupchannel.GroupChannelListFragment
import com.sendbird.android.sample.groupchannel.GroupChatFragment
import com.sendbird.android.sample.groupchannel.GroupChatFragment.CONNECTION_HANDLER_ID
import com.sendbird.android.sample.utils.PreferenceUtils
import com.sendbird.android.sample.utils.PushUtils

class SendBirdChat {

    fun start(userId: String, accessToken: String, userNickname: String, handler: ConnectHandler) {
        connectToSendBird(userId, accessToken, userNickname) { user, e -> handler.onConnected(user, e) }
    }

    fun connectToSendBird(userId: String, accessToken: String, userNickname: String, handler: ConnectHandler) {

        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userNickname)) {
            return
        }

        ConnectionManager.login(userId, accessToken, ConnectHandler { user, e ->

            if (e != null) {
                Log.d("okh", e.message + "")

                return@ConnectHandler
            }

            PreferenceUtils.setConnected(true)

            // Update the user's nickname
            updateCurrentUserInfo(userId, userNickname)
            PushUtils.registerPushHandler(MyFirebaseMessagingService())
            handler.onConnected(user, e)
        })
    }

    private fun updateCurrentUserInfo(userId: String, userNickname: String) {

        SendBird.updateCurrentUserInfo(userNickname, null, object : UserInfoUpdateHandler {

            override fun onUpdated(e: SendBirdException?) {
                if (e != null) {
                    return
                }

                PreferenceUtils.setUserId(userId)
                PreferenceUtils.setNickname(userNickname)
            }

        })
    }

    fun AppCompatActivity.createChat(hostUserData: UserData, otherUserData: UserData) {

        PreferenceUtils.init(this.baseContext)

        if (PreferenceUtils.getConnected()) {

            createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->
                val fragment = GroupChatFragment.newInstance(groupChannel.url)
                this@createChat.supportFragmentManager.beginTransaction()
                        .replace(R.id.content, fragment)
                        .addToBackStack(null)
                        .commit()
            }

        } else {

            login(hostUserData.id, hostUserData.accessToken, hostUserData.nickname) { user, e ->

                createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->

                    val fragment = GroupChatFragment.newInstance(groupChannel.url)
                    this@createChat.supportFragmentManager.beginTransaction()
                            .replace(R.id.container_group_channel, fragment)
                            .addToBackStack(null)
                            .commit()
                }
            }
        }

    }

    data class UserData(val id: String, val nickname: String, val accessToken: String = "")

    fun Fragment.createChat(hostUserData: UserData, otherUserData: UserData) {

        PreferenceUtils.init(this.requireContext())

        if (PreferenceUtils.getConnected()) {

            createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->
                val fragment = GroupChatFragment.newInstance(groupChannel.url)
                activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.content, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
            }

        } else {

            login(hostUserData.id, hostUserData.accessToken, hostUserData.nickname) { user, e ->

                createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->

                    val fragment = GroupChatFragment.newInstance(groupChannel.url)
                    activity?.supportFragmentManager?.beginTransaction()
                            ?.replace(R.id.content, fragment)
                            ?.addToBackStack(null)
                            ?.commit()
                }
            }
        }

    }

    private fun createGroupChat(hostId: String, otherId: String, groupChannelCreateHandler: GroupChannelCreateHandler) {
        val userIdList = listOf(hostId, otherId)
        GroupChannel.createChannelWithUserIds(userIdList, true, GroupChannelCreateHandler { groupChannel, e ->
            if (e != null) {
                // Error!
                return@GroupChannelCreateHandler
            }

            groupChannelCreateHandler.onResult(groupChannel, e)
        })
    }

    fun AppCompatActivity.showChatList(hostUserData: UserData){

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {
            if (it){
                val fragment: Fragment = GroupChannelListFragment.newInstance()

                val manager: FragmentManager = this@showChatList.supportFragmentManager
                manager.popBackStack()

                manager.beginTransaction()
                        .replace(R.id.container_group_channel, fragment)
                        .commit()
            }else{

                login(hostUserData.id, hostUserData.accessToken, hostUserData.nickname) { user, e ->

                    val fragment: Fragment = GroupChannelListFragment.newInstance()

                    val manager: FragmentManager = this@showChatList.supportFragmentManager
                    manager.popBackStack()

                    manager.beginTransaction()
                            .replace(R.id.container_group_channel, fragment)
                            .commit()
                }
            }

        }

    }

    private fun login(hostUserId: String, hostAccessToken: String, userNickName: String, connectHandler: ConnectHandler) {
        SendBirdChat().start(hostUserId, hostAccessToken, userNickName) { user, e -> connectHandler.onConnected(user, e) }
    }


}