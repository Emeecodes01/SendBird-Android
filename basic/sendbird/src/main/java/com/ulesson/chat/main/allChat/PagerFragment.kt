package com.ulesson.chat.main.allChat

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.ulesson.chat.R
import com.ulesson.chat.main.sendBird.ChatActions
import com.ulesson.chat.main.sendBird.TutorActions
import kotlinx.android.synthetic.main.fragment_chat_pager.view.*

class PagerFragment : Fragment() {

    lateinit var chatPagerAdapter: ChatPagerAdapter

    companion object {

        lateinit var tutorActionsChannel: TutorActions
        lateinit var chatActionsChannel: ChatActions
        var newVersionChannel: Boolean = false

        fun newInstance(tutorActions: TutorActions, chatActions: ChatActions, newVersion : Boolean ): PagerFragment {
            tutorActionsChannel = tutorActions
            chatActionsChannel = chatActions
            newVersionChannel = newVersion
            return PagerFragment()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_chat_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val chatPager = view.chatViewPager
        val tabLayout = view.tabLayout

        view.toolbar_group_channel.setNavigationOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        chatPagerAdapter = ChatPagerAdapter(this, tutorActionsChannel, chatActionsChannel, newVersionChannel)
        chatPager.adapter = chatPagerAdapter

        TabLayoutMediator(tabLayout, chatPager) { tab, position ->

            when (position) {
                0 -> tab.text = Html.fromHtml("PENDING")
                1 -> tab.text = Html.fromHtml("ACTIVE")
                2 -> tab.text = Html.fromHtml("PAST")
            }
        }.attach()

        super.onViewCreated(view, savedInstanceState)

    }
}