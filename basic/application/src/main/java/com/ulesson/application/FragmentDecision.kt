package com.ulesson.application

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sendbird.android.sample.main.chat.GroupChatFragment
import com.sendbird.android.sample.main.chat.endsession.EndChatSessionViewModel
import com.sendbird.android.sample.main.sendBird.Chat
import com.sendbird.android.sample.main.sendBird.UserData

class FragmentDecision : Fragment(R.layout.fragment_decision) {

    val endChatVM: EndChatSessionViewModel by activityViewModels()

    private val userData = UserData(
        "Tutor-9", "Emmanuel UAT",
        "d747c1d9420e5ff86901c84809bcb17d03e49826"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //observe with activity lifecycle
        endChatVM.backToTimeline.observe(requireActivity()) { isClicked ->
            if (isClicked) {
                findNavController().navigate(FragmentDecisionDirections.actionNavGraphPop())
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        )

        view.findViewById<Button>(R.id.button).setOnClickListener {
            Chat()
                .connectUserToSendBird(requireContext(), userData,
                    onConnected = {
                        val direct = FragmentDecisionDirections.actionFragmentDecisionToChatNav(
                            "sendbird_group_channel_101121152_2444ab9f8111ce5e62a020ed3f74f0c16ac4b803",
                            GroupChatFragment.DASHBOARD, "fragdecsion://fragment_start"
                        )
                        findNavController().navigate(direct)
                    },
                    connectionFailed = { err ->
                        Toast.makeText(requireContext(), "${err.message}, Code: ${err}", Toast.LENGTH_SHORT)
                            .show()
                    })
        }


        view.findViewById<Button>(R.id.button2).setOnClickListener {
            Chat()
                .connectUserToSendBird(requireContext(), userData,
                    onConnected = {
                        val action =
                            FragmentDecisionDirections.actionFragmentDecisionToAllChatsNav("fragdecsion://fragment_start",
                            "chat_list")
                        findNavController().navigate(action)
                    },
                    connectionFailed = { err ->
                        Toast.makeText(requireContext(), "${err.message}", Toast.LENGTH_SHORT)
                            .show()
                    })

        }


    }
}