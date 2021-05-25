package com.sendbird.android.sample.main.allChat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sendbird.android.*
import com.sendbird.android.sample.groupchannel.CreateGroupChannelActivity
import com.sendbird.android.sample.main.ConnectionManager

import com.sendbird.android.sample.R
import com.sendbird.android.sample.utils.toMutableMap

class GroupAllActiveChatListFragment : Fragment() {
    private var mRecyclerView: RecyclerView? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var mChannelListAdapter: GroupAllChatListAdapter? = null
    private var mChannelListQuery: GroupChannelListQuery? = null
    private var mSwipeRefresh: SwipeRefreshLayout? = null
    var channelListener: ChannelListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_all_chat_channel_list, container, false)
        retainInstance = true
        mRecyclerView = rootView.findViewById(R.id.recycler_group_all_chat_list)
        mSwipeRefresh = rootView.findViewById(R.id.swipe_layout_group_channel_list)

        mSwipeRefresh?.setOnRefreshListener(OnRefreshListener {
            mSwipeRefresh?.setRefreshing(true)
            refresh()
        })

        mChannelListAdapter = GroupAllChatListAdapter(activity)
        //mChannelListAdapter!!.load()
        setUpRecyclerView()
        setUpChannelListAdapter()
        return rootView
    }


    override fun onResume() {
        ConnectionManager.addConnectionManagementHandler(
            CONNECTION_HANDLER_ID
        ) { reconnect: Boolean -> refresh() }
        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, object : SendBird.ChannelHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {}
            override fun onChannelChanged(channel: BaseChannel) {
                mChannelListAdapter!!.clearMap()
                mChannelListAdapter!!.updateOrInsert(channel)
            }

            override fun onTypingStatusUpdated(channel: GroupChannel) {
                mChannelListAdapter!!.notifyDataSetChanged()
            }
        })
        super.onResume()
    }

    override fun onPause() {
        mChannelListAdapter!!.save()

//        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
//        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == INTENT_REQUEST_NEW_GROUP_CHANNEL) {
            if (resultCode == Activity.RESULT_OK) {
                // Channel successfully created
                // Enter the newly created channel.
                val newChannelUrl =
                    data!!.getStringExtra(CreateGroupChannelActivity.EXTRA_NEW_CHANNEL_URL)
                newChannelUrl?.let { enterGroupChannel(it) }
            } else {
                Log.d("GrChLIST", "resultCode not STATUS_OK")
            }
        }
    }

    // Sets up recycler view
    private fun setUpRecyclerView() {
        mLayoutManager = LinearLayoutManager(context)
        mRecyclerView!!.layoutManager = mLayoutManager
        mRecyclerView!!.adapter = mChannelListAdapter
        mRecyclerView!!.addItemDecoration(
            DividerItemDecoration(
                context,
                mLayoutManager!!.orientation
            )
        )

        // If user scrolls to bottom of the list, loads more channels.
        mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (mLayoutManager!!.findLastVisibleItemPosition() == mChannelListAdapter!!.itemCount - 1) {
                    loadNextChannelList()
                }
            }
        })

    }


    // Sets up channel list adapter
    private fun setUpChannelListAdapter() {
        mChannelListAdapter!!.setOnItemClickListener { channel: GroupChannel ->
            enterGroupChannel(
                channel
            )
        }
        mChannelListAdapter!!.setOnItemLongClickListener { channel: GroupChannel ->
//            showChannelOptionsDialog(
//                channel
//            )
        }
    }

    /**
     * Displays a dialog listing channel-specific options.
     */
    private fun showChannelOptionsDialog(channel: GroupChannel) {
        val options: Array<String>
        val pushCurrentlyEnabled = channel.isPushEnabled
        options = if (pushCurrentlyEnabled) arrayOf(
            "Leave channel",
            "Turn push notifications OFF"
        ) else arrayOf("Leave channel", "Turn push notifications ON")
        val builder = AlertDialog.Builder(
            requireActivity()
        )
        builder.setTitle("Channel options")
            .setItems(options) { dialog, which ->
                if (which == 0) {
                    // Show a dialog to confirm that the user wants to leave the channel.
                    AlertDialog.Builder(requireContext())
                        .setTitle("Leave channel " + channel.name + "?")
                        .setPositiveButton(
                            "Leave"
                        ) { dialog, which -> leaveChannel(channel) }
                        .setNegativeButton("Cancel", null)
                        .create().show()
                } else if (which == 1) {
                    setChannelPushPreferences(channel, !pushCurrentlyEnabled)
                }
            }
        builder.create().show()
    }

    /**
     * Turns push notifications on or off for a selected channel.
     *
     * @param channel The channel for which push preferences should be changed.
     * @param on      Whether to set push notifications on or off.
     */
    private fun setChannelPushPreferences(channel: GroupChannel, on: Boolean) {
        // Change push preferences.
        channel.setPushPreference(on,
            GroupChannel.GroupChannelSetPushPreferenceHandler { e ->
                if (e != null) {
                    e.printStackTrace()
                    Toast.makeText(activity, "Error: " + e.message, Toast.LENGTH_SHORT)
                        .show()
                    return@GroupChannelSetPushPreferenceHandler
                }
                val toast =
                    if (on) "Push notifications have been turned ON" else "Push notifications have been turned OFF"
                Toast.makeText(activity, toast, Toast.LENGTH_SHORT)
                    .show()
            })
    }

    /**
     * Enters a Group Channel. Upon entering, a GroupChatFragment will be inflated
     * to display messages within the channel.
     *
     * @param channel The Group Channel to enter.
     */
    private fun enterGroupChannel(channel: GroupChannel) {
        val channelUrl = channel.url
        channelListener?.onChannelClicked(channelUrl)
    }

    /**
     * Enters a Group Channel with a URL.
     *
     * @param channelUrl The URL of the channel to enter.
     */
    private fun enterGroupChannel(channelUrl: String) {

    }

    private fun refresh() {
        refreshChannelList(CHANNEL_LIST_LIMIT)
    }

    /**
     * Creates a new query to get the list of the user's Group Channels,
     * then replaces the existing dataset.
     *
     * @param numChannels The number of channels to load.
     */
    private fun refreshChannelList(numChannels: Int) {
        mChannelListQuery = GroupChannel.createMyGroupChannelListQuery()
        mChannelListQuery?.limit = numChannels
        mChannelListQuery?.customTypesFilter
        mChannelListAdapter?.showLoadingState()
        mChannelListQuery?.next(GroupChannelListQuery.GroupChannelListQueryResultHandler { list, e ->
            if (e != null) {
                // Error!
                e.printStackTrace()
                mChannelListAdapter?.hideLoadingState()
                return@GroupChannelListQueryResultHandler
            }

            mChannelListAdapter?.hideLoadingState()
            mChannelListAdapter!!.clearMap()
            mChannelListAdapter!!.setAllGroupChannelList(list)

            val activeChannelList: List<GroupChannel> = list.filter { ch ->
                val questionDetailsMap = ch.data.toMutableMap()

                questionDetailsMap["active"].toString().toBoolean()
            }

            mChannelListAdapter?.setGroupChannelList(activeChannelList)
        })

        if (mSwipeRefresh!!.isRefreshing) {
            mSwipeRefresh!!.isRefreshing = false
        }

    }



    /**
     * Loads the next channels from the current query instance.
     */
    private fun loadNextChannelList() {
        mChannelListQuery!!.next(GroupChannelListQuery.GroupChannelListQueryResultHandler { list, e ->
            if (e != null) {
                // Error!
                e.printStackTrace()
                return@GroupChannelListQueryResultHandler
            }

            val activeChannelList: List<GroupChannel> = list.filter { ch ->
                val questionDetailsMap = ch.data.toMutableMap()
                (questionDetailsMap["active"] as String).toBoolean()
            }

            mChannelListAdapter?.setGroupChannelList(activeChannelList)
        })
    }



    /**
     * Leaves a Group Channel.
     *
     * @param channel The channel to leave.
     */
    private fun leaveChannel(channel: GroupChannel) {
        channel.leave(GroupChannel.GroupChannelLeaveHandler { e ->
            if (e != null) {
                // Error!
                return@GroupChannelLeaveHandler
            }
            // Re-query message list
            refresh()
        })
    }



    companion object {
        const val EXTRA_GROUP_CHANNEL_URL = "GROUP_CHANNEL_URL"
        const val IS_ACTIVE = "IS_ACTIVE"
        private const val INTENT_REQUEST_NEW_GROUP_CHANNEL = 302
        private const val CHANNEL_LIST_LIMIT = 15
        private const val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHANNEL_LIST"
        private const val CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST"
    }


    interface ChannelListener {
        fun onChannelClicked(channelUrl: String)
    }
}