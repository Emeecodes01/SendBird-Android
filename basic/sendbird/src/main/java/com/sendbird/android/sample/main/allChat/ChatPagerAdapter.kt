package com.sendbird.android.sample.main.allChat

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChatPagerAdapter(val fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> {
                GroupAllActiveChatListFragment.newInstance(true)
            }

            1 -> {
                GroupAllInActiveChatListFragment.newInstance(false)
            }

            else -> GroupAllActiveChatListFragment.newInstance(true)
        }

    }

}