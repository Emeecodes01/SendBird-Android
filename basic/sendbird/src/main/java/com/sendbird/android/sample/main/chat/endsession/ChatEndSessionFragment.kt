package com.sendbird.android.sample.main.chat.endsession

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sendbird.android.sample.R

class ChatEndSessionFragment: Fragment() {

    private val endChatVM: EndChatSessionViewModel by activityViewModels()
    private val chatEndSessionFragmentArgs: ChatEndSessionFragmentArgs by navArgs()

    var listener: ChatEndListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_session_ended, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        requireActivity().onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //disable back button
                }
            }
        )


        view.findViewById<TextView>(R.id.bt_positive).setOnClickListener {
            //endChatVM.backToTimeline.value = true
            findNavController().navigate(Uri.parse(chatEndSessionFragmentArgs.deeplinkUrl))
        }
    }


    interface ChatEndListener {
        fun onBackToTLClicked()
    }
}