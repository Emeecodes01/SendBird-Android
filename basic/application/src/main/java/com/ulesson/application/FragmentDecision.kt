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
        "Tutor-2", "Emmanuel Prod",
        "96d4b9cf3a6aecbad8c1b52f12f9092e3e46fadc"
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
                            "sendbird_group_channel_129398966_c5e5996d9d6f4630491ab131074f727e7600bb40",
                            GroupChatFragment.DASHBOARD, "fragdecsion://fragment_start"
                        )
                        findNavController().navigate(direct)
                    },
                    connectionFailed = { err ->
                        Toast.makeText(requireContext(), "${err.message}", Toast.LENGTH_SHORT)
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