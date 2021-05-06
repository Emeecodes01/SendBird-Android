package com.sendbird.android.sample.groupchannel

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.sendbird.android.sample.R
import com.sendbird.android.sample.utils.GenericDialog
import com.sendbird.android.sample.utils.TextUtils

class TutorDummyChatFragment: Fragment() {

    private var dummy_toolbar: Toolbar? = null
    private var layout_group_chat_root: ConstraintLayout? = null
    private var dummy_message_text: TextView? = null
    private var progressBar: ProgressBar? = null

    var joinChatDialog = GenericDialog().newInstance(TextUtils.THEME_MATH)

    /**
     * To create an instance of this fragment, a Channel URL should be required.
     */
    fun newInstance(channelUrl: String = ""): TutorDummyChatFragment {
        //        val args = Bundle()
//        args.putString(GroupChannelListFragment.EXTRA_GROUP_CHANNEL_URL, channelUrl)
//        fragment.arguments = args
        return TutorDummyChatFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //get question text and uri
        timer()
        //checkTutorStatus(0)
    }

    private fun joinChat() {
        //joinChatDialog.show(requireFragmentManager(), "")
        progressBar!!.visibility = View.GONE
    }

    /*private fun checkTutorStatus(tutorStatus: Int) {
        if (tutorStatus == 0) {
            joinChatDialog.setTitle(getString(R.string.tutor_is_ready))
                    .setMessage(getString(R.string.join_chat))
            joinChatDialog.setPositiveButton(R.string.join, R.drawable.bg_btn_complete) {
                joinChatDialog.dismiss()
                Unit
            }
        } else if (tutorStatus == 1) {
            joinChatDialog.setTitle(getString(R.string.no_tutor))
                    .setMessage(getString(R.string.retry_for_tutor))
        } else if (tutorStatus == 2) {
            joinChatDialog.setTitle(getString(R.string.connection_error))
                    .setMessage(getString(R.string.error_connecting))
        } else {
            Toast.makeText(requireContext(), "Something went wrong, please retry", Toast.LENGTH_LONG).show()
        }
        if (tutorStatus == 1 || tutorStatus == 2) {
            joinChatDialog.setPositiveButton(R.string.retry, R.drawable.ic_continue_enabled) {
                joinChatDialog.dismiss()
                Unit
            }
            joinChatDialog.setNegativeButton(R.string.back, null) {
                joinChatDialog.dismiss()
                Unit
            }
        }
    }*/

    fun startChat() {
//        val chat = Chat()
//        chat.createChat()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.dummy_tutor_group_chat, container, false)
        dummy_toolbar = rootView.findViewById(R.id.dummy_toolbar)
        layout_group_chat_root = rootView.findViewById(R.id.layout_group_chat_root)
        dummy_message_text = rootView.findViewById(R.id.dummy_message_text)
        progressBar = rootView.findViewById(R.id.progressBar)
        dummy_message_text?.setText(R.string.sample_question)
        dummy_toolbar?.setNavigationOnClickListener { view: View? -> activity!!.supportFragmentManager.popBackStack() }
        layout_group_chat_root?.setOnClickListener { view: View? -> joinChatDialog.dismiss() }
        return rootView
    }


    private fun timer() {
        object : CountDownTimer(2000, 1000) {
            override fun onTick(l: Long) {}
            override fun onFinish() {
                joinChat()
            }
        }.start()
    }

}