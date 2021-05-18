package com.sendbird.android.sample.main.allChat

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChatPagerAdapter(
    val fragment: Fragment,
    private val channelClicked: (String, Boolean) -> Unit
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> {
                GroupAllActiveChatListFragment()
                    .apply {
                        channelListener = object : GroupAllActiveChatListFragment.ChannelListener {
                            override fun onChannelClicked(channelUrl: String) {
                                channelClicked.invoke(channelUrl, true)
                            }

                        }
                    }
            }

            1 -> {
                GroupAllInActiveChatListFragment()
                    .apply {
                        channelListener =
                            object : GroupAllInActiveChatListFragment.ChannelListener {
                                override fun onChannelClicked(channelUrl: String) {
                                    channelClicked.invoke(channelUrl, false)
                                }

                            }
                    }
            }

            else -> GroupAllActiveChatListFragment()
        }

    }

}