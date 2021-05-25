package com.sendbird.android.sample.main.sendBird

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.sendbird.android.*
import com.sendbird.android.GroupChannel.GroupChannelCreateHandler
import com.sendbird.android.SendBird.ConnectHandler
import com.sendbird.android.sample.R
import com.sendbird.android.sample.groupchannel.GroupChannelListFragment

import com.sendbird.android.sample.main.ConnectionManager
import com.sendbird.android.sample.main.allChat.PagerFragment
import com.sendbird.android.sample.main.chat.GroupChatFragment
import com.sendbird.android.sample.main.chat.GroupChatFragment.Companion.CONNECTION_HANDLER_ID
import com.sendbird.android.sample.utils.PreferenceUtils

class Chat {


    /**
     * Create chat between 2 users, each user has a UserData object which contains their userid, nickname and access token
     */

    fun createChat(
        context: Context,
        activity: FragmentActivity, hostUserData: UserData, otherUserData: UserData,
        metaData: Map<String, String>
    ) {

        PreferenceUtils.init(activity.baseContext)

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {
            if (it) {
                createGroupChat(hostUserData.id, otherUserData.id, metaData) { groupChannel, p1 ->
                    val fragment = GroupChatFragment.newInstance(groupChannel.url, false)
                    activity.supportFragmentManager.beginTransaction()
                        .add(android.R.id.content, fragment)
                        .addToBackStack(fragment.tag)
                        .commit()
                }

            } else {

                login(
                    context,
                    UserData(
                        hostUserData.id,
                        hostUserData.nickname,
                        hostUserData.accessToken
                    )
                ) { user, e ->

                    if (user != null) {
                        createGroupChat(
                            hostUserData.id,
                            otherUserData.id,
                            metaData
                        ) { groupChannel, p1 ->

                            val fragment = GroupChatFragment.newInstance(groupChannel.url, false)
                            activity.supportFragmentManager.beginTransaction()
                                .add(android.R.id.content, fragment)
                                .addToBackStack(fragment.tag)
                                .commit()
                        }
                    } else {
                        Toast.makeText(activity.baseContext, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }


        }

    }

    fun Fragment.createChat(
        context: Context,
        hostUserData: UserData,
        otherUserData: UserData,
        metaData: Map<String, String>
    ) {

        PreferenceUtils.init(this.requireContext())

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {

            if (it) {

                createGroupChat(hostUserData.id, otherUserData.id, metaData) { groupChannel, p1 ->
                    val fragment = GroupChatFragment.newInstance(groupChannel.url, false)
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.add(android.R.id.content, fragment)
                        ?.addToBackStack(fragment.tag)
                        ?.commit()
                }

            } else {

                login(
                    context,
                    UserData(
                        hostUserData.id,
                        hostUserData.nickname,
                        hostUserData.accessToken
                    )
                ) { user, e ->

                    if (user != null) {

                        createGroupChat(
                            hostUserData.id,
                            otherUserData.id,
                            metaData
                        ) { groupChannel, p1 ->

                            val fragment = GroupChatFragment.newInstance(groupChannel.url, false)
                            activity?.supportFragmentManager?.beginTransaction()
                                ?.add(android.R.id.content, fragment)
                                ?.addToBackStack(fragment.tag)
                                ?.commit()
                        }

                    } else {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    }

                }
            }

        }

    }

    private fun createGroupChat(
        hostId: String, otherId: String,
        channelCreationMetadata: Map<String, String>,
        groupChannelCreateHandler: GroupChannelCreateHandler
    ) {

        val userIdList = listOf(hostId, otherId)
        GroupChannel.createChannelWithUserIds(
            userIdList,
            true,
            "$hostId and $otherId Chat",
            "",
            "active",
            "",
            GroupChannelCreateHandler { groupChannel, e ->
                if (e != null) {
                    // Error!
                    return@GroupChannelCreateHandler
                }


                groupChannel.createMetaData(
                    channelCreationMetadata
                ) { p0, e ->
                    if (e != null) {
                        e.printStackTrace()
                        return@createMetaData
                    }

                    groupChannelCreateHandler.onResult(groupChannel, e)
                }
            })
    }

    /**
     * @param channelUrl -> When a chat is already created, this fun allows another user to enter into the channel
     */
    fun enterGroupChat(context: Context, activity: FragmentActivity, channelUrl: String, userData: UserData) {
        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {
            if (it) {
                val groupChatFragment = GroupChatFragment.newInstance(channelUrl, false)
                activity.supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, groupChatFragment)
                    .addToBackStack(groupChatFragment.tag)
                    .commit()
            } else {
                login(context, UserData(userData.id, userData.nickname, userData.accessToken)) { user, e ->

                    if (e != null) {
                        Toast.makeText(
                            activity,
                            "There was an error entering chat: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@login
                    }


                    val groupChatFragment = GroupChatFragment.newInstance(channelUrl, false)
                    activity.supportFragmentManager.beginTransaction()
                        .add(android.R.id.content, groupChatFragment)
                        .addToBackStack(groupChatFragment.tag)
                        .commit()
                }
            }
        }
    }


    /**
     * Basically reconnects a user to send bird
     */
    fun connectUserToSendBird (
        context: Context,
        hostUserData: UserData,
        onConnected: () -> Unit,
        connectionFailed: (Exception) -> Unit
    ) {
        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) { isReconnected ->
            if (isReconnected) onConnected.invoke()
            else {
                login(
                    context,
                    UserData(
                        hostUserData.id,
                        hostUserData.nickname,
                        hostUserData.accessToken
                    )
                ) { _, e ->
                    when {
                        e != null -> {
                            connectionFailed.invoke(e)
                        }
                        else -> {
                            onConnected.invoke()
                        }
                    }

                }
            }

        }
    }

    /**
     * Show all the chat list of a user, pass in the data of the user you want to show
     */

    fun showAllChat(context: Context, activity: FragmentActivity?, layoutId: Int, hostUserData: UserData) {

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
                login(
                    context,
                    UserData(
                        hostUserData.id,
                        hostUserData.nickname,
                        hostUserData.accessToken
                    )
                ) { user, e ->

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

    fun showChatList(context: Context, activity: FragmentActivity?, layoutId: Int, hostUserData: UserData) {

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {
            if (it) {
                val fragment: Fragment = GroupChannelListFragment.newInstance(true)

                if (activity != null && !fragment.isAdded) {
                    val manager: FragmentManager = activity.supportFragmentManager

                    manager.beginTransaction()
                        .add(layoutId, fragment)
                        .commit()
                }

            } else {

                login(
                    context,
                    UserData(
                        hostUserData.id,
                        hostUserData.nickname,
                        hostUserData.accessToken
                    )
                ) { user, e: SendBirdException? ->

                    if (user != null || PreferenceUtils.getUserId().isNotEmpty()) {

                        val fragment: Fragment = GroupChannelListFragment.newInstance(true)

                        if (activity != null && !fragment.isAdded) {
                            val manager: FragmentManager = activity.supportFragmentManager

                            manager.beginTransaction()
                                .add(layoutId, fragment)
                                .commit()
                        }
                    }

                }
            }

        }

    }

    private fun login(context: Context, userData: UserData, connectHandler: ConnectHandler) {
        Connect().login(context, userData) { user, e: SendBirdException? ->
            connectHandler.onConnected(user, e)
        }
    }

}