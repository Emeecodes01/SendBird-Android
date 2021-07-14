package com.ulesson.chat.groupchannel

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ulesson.chat.R
import com.ulesson.chat.main.sendBird.ChatActions
import com.ulesson.chat.main.sendBird.TutorActions

class GroupChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        intent?.getStringExtra("channelUrl")?.let {

          val customType =  intent?.getStringExtra("customType") ?:""

            val fragment = tutorActions?.let { tutor ->
                chatActions?.let { chat ->
                    GroupChatFragment.newInstance(it, createChat, customType, true, tutor, chat)
                }
            }

            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container_group_chat, fragment, GroupChatFragment.GROUP_CHAT_TAG)
                    .commit()
            }
        }

    }

    companion object {

        private var tutorActions: TutorActions? = null
        private var chatActions: ChatActions? = null
        private var createChat: Boolean = false

        fun setActions(tutorActions: TutorActions, chatActions: ChatActions, createChat : Boolean) {
            this.tutorActions = tutorActions
            this.chatActions = chatActions
            this.createChat = createChat
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}