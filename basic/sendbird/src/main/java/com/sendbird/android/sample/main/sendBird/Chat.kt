package com.sendbird.android.sample.main.sendBird

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sendbird.android.*
import com.sendbird.android.GroupChannel.GroupChannelCreateHandler
import com.sendbird.android.SendBird.ConnectHandler
import com.sendbird.android.sample.R
import com.sendbird.android.sample.groupchannel.GroupChannelListFragment
import com.sendbird.android.sample.groupchannel.GroupChatFragment
import com.sendbird.android.sample.groupchannel.GroupChatFragment.CONNECTION_HANDLER_ID
import com.sendbird.android.sample.main.ConnectionManager
import com.sendbird.android.sample.utils.PreferenceUtils

class Chat {


    /**
     * Create chat between 2 users, each user has a UserData object which contains their userid, nickname and access token
     */

    fun createChat(activity : AppCompatActivity, hostUserData: UserData, otherUserData: UserData) {

        PreferenceUtils.init(activity.baseContext)

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {
            if (it) {
                createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->
                    val fragment = GroupChatFragment.newInstance(groupChannel.url)
                    activity.supportFragmentManager.beginTransaction()
                            .add(R.id.content, fragment)
                            .addToBackStack(fragment.tag)
                            .commit()
                }

            } else {

                login(UserData(hostUserData.id, hostUserData.nickname, hostUserData.accessToken)) { user, e ->

                    if (user != null) {
                        createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->

                            val fragment = GroupChatFragment.newInstance(groupChannel.url)
                            activity.supportFragmentManager.beginTransaction()
                                    .add(R.id.container_group_channel, fragment)
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

    fun Fragment.createChat(hostUserData: UserData, otherUserData: UserData) {

        PreferenceUtils.init(this.requireContext())

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {

            if (it) {

                createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->
                    val fragment = GroupChatFragment.newInstance(groupChannel.url)
                    activity?.supportFragmentManager?.beginTransaction()
                            ?.replace(R.id.content, fragment)
                            ?.addToBackStack(null)
                            ?.commit()
                }

            } else {

                login(UserData(hostUserData.id, hostUserData.nickname, hostUserData.accessToken)) { user, e ->

                    if (user != null) {

                        createGroupChat(hostUserData.id, otherUserData.id) { groupChannel, p1 ->

                            val fragment = GroupChatFragment.newInstance(groupChannel.url)
                            activity?.supportFragmentManager?.beginTransaction()
                                    ?.replace(R.id.content, fragment)
                                    ?.addToBackStack(null)
                                    ?.commit()
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
        GroupChannel.createChannelWithUserIds(userIdList, true, GroupChannelCreateHandler { groupChannel, e ->
            if (e != null) {
                // Error!
                return@GroupChannelCreateHandler
            }

            groupChannelCreateHandler.onResult(groupChannel, e)
        })
    }

    /**
     * Show all the chat list of a user, pass in the data of the user you want to show
     */

    fun showChatList(activity: AppCompatActivity, layoutId : Int, hostUserData: UserData) {

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) {
            if (it) {
                val fragment: Fragment = GroupChannelListFragment.newInstance()

                val manager: FragmentManager = activity.supportFragmentManager

                manager.beginTransaction()
                        .add(layoutId, fragment)
                        .commit()
            } else {

                login(UserData(hostUserData.id, hostUserData.nickname, hostUserData.accessToken)) { user, e ->

                    val fragment: Fragment = GroupChannelListFragment.newInstance()

                    val manager: FragmentManager = activity.supportFragmentManager

                    manager.beginTransaction()
                            .add(layoutId, fragment)
                            .commit()
                }
            }

        }

    }

    private fun login(userData: UserData, connectHandler: ConnectHandler) {
        Connect().login(userData) { user, e -> connectHandler.onConnected(user, e) }
    }

}