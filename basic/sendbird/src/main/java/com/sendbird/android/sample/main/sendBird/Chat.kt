package com.sendbird.android.sample.main.sendBird

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.sendbird.android.*
import com.sendbird.android.GroupChannel.GroupChannelCreateHandler
import com.sendbird.android.SendBird.ConnectHandler
import com.sendbird.android.sample.groupchannel.GroupChannelListFragment
import com.sendbird.android.sample.groupchannel.GroupChatFragment
import com.sendbird.android.sample.groupchannel.GroupChatFragment.CONNECTION_HANDLER_ID
import com.sendbird.android.sample.main.ConnectionManager
import com.sendbird.android.sample.main.allChat.PagerFragment
import com.sendbird.android.sample.network.NetworkRequest
import com.sendbird.android.sample.network.createUser.UpdateUserRequest
import com.sendbird.android.sample.utils.PreferenceUtils
import java.util.*

class Chat {


    /**
     * Create chat between 2 users, each user has a UserData object which contains their userid, nickname and access token
     */

    fun createChat(activity: FragmentActivity, hostUserData: UserData, otherUserData: UserData) {

        PreferenceUtils.init(activity.baseContext)

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {
            if (it) {
                createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->
                    val fragment = GroupChatFragment.newInstance(groupChannel.url)
                    activity.supportFragmentManager.beginTransaction()
                            .add(android.R.id.content, fragment)
                            .addToBackStack(fragment.tag)
                            .commitAllowingStateLoss()
                }

            } else {

                login(UserData(hostUserData.id, hostUserData.nickname, hostUserData.accessToken)) { user, e ->

                    if (user != null) {
                        createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->

                            val fragment = GroupChatFragment.newInstance(groupChannel.url)
                            activity.supportFragmentManager.beginTransaction()
                                    .add(android.R.id.content, fragment)
                                    .addToBackStack(fragment.tag)
                                    .commitAllowingStateLoss()
                        }
                    } else {
                        Toast.makeText(activity.baseContext, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }


        }

    }

    fun Fragment.createChat(hostUserData: UserData, otherUserData: UserData) {

        PreferenceUtils.init(this.requireContext())

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {

            if (it) {

                createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->
                    val fragment = GroupChatFragment.newInstance(groupChannel.url)
                    activity?.supportFragmentManager?.beginTransaction()
                            ?.add(android.R.id.content, fragment)
                            ?.addToBackStack(fragment.tag)
                            ?.commitAllowingStateLoss()
                }

            } else {

                login(UserData(hostUserData.id, hostUserData.nickname, hostUserData.accessToken)) { user, e ->

                    if (user != null) {

                        createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->

                            val fragment = GroupChatFragment.newInstance(groupChannel.url)
                            activity?.supportFragmentManager?.beginTransaction()
                                    ?.add(android.R.id.content, fragment)
                                    ?.addToBackStack(fragment.tag)
                                    ?.commitAllowingStateLoss()
                        }

                    } else {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    }

                }
            }

        }

    }

    private fun createGroupChat(hostId: String, otherId: String, groupChannelCreateHandler: GroupChannelCreateHandler) {
        val userIdList = listOf(hostId, otherId)
        GroupChannel.createChannelWithUserIds(userIdList, true, "$hostId and $otherId Chat", "", "active", "", GroupChannelCreateHandler { groupChannel, e ->
            if (e != null) {
                // Error!
                return@GroupChannelCreateHandler
            }

            groupChannelCreateHandler.onResult(groupChannel, e)
        })
    }

    //make default end time -1
    //if endTime -1, save the end time mapped with the channel url with current time
    //if endTime != -1, subtract current time from end time which is countdown
    //on countdown finished to 0, do nothing


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

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {
            if (it) {
                val fragment: Fragment = PagerFragment()

                if (activity != null && !fragment.isAdded) {
                    val manager: FragmentManager = activity.supportFragmentManager
                    manager.beginTransaction()
                            .add(layoutId, fragment)
                            .addToBackStack(fragment.tag)
                            .commit()
                }

            } else {

                login(UserData(hostUserData.id, hostUserData.nickname, hostUserData.accessToken)) { user, e ->

                    val fragment: Fragment = PagerFragment()

                    if (activity != null && !fragment.isAdded) {
                        val manager: FragmentManager = activity.supportFragmentManager

                        manager.beginTransaction()
                                .add(layoutId, fragment)
                                .addToBackStack(fragment.tag)
                                .commit()
                    }

                }
            }

        }

    }

    fun showChatList(activity: AppCompatActivity?, layoutId: Int, hostUserData: UserData) {

        if (PreferenceUtils.getUserId() != hostUserData.id) {
            login(UserData(hostUserData.id, hostUserData.nickname, hostUserData.accessToken)) { user, e ->

                if (user != null || PreferenceUtils.getUserId().isNotEmpty()) {

                    val fragment: Fragment = GroupChannelListFragment.newInstance(true, hostUserData)

                    if (activity != null && !fragment.isAdded) {
                        val manager: FragmentManager = activity.supportFragmentManager

                        manager.beginTransaction()
                                .add(layoutId, fragment)
                                .commitAllowingStateLoss()
                    }
                }

            }
        } else {

            PreferenceUtils.setAccessToken(hostUserData.accessToken)

            ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {

                if (it) {
                    gotoChatListFragment(activity, hostUserData, layoutId)

                } else {

                    val networkRequest = NetworkRequest()
                    networkRequest.updateUser(UpdateUserRequest(hostUserData.id, true), {

                        val loginData = UserData(hostUserData.id, hostUserData.nickname, it.access_token)

                        PreferenceUtils.setUserId(hostUserData.id)
                        PreferenceUtils.setNickname(hostUserData.nickname)
                        PreferenceUtils.setAccessToken(it.access_token)

                        Connect().login(loginData) { user, loginError ->

                            user?.let {
                                gotoChatListFragment(activity, hostUserData, layoutId)
                            } ?: kotlin.run {

                            }
                        }

                    }) {
                        gotoChatListFragment(activity, hostUserData, layoutId)
                    }
                }

            }
        }

    }

    private fun gotoChatListFragment(activity: AppCompatActivity?, hostUserData: UserData, layoutId: Int) {
        val fragment: Fragment = GroupChannelListFragment.newInstance(true, hostUserData)

        if (activity != null && !fragment.isAdded) {
            val manager: FragmentManager = activity.supportFragmentManager

            manager.beginTransaction()
                    .add(layoutId, fragment)
                    .commitAllowingStateLoss()
        }
    }

    private fun login(userData: UserData, connectHandler: ConnectHandler) {
        Connect().login(userData) { user, e -> connectHandler.onConnected(user, e) }
    }

}