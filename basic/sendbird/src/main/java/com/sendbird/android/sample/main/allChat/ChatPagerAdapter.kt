package com.sendbird.android.sample.main.allChat

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChatPagerAdapter(val fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> {
                GroupAllChatListFragment.newInstance(true)
            }

            1 -> {
                GroupAllChatListFragment.newInstance(false)
            }

            else -> GroupAllChatListFragment.newInstance(true)
        }

    }

}