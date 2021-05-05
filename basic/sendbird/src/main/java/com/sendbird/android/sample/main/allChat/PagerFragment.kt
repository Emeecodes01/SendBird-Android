package com.sendbird.android.sample.main.allChat

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.sendbird.android.sample.R
import kotlinx.android.synthetic.main.fragment_chat_pager.*
import kotlinx.android.synthetic.main.fragment_chat_pager.view.*

class PagerFragment : Fragment() {

    lateinit var chatPagerAdapter: ChatPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_chat_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val chatPager = view.chatViewPager
        val tabLayout = view.tabLayout

        view.toolbar_group_channel.setNavigationOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        chatPagerAdapter = ChatPagerAdapter(this)
        chatPager.adapter = chatPagerAdapter

        TabLayoutMediator(tabLayout, chatPager) { tab, position ->

            when (position) {
                0 -> tab.text = Html.fromHtml("\t\t\t\tACTIVE\t\t\t\t")
                1 -> tab.text = Html.fromHtml("\t\t\t\tPAST\t\t\t\t")
            }
        }.attach()

        super.onViewCreated(view, savedInstanceState)

    }
}