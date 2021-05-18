package com.sendbird.android.sample.main.allChat

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.sendbird.android.sample.R
import kotlinx.android.synthetic.main.fragment_chat_pager.*
import kotlinx.android.synthetic.main.fragment_chat_pager.view.*

class PagerFragment : Fragment() {
    lateinit var chatPagerAdapter: ChatPagerAdapter

    private val pagerFragmentArgs: PagerFragmentArgs by navArgs()
    private val deepLinkUrl: String by lazy { pagerFragmentArgs.deeplinkUrl }
    private val entryPoint: String by lazy { pagerFragmentArgs.entryPoint }

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

        chatPagerAdapter = ChatPagerAdapter(this) { channelUrl, isActive ->
            val directions = PagerFragmentDirections.actionPagerFragmentToChatNav(
                channelUrl, isActive,
                deepLinkUrl, entryPoint
            )
            findNavController().navigate(directions)
        }

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