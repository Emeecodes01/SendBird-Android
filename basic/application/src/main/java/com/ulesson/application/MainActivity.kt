package com.ulesson.application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavDestination
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment
import com.sendbird.android.sample.groupchannel.GroupChatFragment
import com.sendbird.android.sample.main.sendBird.Chat
import com.sendbird.android.sample.main.sendBird.UserData

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        findViewById<Button>(R.id.button).setOnClickListener {
//            Chat()
//                .apply {
//                    backToTimeline = {
//                        Toast.makeText(this@MainActivity, "BACK TO TIMELINE", Toast.LENGTH_SHORT)
//                            .show()
//                        onBackPressed()
//                    }
//                    chatSessionEnded = {
//                        Toast.makeText(this@MainActivity, "CHAT SESSION ENDED", Toast.LENGTH_SHORT)
//                            .show()
//                        supportFragmentManager.popBackStack()
//                    }
//                }
//                .enterGroupChat(
//                this,
//                "sendbird_group_channel_90806412_788f636bc06ce4d387ba32b414ee6f2f162c8537",
//                    userData
//            )
//
//        }
//
//        findViewById<Button>(R.id.button2).setOnClickListener {
//            Chat().showAllChat(
//                this, R.id.container,
//                userData
//            )
//        }

    }
}