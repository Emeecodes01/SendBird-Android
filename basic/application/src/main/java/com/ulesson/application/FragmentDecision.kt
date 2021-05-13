package com.ulesson.application

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import com.sendbird.android.sample.groupchannel.endsession.EndChatSessionViewModel
import com.sendbird.android.sample.main.sendBird.Chat
import com.sendbird.android.sample.main.sendBird.UserData

class FragmentDecision: Fragment(R.layout.fragment_decision) {

    val endChatVM: EndChatSessionViewModel by activityViewModels()

    private val userData = UserData(
        "Tutor-6", "Emmanuel Ozibo",
        "d34c6af89c6bbd4f218af2afb8b63460e4482afa"
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
                    requireActivity().onBackPressed()
                }
            }
        )

        view.findViewById<Button>(R.id.button).setOnClickListener {
            Chat()
                .connectUserToSendBird(userData,
                    onConnected = {
                        val direct = FragmentDecisionDirections.actionFragmentDecisionToChatNav(
                            "sendbird_group_channel_90806412_788f636bc06ce4d387ba32b414ee6f2f162c8537", "sfddfs")
                        findNavController().navigate(direct)
                    },
                    connectionFailed = { err ->
                        Toast.makeText(requireContext(), "${err.message}", Toast.LENGTH_SHORT).show()
                    })
        }


        view.findViewById<Button>(R.id.button2).setOnClickListener {
            Chat()
                .connectUserToSendBird(userData,
                    onConnected = {
                        val action = FragmentDecisionDirections.actionFragmentDecisionToAllChatsNav()
                        findNavController().navigate(action)
                    },
                    connectionFailed = { err ->
                        Toast.makeText(requireContext(), "${err.message}", Toast.LENGTH_SHORT).show()
                    })

        }


    }
}