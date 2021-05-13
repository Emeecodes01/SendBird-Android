package com.sendbird.android.sample.groupchannel.endsession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sendbird.android.sample.R

class ChatEndSessionFragment: Fragment() {

    private val endChatVM: EndChatSessionViewModel by activityViewModels()

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
            endChatVM.backToTimeline.value = true
        }
    }


    interface ChatEndListener {
        fun onBackToTLClicked()
    }
}