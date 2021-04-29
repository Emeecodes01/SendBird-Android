package com.sendbird.android.sample.main

import android.app.Activity
import android.text.TextUtils
import android.util.Log
import com.sendbird.android.SendBird
import com.sendbird.android.SendBird.ConnectHandler
import com.sendbird.android.SendBird.UserInfoUpdateHandler
import com.sendbird.android.SendBirdException
import com.sendbird.android.User
import com.sendbird.android.sample.fcm.MyFirebaseMessagingService
import com.sendbird.android.sample.utils.PreferenceUtils

import com.sendbird.android.sample.utils.PushUtils

class StartSendBird {

    fun start(handler: ConnectHandler) {
        connectToSendBird("taiwo", "taiwo") { user, e -> handler.onConnected(user, e) }
    }

    fun connectToSendBird(userId: String?, userNickname: String, handler: ConnectHandler) {

        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userNickname)) {
            return
        }

        ConnectionManager.login(userId, ConnectHandler { user, e ->

            if (e != null) {
                Log.d("okh", e.message + "")

                return@ConnectHandler
            }
//            PreferenceUtils.setConnected(true)

            // Update the user's nickname
            updateCurrentUserInfo(userNickname)
            PushUtils.registerPushHandler(MyFirebaseMessagingService())
            handler.onConnected(user, e)
        })
    }

    private fun updateCurrentUserInfo(userNickname: String) {

        SendBird.updateCurrentUserInfo(userNickname, null, object : UserInfoUpdateHandler {

            override fun onUpdated(e: SendBirdException?) {
                if (e != null) {
                    return
                }

                PreferenceUtils.setNickname(userNickname)
            }

        })
    }
}