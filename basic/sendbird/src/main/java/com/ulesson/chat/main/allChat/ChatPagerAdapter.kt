package com.ulesson.chat.main.allChat

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ulesson.chat.main.sendBird.ChatActions
import com.ulesson.chat.main.sendBird.TutorActions
import com.ulesson.chat.utils.ChatType

class ChatPagerAdapter(
    fragment: Fragment,
    private val tutorActions: TutorActions,
    private val chatActions: ChatActions,
    private val newVersion: Boolean
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> {
                if (newVersion) {
                    GroupAllChatListFragment.newInstance(
                        ChatType.PendingChat,
                        tutorActions,
                        chatActions
                    )
                } else {
                    GroupAllChatListFragment.newInstance(
                        ChatType.PendingQuestion,
                        tutorActions,
                        chatActions
                    )
                }
            }
            1 -> {
                GroupAllChatListFragment.newInstance(
                    ChatType.Active,
                    tutorActions,
                    chatActions
                )
            }
            else -> {
                GroupAllChatListFragment.newInstance(
                    ChatType.Past,
                    tutorActions,
                    chatActions
                )
            }
        }
    }
}