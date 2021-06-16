package com.ulesson.chat.main.allChat

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ulesson.chat.main.sendBird.ChatActions
import com.ulesson.chat.main.sendBird.TutorActions

class ChatPagerAdapter(
    fragment: Fragment,
    private val tutorActions: TutorActions,
    private val chatActions: ChatActions
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> GroupAllChatListFragment.newInstance(
                GroupAllChatListFragment.ChatType.Pending,
                tutorActions,
                chatActions
            )
            1 -> GroupAllChatListFragment.newInstance(
                GroupAllChatListFragment.ChatType.Active,
                tutorActions,
                chatActions
            )
            2 -> GroupAllChatListFragment.newInstance(
                GroupAllChatListFragment.ChatType.Past,
                tutorActions,
                chatActions
            )
            else -> GroupAllChatListFragment.newInstance(
                GroupAllChatListFragment.ChatType.Active,
                tutorActions,
                chatActions
            )
        }

    }

}